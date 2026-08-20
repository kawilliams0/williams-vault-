package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "CHECKING", "SAVINGS", "CASH", "CREDIT_CARD", "INVESTMENT"
    val balance: Double,
    val accountNumberLast4: String = "",
    val colorHex: String = "#10B981"
)
