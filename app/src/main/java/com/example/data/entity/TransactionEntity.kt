package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["trxId"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trxId: String,
    val userId: Long,
    val userMobile: String,
    // "WELCOME_BONUS", "AD_REWARD", "DAILY_BONUS", "DEPOSIT", "WITHDRAWAL", "REFUND"
    val type: String,
    val title: String,
    val amount: Double, // positive for credits, negative for debits
    // "COMPLETED", "PENDING", "APPROVED", "REJECTED", "PAID"
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
)
