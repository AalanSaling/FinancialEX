package com.example.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import com.example.util.DatabasePassphraseManager
import com.example.util.LegacyDatabaseMigrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        GoalEntity::class,
        FinancialTipEntity::class,
        BillEntity::class,
        InvestmentEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): GoalDao
    abstract fun tipDao(): TipDao
    abstract fun billDao(): BillDao
    abstract fun investmentDao(): InvestmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE transactions ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_1_2 transactions", e)
                }
                try {
                    db.execSQL("ALTER TABLE accounts ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_1_2 accounts", e)
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE goals ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_2_3 goals", e)
                }
                try {
                    db.execSQL("ALTER TABLE bills ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_2_3 bills", e)
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE investments ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_3_4 investments", e)
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE goals ADD COLUMN installmentValue REAL NOT NULL DEFAULT 0.0")
                    db.execSQL("ALTER TABLE goals ADD COLUMN totalInstallments INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE goals ADD COLUMN paidInstallments INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE goals ADD COLUMN dueDayOfMonth INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE goals ADD COLUMN isInstallmentMode INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_4_5 goals", e)
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE goals ADD COLUMN paymentHistoryJson TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_5_6 goals", e)
                }
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE investments ADD COLUMN originAccountName TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE investments ADD COLUMN additionalAporte REAL NOT NULL DEFAULT 0.0")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_6_7 investments", e)
                }
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE investments ADD COLUMN isHistorical INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE investments ADD COLUMN movementHistoryJson TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error in MIGRATION_7_8 investments", e)
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                try {
                    System.loadLibrary("sqlcipher")
                } catch (e: Throwable) {
                    Log.e("AppDatabase", "Failed to load sqlcipher native library", e)
                }
                val legacyFile = LegacyDatabaseMigrator.prepareLegacyFileForMigration(appContext, "financas_db")
                val passphrase = DatabasePassphraseManager.getOrGeneratePassphrase(appContext)
                val factory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "financas_db"
                )
                .openHelperFactory(factory)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8
                )
                .build()

                INSTANCE = instance

                if (legacyFile != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        LegacyDatabaseMigrator.migrateLegacyDataIfNeeded(appContext, legacyFile, instance)
                    }
                }

                instance
            }
        }
    }
}
