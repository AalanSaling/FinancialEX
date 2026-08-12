package com.example.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

object LegacyDatabaseMigrator {

    fun isLegacyUnencryptedDatabase(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists() || dbFile.length() < 16) return false

        return try {
            RandomAccessFile(dbFile, "r").use { raf ->
                val header = ByteArray(16)
                raf.readFully(header)
                val headerStr = String(header, Charsets.US_ASCII)
                headerStr.startsWith("SQLite format 3")
            }
        } catch (_: Exception) {
            false
        }
    }

    fun prepareLegacyFileForMigration(context: Context, dbName: String): File? {
        if (!isLegacyUnencryptedDatabase(context, dbName)) return null

        val dbFile = context.getDatabasePath(dbName)
        val legacyFile = context.getDatabasePath("${dbName}_legacy_unencrypted")

        // Rename main file
        if (dbFile.renameTo(legacyFile)) {
            val walFile = context.getDatabasePath("$dbName-wal")
            if (walFile.exists()) walFile.renameTo(context.getDatabasePath("${dbName}_legacy_unencrypted-wal"))
            val shmFile = context.getDatabasePath("$dbName-shm")
            if (shmFile.exists()) shmFile.renameTo(context.getDatabasePath("${dbName}_legacy_unencrypted-shm"))
            return legacyFile
        }
        return null
    }

    suspend fun migrateLegacyDataIfNeeded(context: Context, legacyFile: File, newEncryptedDb: AppDatabase) {
        withContext(Dispatchers.IO) {
            var legacyDb: SQLiteDatabase? = null
            try {
                legacyDb = SQLiteDatabase.openDatabase(legacyFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

                // Read Accounts
                try {
                    val cursor = legacyDb.rawQuery("SELECT * FROM accounts", null)
                    while (cursor.moveToNext()) {
                        val idIdx = cursor.getColumnIndex("id")
                        val nameIdx = cursor.getColumnIndex("name")
                        val balanceIdx = cursor.getColumnIndex("balance")
                        val currIdx = cursor.getColumnIndex("currency")
                        val wsIdx = cursor.getColumnIndex("workspaceName")

                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                        val bankName = try { val idx = cursor.getColumnIndex("bankName"); if (idx >= 0) cursor.getString(idx) else "Manual" } catch (_: Exception) { "Manual" }
                        val accountType = try { val idx = cursor.getColumnIndex("accountType"); if (idx >= 0) cursor.getString(idx) else "Checking" } catch (_: Exception) { "Checking" }
                        val balance = if (balanceIdx >= 0) cursor.getDouble(balanceIdx) else 0.0
                        val currency = if (currIdx >= 0) cursor.getString(currIdx) ?: "PYG" else "PYG"
                        val ws = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "Pessoal" else "Pessoal"

                        newEncryptedDb.accountDao().insertAccount(AccountEntity(id = id, name = name, bankName = bankName, accountType = accountType, balance = balance, currency = currency, workspaceName = ws))
                    }
                    cursor.close()
                } catch (e: Exception) {
                    android.util.Log.e("LegacyMigrator", "Error copying accounts", e)
                }

                // Read Transactions
                try {
                    val cursor = legacyDb.rawQuery("SELECT * FROM transactions", null)
                    while (cursor.moveToNext()) {
                        val idIdx = cursor.getColumnIndex("id")
                        val titleIdx = cursor.getColumnIndex("title")
                        val amountIdx = cursor.getColumnIndex("amount")
                        val typeIdx = cursor.getColumnIndex("type")
                        val catIdx = cursor.getColumnIndex("category")
                        val dateIdx = cursor.getColumnIndex("dateMillis")
                        val accNameIdx = cursor.getColumnIndex("accountName")
                        val notesIdx = cursor.getColumnIndex("notes")
                        val wsIdx = cursor.getColumnIndex("workspaceName")

                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                        val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                        val amount = if (amountIdx >= 0) cursor.getDouble(amountIdx) else 0.0
                        val typeStr = if (typeIdx >= 0) cursor.getString(typeIdx) ?: "EXPENSE" else "EXPENSE"
                        val typeEnum = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.EXPENSE }
                        val cat = if (catIdx >= 0) cursor.getString(catIdx) ?: "Outros" else "Outros"
                        val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else System.currentTimeMillis()
                        val accName = if (accNameIdx >= 0) cursor.getString(accNameIdx) ?: "" else ""
                        val notes = if (notesIdx >= 0) cursor.getString(notesIdx) ?: "" else ""
                        val ws = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "Pessoal" else "Pessoal"

                        newEncryptedDb.transactionDao().insertTransaction(TransactionEntity(id = id, title = title, amount = amount, type = typeEnum, category = cat, dateMillis = date, accountName = accName, notes = notes, workspaceName = ws))
                    }
                    cursor.close()
                } catch (e: Exception) {
                    android.util.Log.e("LegacyMigrator", "Error copying transactions", e)
                }

                legacyDb.close()
                legacyDb = null
                legacyFile.delete()
                context.getDatabasePath("${legacyFile.name}-wal").delete()
                context.getDatabasePath("${legacyFile.name}-shm").delete()
            } catch (e: Exception) {
                android.util.Log.e("LegacyMigrator", "Legacy migration error", e)
                try { legacyDb?.close() } catch (_: Exception) {}
            }
        }
    }
}
