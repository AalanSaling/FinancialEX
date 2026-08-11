package com.example.util

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class InvestmentMovementLog(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "APORTE", "RESGATE", "RENDIMENTO"
    val amount: Double,
    val accountName: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

object InvestmentHistoryHelper {
    fun parseLogs(json: String): List<InvestmentMovementLog> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<InvestmentMovementLog>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvestmentMovementLog(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        type = obj.optString("type", "APORTE"),
                        amount = obj.optDouble("amount", 0.0),
                        accountName = obj.optString("accountName", ""),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeLogs(logs: List<InvestmentMovementLog>): String {
        val array = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("type", log.type)
                put("amount", log.amount)
                put("accountName", log.accountName)
                put("dateMillis", log.dateMillis)
                put("notes", log.notes)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
