package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val dueDateMillis: Long,
    val recurrence: String = "MENSAL", // MENSAL, SEMANAL, ANUAL, UNICO
    val isPaid: Boolean = false,
    val currency: String = "BRL",
    val notifyDaysBefore: Int = 2,
    val workspaceName: String = "Pessoal"
)
