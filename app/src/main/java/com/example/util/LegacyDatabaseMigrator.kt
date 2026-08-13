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
            var accountCount = 0
            var txCount = 0
            var goalCount = 0
            var billCount = 0
            var invCount = 0

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
                        accountCount++
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
                        txCount++
                    }
                    cursor.close()
                } catch (e: Exception) {
                    android.util.Log.e("LegacyMigrator", "Error copying transactions", e)
                }

                // Read Goals
                try {
                    val cursor = legacyDb.rawQuery("SELECT * FROM goals", null)
                    while (cursor.moveToNext()) {
                        val idIdx = cursor.getColumnIndex("id")
                        val titleIdx = cursor.getColumnIndex("title")
                        val targetIdx = cursor.getColumnIndex("targetAmount")
                        val currentIdx = cursor.getColumnIndex("currentAmount")
                        val deadlineIdx = cursor.getColumnIndex("deadlineMillis")
                        val categoryIdx = cursor.getColumnIndex("category")
                        val wsIdx = cursor.getColumnIndex("workspaceName")

                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                        val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                        val target = if (targetIdx >= 0) cursor.getDouble(targetIdx) else 0.0
                        val current = if (currentIdx >= 0) cursor.getDouble(currentIdx) else 0.0
                        val deadline = if (deadlineIdx >= 0) cursor.getLong(deadlineIdx) else System.currentTimeMillis()
                        val category = if (categoryIdx >= 0) cursor.getString(categoryIdx) ?: "Outros" else "Outros"
                        val ws = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "Pessoal" else "Pessoal"

                        newEncryptedDb.goalDao().insertGoal(
                            GoalEntity(
                                id = id,
                                title = title,
                                targetAmount = target,
                                currentAmount = current,
                                deadlineMillis = deadline,
                                category = category,
                                workspaceName = ws
                            )
                        )
                        goalCount++
                    }
                    cursor.close()
                } catch (e: Exception) {
                    android.util.Log.w("LegacyMigrator", "No goals table found or error reading goals", e)
                }

                // Read Bills
                try {
                    val cursor = legacyDb.rawQuery("SELECT * FROM bills", null)
                    while (cursor.moveToNext()) {
                        val idIdx = cursor.getColumnIndex("id")
                        val titleIdx = cursor.getColumnIndex("title")
                        val amountIdx = cursor.getColumnIndex("amount")
                        val dueIdx = cursor.getColumnIndex("dueDateMillis")
                        val isPaidIdx = cursor.getColumnIndex("isPaid")
                        val catIdx = cursor.getColumnIndex("category")
                        val wsIdx = cursor.getColumnIndex("workspaceName")

                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                        val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                        val amount = if (amountIdx >= 0) cursor.getDouble(amountIdx) else 0.0
                        val due = if (dueIdx >= 0) cursor.getLong(dueIdx) else System.currentTimeMillis()
                        val isPaid = if (isPaidIdx >= 0) cursor.getInt(isPaidIdx) == 1 else false
                        val cat = if (catIdx >= 0) cursor.getString(catIdx) ?: "Outros" else "Outros"
                        val ws = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "Pessoal" else "Pessoal"

                        newEncryptedDb.billDao().insertBill(
                            BillEntity(
                                id = id,
                                title = title,
                                amount = amount,
                                dueDateMillis = due,
                                isPaid = isPaid,
                                category = cat,
                                workspaceName = ws
                            )
                        )
                        billCount++
                    }
                    cursor.close()
                } catch (e: Exception) {
                    android.util.Log.w("LegacyMigrator", "No bills table found or error reading bills", e)
                }

                // Read Investments
                try {
                    val cursor = legacyDb.rawQuery("SELECT * FROM investments", null)
                    while (cursor.moveToNext()) {
                        val idIdx = cursor.getColumnIndex("id")
                        val titleIdx = cursor.getColumnIndex("title")
                        val typeIdx = cursor.getColumnIndex("type")
                        val instIdx = cursor.getColumnIndex("institution")
                        val amountIdx = cursor.getColumnIndex("amountInvested")
                        val valCurrIdx = cursor.getColumnIndex("currentValue")
                        val yieldIdx = cursor.getColumnIndex("yieldRate")
                        val currIdx = cursor.getColumnIndex("currency")
                        val notesIdx = cursor.getColumnIndex("notes")
                        val wsIdx = cursor.getColumnIndex("workspaceName")

                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                        val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                        val typeStr = if (typeIdx >= 0) cursor.getString(typeIdx) ?: "MUTUAL_FUND" else "MUTUAL_FUND"
                        val typeEnum = try { InvestmentType.valueOf(typeStr) } catch (_: Exception) { InvestmentType.MUTUAL_FUND }
                        val inst = if (instIdx >= 0) cursor.getString(instIdx) ?: "" else ""
                        val amount = if (amountIdx >= 0) cursor.getDouble(amountIdx) else 0.0
                        val valCurr = if (valCurrIdx >= 0) cursor.getDouble(valCurrIdx) else 0.0
                        val yield = if (yieldIdx >= 0) cursor.getDouble(yieldIdx) else 0.0
                        val curr = if (currIdx >= 0) cursor.getString(currIdx) ?: "PYG" else "PYG"
                        val notes = if (notesIdx >= 0) cursor.getString(notesIdx) ?: "" else ""
                        val ws = if (wsIdx >= 0) cursor.getString(wsIdx) ?: "Pessoal" else "Pessoal"

                        newEncryptedDb.investmentDao().insertInvestment(
                            InvestmentEntity(
                                id = id,
                                title = title,
                                type = typeEnum,
                                institution = inst,
                                amountInvested = amount,
                                currentValue = valCurr,
                                yieldRate = yield,
                                currency = curr,
                                notes = notes,
                                workspaceName = ws
                            )
                        )
                        invCount++
                    }
                    cursor.close()
                } catch (e: Exception) {
                    android.util.Log.w("LegacyMigrator", "No investments table found or error reading investments", e)
                }

                android.util.Log.i(
                    "LegacyMigrator",
                    "Migração legada concluída com sucesso: copiadas $accountCount contas, $txCount transações, $goalCount metas, $billCount contas a pagar, $invCount investimentos"
                )

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
