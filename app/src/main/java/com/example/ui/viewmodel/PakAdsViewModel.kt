package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DepositRequestEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WithdrawalRequestEntity
import com.example.data.repository.PakAdsRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PakAdsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = PakAdsRepository(database)
    val sessionManager = SessionManager(application)

    private val _currentUserId = MutableStateFlow(sessionManager.getUserId())
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.observeUser(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appConfig: StateFlow<AppConfigEntity> = repository.configFlow
        .flatMapLatest { flowOf(it ?: AppConfigEntity()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppConfigEntity())

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserTransactions(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTotalApprovedDeposits: StateFlow<Double> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserTotalApprovedDeposits(id) else flowOf(0.0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val userTotalWithdrawals: StateFlow<Double> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserTotalWithdrawals(id) else flowOf(0.0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val userTotalAdRewards: StateFlow<Double> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserTotalAdRewards(id) else flowOf(0.0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val userAdsCompletedCount: StateFlow<Int> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserAdsCompletedCount(id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userDepositsCount: StateFlow<Int> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserDepositsCount(id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userWithdrawalsCount: StateFlow<Int> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserWithdrawalsCount(id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userDeposits: StateFlow<List<DepositRequestEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserDeposits(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserWithdrawals(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUserNotifications(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = _currentUserId
        .flatMapLatest { id ->
            if (id > 0) repository.getUnreadNotificationCount(id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Admin flows
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositRequestEntity>> = repository.allDepositsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = repository.allWithdrawalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    private val _activeAdModal = MutableStateFlow<Int?>(null) // null if closed, or adIndex
    val activeAdModal: StateFlow<Int?> = _activeAdModal.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(sessionManager.isAdminLoggedIn())
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAppConfig() // triggers default initialization if empty
            // Check ad reset on startup if user is logged in
            val userId = sessionManager.getUserId()
            if (userId > 0) {
                val user = repository.getUserSync(userId)
                if (user != null) {
                    repository.checkAndApplyAdReset(user)
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Authentication Actions
    // -------------------------------------------------------------

    fun register(
        mobile: String,
        pass: String,
        confirmPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (pass != confirmPass) {
            onError("Passwords do not match.")
            return
        }
        if (pass.length < 4) {
            onError("Password must be at least 4 characters.")
            return
        }

        viewModelScope.launch {
            val result = repository.registerUser(mobile, pass)
            result.fold(
                onSuccess = { newUserId ->
                    sessionManager.saveUserId(newUserId)
                    _currentUserId.value = newUserId
                    _uiMessage.emit("Welcome! 100 PKR Welcome Bonus added to your account!")
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Registration failed.")
                }
            )
        }
    }

    fun login(
        mobile: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginUser(mobile, pass)
            result.fold(
                onSuccess = { user ->
                    sessionManager.saveUserId(user.id)
                    _currentUserId.value = user.id
                    _uiMessage.emit("Logged in successfully.")
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Login failed.")
                }
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        sessionManager.clearSession()
        _currentUserId.value = -1L
        onLoggedOut()
    }

    // -------------------------------------------------------------
    // Watch & Earn Actions
    // -------------------------------------------------------------

    fun tryStartWatchingAd(
        onErrorLocked: (String) -> Unit
    ) {
        val user = currentUser.value
        val config = appConfig.value

        if (user == null) {
            onErrorLocked("Please login first.")
            return
        }

        // Rule 7: User must have APPROVED deposit before Watch & Earn becomes active!
        if (config.depositRequiredBeforeAds && user.depositStatus != "APPROVED") {
            onErrorLocked("Please make a deposit first. Your ad-watching feature will be activated after your deposit is approved by the Admin.")
            return
        }

        if (user.adsWatchedToday >= config.dailyAdsLimit) {
            onErrorLocked("You have completed all ${config.dailyAdsLimit} ads for today! Please wait for the 24-hour reset.")
            return
        }

        // Open ad dialog for the current ad
        _activeAdModal.value = user.adsWatchedToday
    }

    fun closeAdModal() {
        _activeAdModal.value = null
    }

    fun completeCurrentAd(onSuccess: (Double) -> Unit, onError: (String) -> Unit) {
        val userId = _currentUserId.value
        if (userId <= 0) return

        viewModelScope.launch {
            val result = repository.completeRewardedAd(userId)
            result.fold(
                onSuccess = { reward ->
                    _activeAdModal.value = null
                    _uiMessage.emit("Congratulations! ${reward.toInt()} PKR added to your balance.")
                    onSuccess(reward)
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to credit reward.")
                }
            )
        }
    }

    // -------------------------------------------------------------
    // Daily Bonus Actions
    // -------------------------------------------------------------

    fun claimDailyBonus(onSuccess: (Double) -> Unit, onError: (String) -> Unit) {
        val userId = _currentUserId.value
        if (userId <= 0) return

        viewModelScope.launch {
            val result = repository.claimDailyBonus(userId)
            result.fold(
                onSuccess = { bonus ->
                    _uiMessage.emit("Daily Bonus claimed: ${bonus.toInt()} PKR!")
                    onSuccess(bonus)
                },
                onFailure = { error ->
                    onError(error.message ?: "Could not claim daily bonus.")
                }
            )
        }
    }

    // -------------------------------------------------------------
    // Deposit Actions
    // -------------------------------------------------------------

    fun submitDeposit(
        amount: Double,
        senderName: String,
        senderMobile: String,
        trxReference: String,
        screenshotUri: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = _currentUserId.value
        if (userId <= 0) return

        viewModelScope.launch {
            val result = repository.submitDeposit(
                userId = userId,
                amount = amount,
                senderName = senderName,
                senderMobile = senderMobile,
                trxReference = trxReference,
                screenshotUri = screenshotUri
            )
            result.fold(
                onSuccess = {
                    _uiMessage.emit("Deposit request submitted successfully for verification.")
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Deposit submission failed.")
                }
            )
        }
    }

    // -------------------------------------------------------------
    // Withdrawal Actions
    // -------------------------------------------------------------

    fun submitWithdrawal(
        method: String,
        holderName: String,
        accountNumber: String,
        amount: Double,
        saveDetails: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = _currentUserId.value
        if (userId <= 0) return

        viewModelScope.launch {
            val result = repository.submitWithdrawal(
                userId = userId,
                method = method,
                holderName = holderName,
                accountNumber = accountNumber,
                amount = amount,
                saveDetails = saveDetails
            )
            result.fold(
                onSuccess = {
                    _uiMessage.emit("Withdrawal request placed successfully.")
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Withdrawal submission failed.")
                }
            )
        }
    }

    // -------------------------------------------------------------
    // Profile Actions
    // -------------------------------------------------------------

    fun updatePassword(oldPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = _currentUserId.value
        if (userId <= 0) return

        viewModelScope.launch {
            val result = repository.updatePassword(userId, oldPass, newPass)
            result.fold(
                onSuccess = {
                    _uiMessage.emit("Password updated successfully.")
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to update password.")
                }
            )
        }
    }

    fun markAllNotificationsRead() {
        val userId = _currentUserId.value
        if (userId > 0) {
            viewModelScope.launch {
                repository.markAllNotificationsAsRead(userId)
            }
        }
    }

    // -------------------------------------------------------------
    // Admin Actions
    // -------------------------------------------------------------

    fun loginAdmin(passcode: String): Boolean {
        val valid = SessionManager.verifyAdminPassword(passcode)
        if (valid) {
            sessionManager.setAdminLoggedIn(true)
            _isAdminLoggedIn.value = true
        }
        return valid
    }

    fun logoutAdmin() {
        sessionManager.clearAdminSession()
        _isAdminLoggedIn.value = false
    }

    fun adminReviewDeposit(depositId: Long, status: String, adminNotes: String? = null) {
        viewModelScope.launch {
            repository.reviewDeposit(depositId, status, adminNotes)
            _uiMessage.emit("Deposit status updated to $status.")
        }
    }

    fun adminReviewWithdrawal(withdrawalId: Long, status: String, adminNotes: String? = null) {
        viewModelScope.launch {
            repository.reviewWithdrawal(withdrawalId, status, adminNotes)
            _uiMessage.emit("Withdrawal status updated to $status.")
        }
    }

    fun adminToggleSuspension(userId: Long, suspend: Boolean) {
        viewModelScope.launch {
            repository.toggleUserSuspension(userId, suspend)
            _uiMessage.emit(if (suspend) "User suspended." else "User activated.")
        }
    }

    fun adminAdjustBalance(userId: Long, delta: Double, reason: String) {
        viewModelScope.launch {
            repository.manualAdjustBalance(userId, delta, reason)
            _uiMessage.emit("Balance adjusted by ${delta.toInt()} PKR.")
        }
    }

    fun adminUpdateConfig(config: AppConfigEntity) {
        viewModelScope.launch {
            repository.updateAppConfig(config)
            _uiMessage.emit("Platform settings updated successfully.")
        }
    }

    fun adminBroadcast(title: String, message: String) {
        viewModelScope.launch {
            repository.sendBroadcastNotification(title, message)
            _uiMessage.emit("Broadcast announcement sent.")
        }
    }
}
