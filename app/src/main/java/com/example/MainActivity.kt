package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.PakAdsBottomNav
import com.example.ui.components.PakAdsHeader
import com.example.ui.components.PakAdsTab
import com.example.ui.components.RewardedAdSimulatorDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.DailyBonusScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.WatchAndEarnScreen
import com.example.ui.screens.WithdrawalScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PakAdsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppDestination {
    SPLASH,
    LOGIN,
    REGISTER,
    MAIN_TABS,
    DAILY_BONUS,
    NOTIFICATIONS,
    ADMIN_PANEL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PakAdsMainApp()
            }
        }
    }
}

@Composable
fun PakAdsMainApp(
    viewModel: PakAdsViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val userDeposits by viewModel.userDeposits.collectAsStateWithLifecycle()
    val userWithdrawals by viewModel.userWithdrawals.collectAsStateWithLifecycle()
    val userTotalApprovedDeposits by viewModel.userTotalApprovedDeposits.collectAsStateWithLifecycle()
    val userTotalWithdrawals by viewModel.userTotalWithdrawals.collectAsStateWithLifecycle()
    val userTotalAdRewards by viewModel.userTotalAdRewards.collectAsStateWithLifecycle()
    val userAdsCompletedCount by viewModel.userAdsCompletedCount.collectAsStateWithLifecycle()
    val userDepositsCount by viewModel.userDepositsCount.collectAsStateWithLifecycle()
    val userWithdrawalsCount by viewModel.userWithdrawalsCount.collectAsStateWithLifecycle()
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val activeAdModal by viewModel.activeAdModal.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()

    // Admin lists
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allDeposits by viewModel.allDeposits.collectAsStateWithLifecycle()
    val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
    var currentTab by remember { mutableStateOf(PakAdsTab.HOME) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }

    // Listen to UI messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle Hardware Back Button
    BackHandler(
        enabled = currentDestination != AppDestination.SPLASH && currentDestination != AppDestination.LOGIN
    ) {
        when (currentDestination) {
            AppDestination.REGISTER -> currentDestination = AppDestination.LOGIN
            AppDestination.ADMIN_PANEL -> {
                currentDestination = if (currentUserId > 0) AppDestination.MAIN_TABS else AppDestination.LOGIN
            }
            AppDestination.DAILY_BONUS, AppDestination.NOTIFICATIONS -> {
                currentDestination = AppDestination.MAIN_TABS
            }
            AppDestination.MAIN_TABS -> {
                if (currentTab != PakAdsTab.HOME) {
                    currentTab = PakAdsTab.HOME
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (currentDestination) {
            AppDestination.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentDestination = if (currentUserId > 0) {
                            AppDestination.MAIN_TABS
                        } else {
                            AppDestination.LOGIN
                        }
                    }
                )
            }

            AppDestination.LOGIN -> {
                LoginScreen(
                    onLoginClick = { mobile, pass ->
                        viewModel.login(
                            mobile = mobile,
                            pass = pass,
                            onSuccess = {
                                currentDestination = AppDestination.MAIN_TABS
                                currentTab = PakAdsTab.HOME
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onNavigateToRegister = {
                        currentDestination = AppDestination.REGISTER
                    },
                    onAdminClick = {
                        showAdminLoginDialog = true
                    }
                )
            }

            AppDestination.REGISTER -> {
                RegisterScreen(
                    onRegisterClick = { mobile, pass, confirmPass ->
                        viewModel.register(
                            mobile = mobile,
                            pass = pass,
                            confirmPass = confirmPass,
                            onSuccess = {
                                currentDestination = AppDestination.MAIN_TABS
                                currentTab = PakAdsTab.HOME
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onNavigateToLogin = {
                        currentDestination = AppDestination.LOGIN
                    }
                )
            }

            AppDestination.ADMIN_PANEL -> {
                AdminPanelScreen(
                    users = allUsers,
                    deposits = allDeposits,
                    withdrawals = allWithdrawals,
                    transactions = allTransactions,
                    config = appConfig,
                    onReviewDeposit = { id, status, notes ->
                        viewModel.adminReviewDeposit(id, status, notes)
                    },
                    onReviewWithdrawal = { id, status, notes ->
                        viewModel.adminReviewWithdrawal(id, status, notes)
                    },
                    onToggleSuspend = { userId, suspend ->
                        viewModel.adminToggleSuspension(userId, suspend)
                    },
                    onAdjustBalance = { userId, delta, reason ->
                        viewModel.adminAdjustBalance(userId, delta, reason)
                    },
                    onUpdateConfig = { updated ->
                        viewModel.adminUpdateConfig(updated)
                    },
                    onBroadcast = { title, msg ->
                        viewModel.adminBroadcast(title, msg)
                    },
                    onLogoutAdmin = {
                        viewModel.logoutAdmin()
                        currentDestination = if (currentUserId > 0) AppDestination.MAIN_TABS else AppDestination.LOGIN
                    }
                )
            }

            AppDestination.DAILY_BONUS -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    PakAdsHeader(
                        title = "Daily Bonus",
                        showBackButton = true,
                        onBackClick = { currentDestination = AppDestination.MAIN_TABS },
                        balance = currentUser?.availableBalance,
                        unreadNotificationCount = unreadNotificationsCount,
                        onNotificationClick = { currentDestination = AppDestination.NOTIFICATIONS },
                        onAdminClick = { showAdminLoginDialog = true }
                    )

                    DailyBonusScreen(
                        user = currentUser,
                        config = appConfig,
                        onClaimClick = {
                            viewModel.claimDailyBonus(
                                onSuccess = { reward ->
                                    Toast.makeText(context, "Claimed ${reward.toInt()} PKR bonus!", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }
            }

            AppDestination.NOTIFICATIONS -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    PakAdsHeader(
                        title = "Alerts & Updates",
                        showBackButton = true,
                        onBackClick = { currentDestination = AppDestination.MAIN_TABS },
                        balance = currentUser?.availableBalance,
                        unreadNotificationCount = 0,
                        onNotificationClick = {},
                        onAdminClick = { showAdminLoginDialog = true }
                    )

                    NotificationsScreen(
                        notifications = userNotifications,
                        onMarkAllRead = {
                            viewModel.markAllNotificationsRead()
                        }
                    )
                }
            }

            AppDestination.MAIN_TABS -> {
                Scaffold(
                    topBar = {
                        val headerTitle = when (currentTab) {
                            PakAdsTab.HOME -> null
                            PakAdsTab.WATCH -> "Watch & Earn"
                            PakAdsTab.DEPOSIT -> "Deposit Funds"
                            PakAdsTab.WITHDRAW -> "Withdraw Earnings"
                            PakAdsTab.HISTORY -> "Transaction History"
                            PakAdsTab.PROFILE -> "My Profile"
                        }

                        PakAdsHeader(
                            title = headerTitle,
                            showBackButton = currentTab != PakAdsTab.HOME,
                            onBackClick = { currentTab = PakAdsTab.HOME },
                            balance = currentUser?.availableBalance,
                            unreadNotificationCount = unreadNotificationsCount,
                            onNotificationClick = { currentDestination = AppDestination.NOTIFICATIONS },
                            onAdminClick = { showAdminLoginDialog = true }
                        )
                    },
                    bottomBar = {
                        PakAdsBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { tab -> currentTab = tab }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    containerColor = DarkBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            PakAdsTab.HOME -> {
                                DashboardScreen(
                                    user = currentUser,
                                    config = appConfig,
                                    onNavigateWatchAds = { currentTab = PakAdsTab.WATCH },
                                    onNavigateDailyBonus = { currentDestination = AppDestination.DAILY_BONUS },
                                    onNavigateDeposit = { currentTab = PakAdsTab.DEPOSIT },
                                    onNavigateWithdraw = { currentTab = PakAdsTab.WITHDRAW },
                                    onNavigateHistory = { currentTab = PakAdsTab.HISTORY },
                                    onNavigateProfile = { currentTab = PakAdsTab.PROFILE }
                                )
                            }

                            PakAdsTab.WATCH -> {
                                WatchAndEarnScreen(
                                    user = currentUser,
                                    config = appConfig,
                                    onStartAd = {
                                        viewModel.tryStartWatchingAd(
                                            onErrorLocked = { reason ->
                                                Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    onNavigateDeposit = { currentTab = PakAdsTab.DEPOSIT }
                                )
                            }

                            PakAdsTab.DEPOSIT -> {
                                DepositScreen(
                                    config = appConfig,
                                    depositHistory = userDeposits,
                                    onSubmitDeposit = { amount, sName, sMobile, trx, screenshot ->
                                        viewModel.submitDeposit(
                                            amount = amount,
                                            senderName = sName,
                                            senderMobile = sMobile,
                                            trxReference = trx,
                                            screenshotUri = screenshot,
                                            onSuccess = {
                                                Toast.makeText(context, "Deposit submitted for review!", Toast.LENGTH_LONG).show()
                                                currentTab = PakAdsTab.HOME
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                )
                            }

                            PakAdsTab.WITHDRAW -> {
                                WithdrawalScreen(
                                    user = currentUser,
                                    config = appConfig,
                                    withdrawHistory = userWithdrawals,
                                    onSubmitWithdrawal = { method: String, hName: String, aNumber: String, amount: Double, save: Boolean ->
                                        viewModel.submitWithdrawal(
                                            method = method,
                                            holderName = hName,
                                            accountNumber = aNumber,
                                            amount = amount,
                                            saveDetails = save,
                                            onSuccess = {
                                                Toast.makeText(context, "Withdrawal request submitted!", Toast.LENGTH_LONG).show()
                                                currentTab = PakAdsTab.HOME
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                )
                            }

                            PakAdsTab.HISTORY -> {
                                TransactionHistoryScreen(
                                    transactions = userTransactions,
                                    totalAdRewards = userTotalAdRewards,
                                    totalApprovedDeposits = userTotalApprovedDeposits,
                                    totalWithdrawals = userTotalWithdrawals,
                                    adsCompletedCount = userAdsCompletedCount,
                                    depositsCount = userDepositsCount,
                                    withdrawalsCount = userWithdrawalsCount,
                                    onNavigateToWatchAds = {
                                        currentTab = PakAdsTab.WATCH
                                    },
                                    onNavigateToDeposit = {
                                        currentTab = PakAdsTab.DEPOSIT
                                    },
                                    onNavigateToWithdraw = {
                                        currentTab = PakAdsTab.WITHDRAW
                                    }
                                )
                            }

                            PakAdsTab.PROFILE -> {
                                ProfileScreen(
                                    user = currentUser,
                                    onUpdatePassword = { oldPass, newPass ->
                                        viewModel.updatePassword(
                                            oldPass = oldPass,
                                            newPass = newPass,
                                            onSuccess = {
                                                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    onLogout = {
                                        viewModel.logout {
                                            currentDestination = AppDestination.LOGIN
                                        }
                                    },
                                    onOpenAdmin = {
                                        showAdminLoginDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Rewarded Ad Simulator Modal
        if (activeAdModal != null) {
            val adIdx = activeAdModal ?: 0
            RewardedAdSimulatorDialog(
                adIndex = adIdx,
                rewardAmount = appConfig.rewardPerAd,
                onRewardEarned = {
                    viewModel.completeCurrentAd(
                        onSuccess = { reward ->
                            Toast.makeText(context, "+${reward.toInt()} PKR Credited to Balance!", Toast.LENGTH_LONG).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onDismiss = {
                    viewModel.closeAdModal()
                }
            )
        }

        // Admin Security Passcode Dialog
        if (showAdminLoginDialog) {
            AdminLoginDialog(
                onDismiss = { showAdminLoginDialog = false },
                onSuccess = {
                    showAdminLoginDialog = false
                    viewModel.loginAdmin("085213")
                    currentDestination = AppDestination.ADMIN_PANEL
                }
            )
        }
    }
}
