package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DepositRequestEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WithdrawalRequestEntity
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PakAdsRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val transactionDao = database.transactionDao()
    private val depositDao = database.depositDao()
    private val withdrawalDao = database.withdrawalDao()
    private val configDao = database.configDao()
    private val notificationDao = database.notificationDao()

    val configFlow: Flow<AppConfigEntity?> = configDao.getConfig()
    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allDepositsFlow: Flow<List<DepositRequestEntity>> = depositDao.getAllDeposits()
    val allWithdrawalsFlow: Flow<List<WithdrawalRequestEntity>> = withdrawalDao.getAllWithdrawals()
    val allTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun getAppConfig(): AppConfigEntity {
        return withContext(Dispatchers.IO) {
            configDao.getConfigSync() ?: run {
                val defaultConfig = AppConfigEntity()
                configDao.insertOrUpdateConfig(defaultConfig)
                defaultConfig
            }
        }
    }

    suspend fun updateAppConfig(config: AppConfigEntity) {
        withContext(Dispatchers.IO) {
            configDao.insertOrUpdateConfig(config)
        }
    }

    // -------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------

    suspend fun registerUser(
        mobile: String,
        password: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val cleanMobile = mobile.trim()
        if (cleanMobile.length < 10) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid mobile number (min 10 digits)."))
        }

        val existingUser = userDao.getUserByMobile(cleanMobile)
        if (existingUser != null) {
            return@withContext Result.failure(IllegalStateException("This mobile number is already registered."))
        }

        val config = getAppConfig()
        val welcomeAmount = config.welcomeBonus

        val newUser = UserEntity(
            mobileNumber = cleanMobile,
            passwordHash = SessionManager.hashPassword(password),
            createdAt = System.currentTimeMillis(),
            totalEarnings = welcomeAmount,
            availableBalance = welcomeAmount,
            todayEarnings = 0.0,
            welcomeBonusClaimed = true,
            depositStatus = "NONE"
        )

        val newUserId = userDao.insertUser(newUser)

        // Record Welcome Bonus transaction
        val trxId = generateTrxId("WB")
        val welcomeTrx = TransactionEntity(
            trxId = trxId,
            userId = newUserId,
            userMobile = cleanMobile,
            type = "WELCOME_BONUS",
            title = "Welcome Bonus",
            amount = welcomeAmount,
            status = "COMPLETED",
            notes = "One-time registration reward"
        )
        transactionDao.insertTransaction(welcomeTrx)

        // Notification
        val notif = NotificationEntity(
            userId = newUserId,
            title = "Welcome to PAK ADS! 🎉",
            message = "Your account is created! ${welcomeAmount.toInt()} PKR Welcome Bonus has been credited to your available balance.",
            type = "WELCOME"
        )
        notificationDao.insertNotification(notif)

        Result.success(newUserId)
    }

    suspend fun loginUser(
        mobile: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanMobile = mobile.trim()
        val user = userDao.getUserByMobile(cleanMobile)
            ?: return@withContext Result.failure(IllegalArgumentException("Mobile number not found. Please register first."))

        if (user.isSuspended) {
            return@withContext Result.failure(IllegalStateException("Account suspended. Please contact Admin."))
        }

        val inputHash = SessionManager.hashPassword(password)
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        // Check and apply 24h ad reset on login
        checkAndApplyAdReset(user)

        val refreshedUser = userDao.getUserByIdSync(user.id) ?: user
        Result.success(refreshedUser)
    }

    fun observeUser(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun getUserSync(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByIdSync(userId)
    }

    // -------------------------------------------------------------
    // Ad Reset & Watch & Earn
    // -------------------------------------------------------------

    suspend fun checkAndApplyAdReset(user: UserEntity): UserEntity = withContext(Dispatchers.IO) {
        val config = getAppConfig()
        val resetPeriodMillis = config.adResetPeriodHours * 3600 * 1000L
        val now = System.currentTimeMillis()

        if (now - user.lastAdResetTime >= resetPeriodMillis) {
            val updatedUser = user.copy(
                adsWatchedToday = 0,
                todayEarnings = 0.0,
                lastAdResetTime = now
            )
            userDao.updateUser(updatedUser)
            updatedUser
        } else {
            user
        }
    }

    suspend fun completeRewardedAd(userId: Long): Result<Double> = withContext(Dispatchers.IO) {
        var user = userDao.getUserByIdSync(userId)
            ?: return@withContext Result.failure(IllegalStateException("User session not found."))

        val config = getAppConfig()

        // 1. Verify Deposit Requirement
        if (config.depositRequiredBeforeAds && user.depositStatus != "APPROVED") {
            return@withContext Result.failure(IllegalStateException(
                "Please make a deposit first. Your ad-watching feature will be activated after your deposit is approved by the Admin."
            ))
        }

        // 2. Check 24-hour cycle reset
        user = checkAndApplyAdReset(user)

        // 3. Check daily limit
        if (user.adsWatchedToday >= config.dailyAdsLimit) {
            return@withContext Result.failure(IllegalStateException(
                "Daily ad limit reached (${config.dailyAdsLimit}/${config.dailyAdsLimit}). Please wait for the 24-hour reset timer."
            ))
        }

        val rewardAmount = config.rewardPerAd
        val newAdsWatchedToday = user.adsWatchedToday + 1
        val newAdsWatchedTotal = user.adsWatchedTotal + 1
        val newAvailableBalance = user.availableBalance + rewardAmount
        val newTotalEarnings = user.totalEarnings + rewardAmount
        val newTodayEarnings = user.todayEarnings + rewardAmount

        val updatedUser = user.copy(
            adsWatchedToday = newAdsWatchedToday,
            adsWatchedTotal = newAdsWatchedTotal,
            availableBalance = newAvailableBalance,
            totalEarnings = newTotalEarnings,
            todayEarnings = newTodayEarnings
        )
        userDao.updateUser(updatedUser)

        // Record Transaction
        val trxId = generateTrxId("AD")
        val trx = TransactionEntity(
            trxId = trxId,
            userId = user.id,
            userMobile = user.mobileNumber,
            type = "AD_REWARD",
            title = "Rewarded Ad #$newAdsWatchedToday Reward",
            amount = rewardAmount,
            status = "COMPLETED",
            notes = "Ad reward verified and credited"
        )
        transactionDao.insertTransaction(trx)

        // Record Notification
        val notif = NotificationEntity(
            userId = user.id,
            title = "Ad Reward Received! 💰",
            message = "You earned ${rewardAmount.toInt()} PKR for watching ad #$newAdsWatchedToday/$config.dailyAdsLimit.",
            type = "AD_REWARD"
        )
        notificationDao.insertNotification(notif)

        Result.success(rewardAmount)
    }

    // -------------------------------------------------------------
    // Daily Bonus
    // -------------------------------------------------------------

    suspend fun claimDailyBonus(userId: Long): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId)
            ?: return@withContext Result.failure(IllegalStateException("User not found."))

        val config = getAppConfig()
        val now = System.currentTimeMillis()
        val cooldownMillis = 24 * 3600 * 1000L

        if (user.lastDailyBonusClaimTime > 0 && (now - user.lastDailyBonusClaimTime) < cooldownMillis) {
            val remainingMillis = cooldownMillis - (now - user.lastDailyBonusClaimTime)
            val hours = remainingMillis / (3600 * 1000)
            val minutes = (remainingMillis % (3600 * 1000)) / (60 * 1000)
            return@withContext Result.failure(IllegalStateException(
                "Daily bonus already claimed! Next claim in ${hours}h ${minutes}m."
            ))
        }

        val bonusAmount = config.dailyBonus
        val updatedUser = user.copy(
            availableBalance = user.availableBalance + bonusAmount,
            totalEarnings = user.totalEarnings + bonusAmount,
            todayEarnings = user.todayEarnings + bonusAmount,
            lastDailyBonusClaimTime = now
        )
        userDao.updateUser(updatedUser)

        val trxId = generateTrxId("DB")
        val trx = TransactionEntity(
            trxId = trxId,
            userId = user.id,
            userMobile = user.mobileNumber,
            type = "DAILY_BONUS",
            title = "Daily Bonus",
            amount = bonusAmount,
            status = "COMPLETED"
        )
        transactionDao.insertTransaction(trx)

        val notif = NotificationEntity(
            userId = user.id,
            title = "Daily Bonus Claimed! 🎁",
            message = "You received ${bonusAmount.toInt()} PKR daily bonus!",
            type = "DAILY_BONUS"
        )
        notificationDao.insertNotification(notif)

        Result.success(bonusAmount)
    }

    // -------------------------------------------------------------
    // Deposit System
    // -------------------------------------------------------------

    suspend fun submitDeposit(
        userId: Long,
        amount: Double,
        senderName: String,
        senderMobile: String,
        trxReference: String,
        screenshotUri: String?
    ): Result<DepositRequestEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId)
            ?: return@withContext Result.failure(IllegalStateException("User not found."))

        val config = getAppConfig()
        if (amount < config.minDeposit) {
            return@withContext Result.failure(IllegalArgumentException(
                "Minimum deposit amount is ${config.minDeposit.toInt()} PKR."
            ))
        }
        if (senderName.trim().isEmpty() || senderMobile.trim().isEmpty() || trxReference.trim().isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please fill in all deposit details."))
        }

        val deposit = DepositRequestEntity(
            trxId = trxReference.trim(),
            userId = user.id,
            userMobile = user.mobileNumber,
            amount = amount,
            senderName = senderName.trim(),
            senderMobile = senderMobile.trim(),
            paymentMethod = "Easypaisa",
            screenshotUri = screenshotUri,
            status = "PENDING"
        )
        val depositId = depositDao.insertDeposit(deposit)

        // Update user depositStatus to PENDING if not already APPROVED
        if (user.depositStatus != "APPROVED") {
            userDao.updateDepositStatus(user.id, "PENDING", user.approvedDepositAmount)
        }

        // Ledger record
        val trx = TransactionEntity(
            trxId = trxReference.trim(),
            userId = user.id,
            userMobile = user.mobileNumber,
            type = "DEPOSIT",
            title = "Easypaisa Deposit",
            amount = amount,
            status = "PENDING",
            notes = "Pending Admin verification"
        )
        transactionDao.insertTransaction(trx)

        // Notification
        val notif = NotificationEntity(
            userId = user.id,
            title = "Deposit Submitted ⏳",
            message = "Your deposit of ${amount.toInt()} PKR (TRX: ${trxReference.trim()}) is under review by admin.",
            type = "DEPOSIT_SUBMITTED"
        )
        notificationDao.insertNotification(notif)

        Result.success(deposit.copy(id = depositId))
    }

    // -------------------------------------------------------------
    // Withdrawal System
    // -------------------------------------------------------------

    suspend fun submitWithdrawal(
        userId: Long,
        method: String,
        holderName: String,
        accountNumber: String,
        amount: Double,
        saveDetails: Boolean
    ): Result<WithdrawalRequestEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId)
            ?: return@withContext Result.failure(IllegalStateException("User not found."))

        val config = getAppConfig()
        if (amount < config.minWithdrawal) {
            return@withContext Result.failure(IllegalArgumentException(
                "Minimum withdrawal amount is ${config.minWithdrawal.toInt()} PKR."
            ))
        }

        if (user.availableBalance < amount) {
            return@withContext Result.failure(IllegalArgumentException(
                "Insufficient balance. Available: ${user.availableBalance.toInt()} PKR."
            ))
        }

        if (holderName.trim().isEmpty() || accountNumber.trim().isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please fill in account details."))
        }

        val trxId = generateTrxId("WD")

        // Deduct available balance
        val updatedUser = user.copy(
            availableBalance = user.availableBalance - amount,
            savedPaymentMethod = if (saveDetails) method else user.savedPaymentMethod,
            savedAccountHolder = if (saveDetails) holderName.trim() else user.savedAccountHolder,
            savedAccountNumber = if (saveDetails) accountNumber.trim() else user.savedAccountNumber
        )
        userDao.updateUser(updatedUser)

        val withdrawal = WithdrawalRequestEntity(
            trxId = trxId,
            userId = user.id,
            userMobile = user.mobileNumber,
            paymentMethod = method,
            accountHolderName = holderName.trim(),
            accountNumber = accountNumber.trim(),
            amount = amount,
            status = "PENDING"
        )
        val withdrawalId = withdrawalDao.insertWithdrawal(withdrawal)

        // Record debit transaction
        val trx = TransactionEntity(
            trxId = trxId,
            userId = user.id,
            userMobile = user.mobileNumber,
            type = "WITHDRAWAL",
            title = "$method Withdrawal",
            amount = -amount,
            status = "PENDING",
            notes = "To $holderName ($accountNumber)"
        )
        transactionDao.insertTransaction(trx)

        // Notification
        val notif = NotificationEntity(
            userId = user.id,
            title = "Withdrawal Submitted ⏳",
            message = "Withdrawal request for ${amount.toInt()} PKR via $method has been placed.",
            type = "WITHDRAWAL_SUBMITTED"
        )
        notificationDao.insertNotification(notif)

        Result.success(withdrawal.copy(id = withdrawalId))
    }

    // -------------------------------------------------------------
    // Admin Deposit Management
    // -------------------------------------------------------------

    suspend fun reviewDeposit(
        depositId: Long,
        newStatus: String, // "APPROVED", "REJECTED", "PENDING"
        adminNote: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val deposit = depositDao.getDepositById(depositId)
            ?: return@withContext Result.failure(IllegalStateException("Deposit request not found."))

        val user = userDao.getUserByIdSync(deposit.userId)
            ?: return@withContext Result.failure(IllegalStateException("User not found."))

        val updatedDeposit = deposit.copy(
            status = newStatus,
            reviewedAt = System.currentTimeMillis(),
            adminNotes = adminNote
        )
        depositDao.updateDeposit(updatedDeposit)
        transactionDao.updateTransactionStatus(deposit.trxId, newStatus)

        when (newStatus) {
            "APPROVED" -> {
                // Credit deposit amount to user's available balance and activate Watch & Earn
                val updatedUser = user.copy(
                    availableBalance = user.availableBalance + deposit.amount,
                    depositStatus = "APPROVED",
                    approvedDepositAmount = user.approvedDepositAmount + deposit.amount
                )
                userDao.updateUser(updatedUser)

                // Notification: Deposit Approved and Ads Activated
                notificationDao.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Deposit Approved & Ads Activated! 🚀",
                    message = "Your Easypaisa deposit of ${deposit.amount.toInt()} PKR is approved! Ad watching is now UNLOCKED.",
                    type = "DEPOSIT_APPROVED"
                ))
            }
            "REJECTED" -> {
                // If user had no other approved deposit, set depositStatus = REJECTED
                val updatedUser = user.copy(
                    depositStatus = if (user.approvedDepositAmount > 0) "APPROVED" else "REJECTED"
                )
                userDao.updateUser(updatedUser)

                notificationDao.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Deposit Rejected ❌",
                    message = "Your deposit of ${deposit.amount.toInt()} PKR was rejected. Reason: ${adminNote ?: "Payment verification failed"}.",
                    type = "DEPOSIT_REJECTED"
                ))
            }
            "PENDING" -> {
                // Keep pending
            }
        }

        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Admin Withdrawal Management
    // -------------------------------------------------------------

    suspend fun reviewWithdrawal(
        withdrawalId: Long,
        newStatus: String, // "APPROVED", "PAID", "REJECTED", "PENDING"
        adminNote: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: return@withContext Result.failure(IllegalStateException("Withdrawal not found."))

        val user = userDao.getUserByIdSync(withdrawal.userId)
            ?: return@withContext Result.failure(IllegalStateException("User not found."))

        val updatedWithdrawal = withdrawal.copy(
            status = newStatus,
            processedAt = System.currentTimeMillis(),
            adminNotes = adminNote
        )
        withdrawalDao.updateWithdrawal(updatedWithdrawal)
        transactionDao.updateTransactionStatus(withdrawal.trxId, newStatus)

        when (newStatus) {
            "APPROVED" -> {
                notificationDao.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Withdrawal Approved! ✅",
                    message = "Your withdrawal of ${withdrawal.amount.toInt()} PKR via ${withdrawal.paymentMethod} has been approved and is being dispatched.",
                    type = "WITHDRAWAL_APPROVED"
                ))
            }
            "PAID" -> {
                notificationDao.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Withdrawal Paid! 💸",
                    message = "Your withdrawal of ${withdrawal.amount.toInt()} PKR has been sent to your ${withdrawal.paymentMethod} account (${withdrawal.accountNumber}).",
                    type = "WITHDRAWAL_PAID"
                ))
            }
            "REJECTED" -> {
                // Refund funds back to user's available balance!
                val refundedUser = user.copy(
                    availableBalance = user.availableBalance + withdrawal.amount
                )
                userDao.updateUser(refundedUser)

                // Add Refund transaction record
                val refundTrx = TransactionEntity(
                    trxId = generateTrxId("RF"),
                    userId = user.id,
                    userMobile = user.mobileNumber,
                    type = "REFUND",
                    title = "Withdrawal Refund",
                    amount = withdrawal.amount,
                    status = "COMPLETED",
                    notes = "Refund for rejected withdrawal: ${adminNote ?: "Invalid account details"}"
                )
                transactionDao.insertTransaction(refundTrx)

                notificationDao.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Withdrawal Rejected & Refunded 🔄",
                    message = "Withdrawal of ${withdrawal.amount.toInt()} PKR was rejected. The amount has been refunded to your balance. Note: ${adminNote ?: "Verification issue"}.",
                    type = "WITHDRAWAL_REJECTED"
                ))
            }
            "PENDING" -> {}
        }

        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Admin User Management & Balance Adjustment
    // -------------------------------------------------------------

    suspend fun toggleUserSuspension(userId: Long, suspend: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateSuspensionStatus(userId, suspend)
    }

    suspend fun manualAdjustBalance(userId: Long, delta: Double, reason: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext
        val newBalance = (user.availableBalance + delta).coerceAtLeast(0.0)
        val updatedUser = user.copy(
            availableBalance = newBalance,
            totalEarnings = if (delta > 0) user.totalEarnings + delta else user.totalEarnings
        )
        userDao.updateUser(updatedUser)

        val trx = TransactionEntity(
            trxId = generateTrxId("ADM"),
            userId = user.id,
            userMobile = user.mobileNumber,
            type = if (delta >= 0) "ADMIN_CREDIT" else "ADMIN_DEBIT",
            title = if (delta >= 0) "Admin Credit" else "Admin Deduction",
            amount = delta,
            status = "COMPLETED",
            notes = reason
        )
        transactionDao.insertTransaction(trx)

        notificationDao.insertNotification(NotificationEntity(
            userId = user.id,
            title = if (delta >= 0) "Balance Credited by Admin" else "Balance Adjusted by Admin",
            message = "${delta.toInt()} PKR adjusted in your balance. Reason: $reason",
            type = "SYSTEM"
        ))
    }

    suspend fun sendBroadcastNotification(title: String, message: String) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(NotificationEntity(
            userId = null, // broadcast to all
            title = title,
            message = message,
            type = "SYSTEM"
        ))
    }

    // -------------------------------------------------------------
    // User Queries
    // -------------------------------------------------------------

    fun getUserTransactions(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForUser(userId)

    fun getUserTransactionsByType(userId: Long, type: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForUserByType(userId, type)

    fun searchUserTransactions(userId: Long, query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactionsForUser(userId, query)

    fun getUserTotalApprovedDeposits(userId: Long): Flow<Double> =
        transactionDao.getUserTotalApprovedDeposits(userId)

    fun getUserTotalWithdrawals(userId: Long): Flow<Double> =
        transactionDao.getUserTotalWithdrawals(userId)

    fun getUserTotalAdRewards(userId: Long): Flow<Double> =
        transactionDao.getUserTotalAdRewards(userId)

    fun getUserAdsCompletedCount(userId: Long): Flow<Int> =
        transactionDao.getUserAdsCompletedCount(userId)

    fun getUserDepositsCount(userId: Long): Flow<Int> =
        transactionDao.getUserDepositsCount(userId)

    fun getUserWithdrawalsCount(userId: Long): Flow<Int> =
        transactionDao.getUserWithdrawalsCount(userId)

    fun getUserDeposits(userId: Long): Flow<List<DepositRequestEntity>> =
        depositDao.getDepositsForUser(userId)

    fun getUserWithdrawals(userId: Long): Flow<List<WithdrawalRequestEntity>> =
        withdrawalDao.getWithdrawalsForUser(userId)

    fun getUserNotifications(userId: Long): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    fun getUnreadNotificationCount(userId: Long): Flow<Int> =
        notificationDao.getUnreadCount(userId)

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun updatePassword(userId: Long, oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(IllegalStateException("User not found"))
        if (user.passwordHash != SessionManager.hashPassword(oldPass)) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect current password"))
        }
        val updated = user.copy(passwordHash = SessionManager.hashPassword(newPass))
        userDao.updateUser(updated)
        Result.success(Unit)
    }

    private fun generateTrxId(prefix: String): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomPart = UUID.randomUUID().toString().substring(0, 6).uppercase(Locale.US)
        return "$prefix-$dateStr-$randomPart"
    }
}
