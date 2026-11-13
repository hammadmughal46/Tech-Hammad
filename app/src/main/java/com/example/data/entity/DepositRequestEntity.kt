package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deposits",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["trxId"])
    ]
)
data class DepositRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trxId: String,
    val userId: Long,
    val userMobile: String,
    val amount: Double,
    val senderName: String,
    val senderMobile: String,
    val paymentMethod: String = "Easypaisa",
    val screenshotUri: String? = null,
    // "PENDING", "APPROVED", "REJECTED"
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val adminNotes: String? = null
)
