package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["mobileNumber"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mobileNumber: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSuspended: Boolean = false,
    
    // Financial balances
    val totalEarnings: Double = 100.0, // Starts with 100 PKR welcome bonus
    val availableBalance: Double = 100.0, // Starts with 100 PKR welcome bonus
    val todayEarnings: Double = 0.0,
    val welcomeBonusClaimed: Boolean = true, // Received at signup
    
    // Deposit state: NONE, PENDING, APPROVED, REJECTED
    val depositStatus: String = "NONE",
    val approvedDepositAmount: Double = 0.0,
    
    // Ads tracking
    val adsWatchedTotal: Int = 0,
    val adsWatchedToday: Int = 0,
    val lastAdResetTime: Long = System.currentTimeMillis(),
    
    // Daily bonus
    val lastDailyBonusClaimTime: Long = 0L,
    
    // Saved payment info (optional profile convenience)
    val savedPaymentMethod: String? = null,
    val savedAccountHolder: String? = null,
    val savedAccountNumber: String? = null
)
