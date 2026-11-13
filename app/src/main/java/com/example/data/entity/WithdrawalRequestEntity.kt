package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "withdrawals",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["trxId"])
    ]
)
data class WithdrawalRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trxId: String,
    val userId: Long,
    val userMobile: String,
    val paymentMethod: String, // "JazzCash" or "Easypaisa"
    val accountHolderName: String,
    val accountNumber: String,
    val amount: Double,
    // "PENDING", "APPROVED", "REJECTED", "PAID"
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val adminNotes: String? = null
)
