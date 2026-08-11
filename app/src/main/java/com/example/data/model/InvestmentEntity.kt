package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.AppLanguage

enum class InvestmentType(val displayName: String) {
    MUTUAL_FUND("Fondo Mutuo"),
    CDA("CDA / Depósito a Plazo"),
    STOCKS("Acciones"),
    BONDS("Bonos");

    fun getLocalizedName(lang: AppLanguage): String {
        return when (this) {
            MUTUAL_FUND -> when (lang) {
                AppLanguage.PORTUGUESE -> "Fundos Mútuos"
                AppLanguage.SPANISH -> "Fondos Mutuos"
                AppLanguage.ENGLISH -> "Mutual Funds"
            }
            CDA -> when (lang) {
                AppLanguage.PORTUGUESE -> "CDB / CDA (Depósito a Prazo)"
                AppLanguage.SPANISH -> "CDA / Depósito a Plazo"
                AppLanguage.ENGLISH -> "CDA / Time Deposit"
            }
            STOCKS -> when (lang) {
                AppLanguage.PORTUGUESE -> "Ações"
                AppLanguage.SPANISH -> "Acciones"
                AppLanguage.ENGLISH -> "Stocks / Shares"
            }
            BONDS -> when (lang) {
                AppLanguage.PORTUGUESE -> "Títulos / Bonos"
                AppLanguage.SPANISH -> "Bonos"
                AppLanguage.ENGLISH -> "Bonds"
            }
        }
    }
}

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: InvestmentType = InvestmentType.MUTUAL_FUND,
    val institution: String, // e.g., "Basa Capital", "Itaú Asset", "Cadiem"
    val amountInvested: Double,
    val currentValue: Double,
    val yieldRate: Double = 0.0, // e.g. 8.5% p.a.
    val currency: String = "PYG",
    val firstDepositDateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isHistorical: Boolean = false,
    val movementHistoryJson: String = "",
    val workspaceName: String = "Pessoal",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
