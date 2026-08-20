package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String = "Bills & Utilities",
    val dueDayOfMonth: Int = 1,
    val isPaidThisMonth: Boolean = false,
    val frequency: String = "Monthly"
)
