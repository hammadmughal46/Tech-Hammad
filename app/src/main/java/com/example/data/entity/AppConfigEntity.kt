package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val dailyAdsLimit: Int = 5,
    val rewardPerAd: Double = 50.0,
    val adResetPeriodHours: Int = 24,
    val minDeposit: Double = 300.0,
    val minWithdrawal: Double = 800.0,
    val welcomeBonus: Double = 100.0,
    val dailyBonus: Double = 25.0,
    val depositRequiredBeforeAds: Boolean = true,
    val depositAccountName: String = "SAHANA SAHANA",
    val depositAccountNumber: String = "03174254463",
    val depositPaymentMethod: String = "Easypaisa"
) {
    val maxDailyAdEarnings: Double
        get() = dailyAdsLimit * rewardPerAd
}
