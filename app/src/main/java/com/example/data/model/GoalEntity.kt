package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Category linked to transactions or custom goal e.g. "Alimentação", "Reserva de Emergência", "Viagem"
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val monthlyLimit: Double = 0.0, // Limit for category spending
    val deadlineMillis: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
    val isCategoryBudget: Boolean = false, // true = limite de gastos por categoria, false = meta de economia
    val workspaceName: String = "Pessoal",
    val installmentValue: Double = 0.0, // Monthly/Installment payment amount
    val totalInstallments: Int = 0, // Total number of installments
    val paidInstallments: Int = 0, // Number of paid installments
    val dueDayOfMonth: Int = 0, // Due day of month (e.g. 10th)
    val isInstallmentMode: Boolean = false,
    val paymentHistoryJson: String = ""
)

data class GoalPaymentLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val paymentDateMillis: Long = System.currentTimeMillis(),
    val installmentNumber: Int = 0,
    val note: String = ""
)

object GoalPaymentHistoryHelper {
    fun parseLogs(jsonStr: String): List<GoalPaymentLog> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<GoalPaymentLog>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GoalPaymentLog(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        amount = obj.optDouble("amount", 0.0),
                        paymentDateMillis = obj.optLong("paymentDateMillis", System.currentTimeMillis()),
                        installmentNumber = obj.optInt("installmentNumber", 0),
                        note = obj.optString("note", "")
                    )
                )
            }
            list.sortedByDescending { it.paymentDateMillis }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeLogs(logs: List<GoalPaymentLog>): String {
        val array = org.json.JSONArray()
        logs.forEach { log ->
            val obj = org.json.JSONObject()
            obj.put("id", log.id)
            obj.put("amount", log.amount)
            obj.put("paymentDateMillis", log.paymentDateMillis)
            obj.put("installmentNumber", log.installmentNumber)
            obj.put("note", log.note)
            array.put(obj)
        }
        return array.toString()
    }
}

data class BudgetItemProposal(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var category: String,
    var amount: Double,
    var isCategoryBudget: Boolean = true
)

