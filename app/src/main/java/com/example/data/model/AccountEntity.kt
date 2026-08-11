package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bankName: String, // Nubank, Itaú, Bradesco, Inter, Binance, Manual
    val accountType: String, // Checking, Savings, Credit Card, Crypto
    val balance: Double,
    val currency: String = "BRL",
    val workspaceName: String = "Pessoal",
    val lastSyncedMillis: Long = System.currentTimeMillis()
)
