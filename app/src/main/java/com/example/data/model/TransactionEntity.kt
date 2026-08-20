package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME", "TRANSFER"
    val category: String,
    val accountId: Long = 1,
    val accountName: String = "Main Checking",
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
