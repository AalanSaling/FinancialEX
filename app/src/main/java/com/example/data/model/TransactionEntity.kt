package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,          // Entradas
    EXPENSE,         // Saídas / Despesas
    FUTURE_EXPENSE,  // Futuros Gastos / Contas a Pagar
    RECEIVABLE,      // A Receber
    INVESTMENT       // Investimentos
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String, // e.g. "Alimentação", "Transporte", "Moradia", "Lazer", "Investimentos", "Saúde", "Educação", "Outros"
    val dateMillis: Long = System.currentTimeMillis(),
    val accountName: String = "Conta Principal",
    val currency: String = "BRL", // BRL, USD, EUR, BTC, ETH
    val notes: String = "",
    val workspaceName: String = "Pessoal",
    val isOfflinePending: Boolean = false,
    val isAutoSynced: Boolean = false
)
