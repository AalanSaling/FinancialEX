package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    exportSchema = false
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
                try { db.execSQL("ALTER TABLE transactions ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE accounts ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'") } catch (_: Exception) {}
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE goals ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE bills ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'") } catch (_: Exception) {}
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE investments ADD COLUMN workspaceName TEXT NOT NULL DEFAULT 'Pessoal'") } catch (_: Exception) {}
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE goals ADD COLUMN installmentValue REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE goals ADD COLUMN totalInstallments INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE goals ADD COLUMN paidInstallments INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE goals ADD COLUMN dueDayOfMonth INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE goals ADD COLUMN isInstallmentMode INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE goals ADD COLUMN paymentHistoryJson TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE investments ADD COLUMN originAccountName TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE investments ADD COLUMN additionalAporte REAL NOT NULL DEFAULT 0.0") } catch (_: Exception) {}
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE investments ADD COLUMN isHistorical INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE investments ADD COLUMN movementHistoryJson TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "financas_db"
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
