package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class FinancialTipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String,
    val potentialSavingsMonthly: Double = 0.0,
    val isSaved: Boolean = false,
    val tipType: String = "SAVING" // SAVING, INVESTMENT, WARNING
)
