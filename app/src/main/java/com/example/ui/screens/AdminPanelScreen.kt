package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DepositRequestEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WithdrawalRequestEntity
import com.example.ui.components.PakAdsBrandedBackground
import com.example.ui.components.PakAdsCard
import com.example.ui.components.PakAdsPrimaryButton
import com.example.ui.components.PakAdsStatCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EasypaisaGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.JazzCashRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminSection(val title: String) {
    STATISTICS("Statistics"),
    DEPOSITS("Deposits"),
    WITHDRAWALS("Withdrawals"),
    USERS("Users"),
    SETTINGS("Settings"),
    TRANSACTIONS("Transactions"),
    BROADCAST("Broadcast")
}

@Composable
fun AdminPanelScreen(
    users: List<UserEntity>,
    deposits: List<DepositRequestEntity>,
    withdrawals: List<WithdrawalRequestEntity>,
    transactions: List<TransactionEntity>,
    config: AppConfigEntity,
    onReviewDeposit: (depositId: Long, status: String, notes: String?) -> Unit,
    onReviewWithdrawal: (withdrawalId: Long, status: String, notes: String?) -> Unit,
    onToggleSuspend: (userId: Long, suspend: Boolean) -> Unit,
    onAdjustBalance: (userId: Long, delta: Double, reason: String) -> Unit,
    onUpdateConfig: (AppConfigEntity) -> Unit,
    onBroadcast: (title: String, message: String) -> Unit,
    onLogoutAdmin: () -> Unit
) {
    var currentSection by remember { mutableStateOf(AdminSection.STATISTICS) }
    var previewScreenshotDeposit by remember { mutableStateOf<DepositRequestEntity?>(null) }
    var adjustUserModal by remember { mutableStateOf<UserEntity?>(null) }

    // Statistics calculations
    val totalUsers = users.size
    val activeUsers = users.count { !it.isSuspended }
    val suspendedUsers = users.count { it.isSuspended }

    val totalDeposits = deposits.size
    val pendingDeposits = deposits.count { it.status == "PENDING" }
    val approvedDeposits = deposits.count { it.status == "APPROVED" }
    val totalDepositAmount = deposits.filter { it.status == "APPROVED" }.sumOf { it.amount }

    val totalWithdrawals = withdrawals.size
    val pendingWithdrawals = withdrawals.count { it.status == "PENDING" }
    val approvedWithdrawals = withdrawals.count { it.status == "APPROVED" || it.status == "PAID" }
    val totalWithdrawalAmount = withdrawals.filter { it.status == "APPROVED" || it.status == "PAID" }.sumOf { it.amount }

    val totalAdsWatched = users.sumOf { it.adsWatchedTotal }
    val totalAdEarnings = transactions.filter { it.type == "AD_REWARD" && it.status == "COMPLETED" }.sumOf { it.amount }

    PakAdsBrandedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Admin Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PANEL",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                            }
                            Text(
                                text = "Authorized Session Active",
                                fontSize = 10.sp,
                                color = NeonGreen
                            )
                        }
                    }

                    // Logout Admin button
                    IconButton(
                        onClick = onLogoutAdmin,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ErrorRed.copy(alpha = 0.15f), CircleShape)
                            .testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout Admin",
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Navigation Sections Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0E17))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AdminSection.values()) { section ->
                    val isSelected = section == currentSection
                    val badgeCount = when (section) {
                        AdminSection.DEPOSITS -> pendingDeposits
                        AdminSection.WITHDRAWALS -> pendingWithdrawals
                        else -> 0
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) NeonCyan else DarkSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else DarkBorderHighlight,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { currentSection = section }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("admin_tab_${section.name.lowercase()}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = section.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TextGrayLight
                            )

                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(if (isSelected) Color.Black else ErrorRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = badgeCount.toString(),
                                        color = if (isSelected) NeonCyan else TextWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Content Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                when (currentSection) {
                    AdminSection.STATISTICS -> {
                        // Statistics Dashboard
                        item {
                            Text(
                                text = "PLATFORM OVERVIEW & METRICS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PakAdsStatCard(
                                    title = "Total Users",
                                    value = totalUsers.toString(),
                                    subtitle = "$activeUsers Active • $suspendedUsers Suspended",
                                    accentColor = NeonCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                PakAdsStatCard(
                                    title = "Ads Watched",
                                    value = totalAdsWatched.toString(),
                                    subtitle = "${totalAdEarnings.toInt()} PKR Paid",
                                    accentColor = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PakAdsStatCard(
                                    title = "Deposits",
                                    value = "${totalDepositAmount.toInt()} PKR",
                                    subtitle = "$pendingDeposits Pending • $approvedDeposits Approved",
                                    accentColor = GoldYellow,
                                    modifier = Modifier.weight(1f)
                                )
                                PakAdsStatCard(
                                    title = "Withdrawals",
                                    value = "${totalWithdrawalAmount.toInt()} PKR",
                                    subtitle = "$pendingWithdrawals Pending • $approvedWithdrawals Approved",
                                    accentColor = Color(0xFFFF8A80),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            PakAdsCard {
                                Text(
                                    text = "System Financial Ledger",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Total Approved Deposits:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "+${totalDepositAmount.toInt()} PKR", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Total Completed Withdrawals:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "-${totalWithdrawalAmount.toInt()} PKR", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Total Ad Rewards Generated:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "${totalAdEarnings.toInt()} PKR", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    AdminSection.DEPOSITS -> {
                        // Deposit Management Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DEPOSIT MANAGEMENT ($pendingDeposits PENDING)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        if (deposits.isEmpty()) {
                            item {
                                PakAdsCard {
                                    Text(
                                        text = "No deposit requests on record.",
                                        color = TextGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else {
                            // Sort pending first
                            val sortedDeposits = deposits.sortedBy { if (it.status == "PENDING") 0 else 1 }
                            items(sortedDeposits, key = { it.id }) { dep ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when (dep.status) {
                                            "PENDING" -> WarningAmber
                                            "APPROVED" -> NeonGreen.copy(alpha = 0.5f)
                                            else -> ErrorRed.copy(alpha = 0.5f)
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${dep.amount.toInt()} PKR",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = TextWhite
                                                )
                                                Text(
                                                    text = "User: ${dep.userMobile} (ID #${dep.userId})",
                                                    fontSize = 11.sp,
                                                    color = NeonCyan
                                                )
                                            }

                                            // Status
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        when (dep.status) {
                                                            "APPROVED" -> NeonGreen.copy(alpha = 0.2f)
                                                            "PENDING" -> WarningAmber.copy(alpha = 0.2f)
                                                            else -> ErrorRed.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = dep.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (dep.status) {
                                                        "APPROVED" -> NeonGreen
                                                        "PENDING" -> WarningAmber
                                                        else -> ErrorRed
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(text = "Sender Name: ${dep.senderName}", fontSize = 12.sp, color = TextWhite)
                                        Text(text = "Sender Number: ${dep.senderMobile}", fontSize = 12.sp, color = TextWhite)
                                        Text(text = "Payment Method: ${dep.paymentMethod}", fontSize = 12.sp, color = NeonGreen)
                                        Text(text = "TRX ID: ${dep.trxId}", fontSize = 12.sp, color = GoldYellow)
                                        Text(
                                            text = "Submitted: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(dep.createdAt))}",
                                            fontSize = 10.sp,
                                            color = TextGray
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // View Screenshot button
                                        OutlinedButton(
                                            onClick = { previewScreenshotDeposit = dep },
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "VIEW PAYMENT SCREENSHOT / RECEIPT",
                                                fontSize = 11.sp,
                                                color = NeonCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Admin Controls: PENDING / APPROVED / REJECTED
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onReviewDeposit(dep.id, "APPROVED", null) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = NeonGreen,
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_approve_deposit_${dep.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("APPROVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onReviewDeposit(dep.id, "REJECTED", "Payment verification failed") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ErrorRed,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_reject_deposit_${dep.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onReviewDeposit(dep.id, "PENDING", null) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF334155),
                                                    contentColor = TextWhite
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("PENDING", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminSection.WITHDRAWALS -> {
                        // Withdrawal Management Section
                        item {
                            Text(
                                text = "WITHDRAWAL MANAGEMENT ($pendingWithdrawals PENDING)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        if (withdrawals.isEmpty()) {
                            item {
                                PakAdsCard {
                                    Text(
                                        text = "No withdrawal requests found.",
                                        color = TextGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else {
                            val sortedWithdrawals = withdrawals.sortedBy { if (it.status == "PENDING") 0 else 1 }
                            items(sortedWithdrawals, key = { it.id }) { with ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when (with.status) {
                                            "PAID" -> NeonGreen
                                            "APPROVED" -> NeonCyan
                                            "PENDING" -> WarningAmber
                                            else -> ErrorRed
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${with.amount.toInt()} PKR",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = TextWhite
                                                )
                                                Text(
                                                    text = "User Mobile: ${with.userMobile} (ID #${with.userId})",
                                                    fontSize = 11.sp,
                                                    color = NeonCyan
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(DarkSurfaceElevated)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = with.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (with.status) {
                                                        "PAID" -> NeonGreen
                                                        "APPROVED" -> NeonCyan
                                                        "PENDING" -> WarningAmber
                                                        else -> ErrorRed
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(text = "Payment Method: ${with.paymentMethod}", fontSize = 12.sp, color = if (with.paymentMethod == "JazzCash") JazzCashRed else EasypaisaGreen, fontWeight = FontWeight.Bold)
                                        Text(text = "Account Holder: ${with.accountHolderName}", fontSize = 12.sp, color = TextWhite)
                                        Text(text = "Account Number: ${with.accountNumber}", fontSize = 12.sp, color = TextWhite)
                                        Text(text = "TRX Reference: ${with.trxId}", fontSize = 11.sp, color = GoldYellow)
                                        Text(
                                            text = "Requested: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(with.createdAt))}",
                                            fontSize = 10.sp,
                                            color = TextGray
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Admin Controls: PAID / APPROVED / REJECTED / PENDING
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = { onReviewWithdrawal(with.id, "PAID", null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("admin_paid_withdrawal_${with.id}")
                                            ) {
                                                Text("PAID", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                            }

                                            Button(
                                                onClick = { onReviewWithdrawal(with.id, "APPROVED", null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Text("APPROVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onReviewWithdrawal(with.id, "REJECTED", "Details verification failed") },
                                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("admin_reject_withdrawal_${with.id}")
                                            ) {
                                                Text("REJECT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onReviewWithdrawal(with.id, "PENDING", null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = TextWhite),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Text("PENDING", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminSection.USERS -> {
                        // User Management Section
                        item {
                            Text(
                                text = "USER MANAGEMENT ($totalUsers USERS REGISTERED)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        items(users, key = { it.id }) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = u.mobileNumber,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "ID #${u.id} • Balance: ${u.availableBalance.toInt()} PKR",
                                                fontSize = 12.sp,
                                                color = NeonGreen
                                            )
                                        }

                                        // Status Pill
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (u.isSuspended) ErrorRed.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (u.isSuspended) "SUSPENDED" else "ACTIVE",
                                                color = if (u.isSuspended) ErrorRed else NeonGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Deposit Status: ${u.depositStatus} • Lifetime Ads Watched: ${u.adsWatchedTotal}",
                                        fontSize = 11.sp,
                                        color = TextGrayLight
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onToggleSuspend(u.id, !u.isSuspended) },
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (u.isSuspended) NeonGreen else ErrorRed
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = if (u.isSuspended) NeonGreen else ErrorRed
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (u.isSuspended) "UNSUSPEND" else "SUSPEND",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = { adjustUserModal = u },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = NeonCyan,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("ADJUST BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdminSection.SETTINGS -> {
                        // Platform Configuration Editor
                        item {
                            Text(
                                text = "ADMIN CONFIGURATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            var dailyAds by remember { mutableStateOf(config.dailyAdsLimit.toString()) }
                            var rewardPerAd by remember { mutableStateOf(config.rewardPerAd.toInt().toString()) }
                            var minDeposit by remember { mutableStateOf(config.minDeposit.toInt().toString()) }
                            var minWithdrawal by remember { mutableStateOf(config.minWithdrawal.toInt().toString()) }
                            var welcomeBonus by remember { mutableStateOf(config.welcomeBonus.toInt().toString()) }
                            var dailyBonus by remember { mutableStateOf(config.dailyBonus.toInt().toString()) }
                            var depositReq by remember { mutableStateOf(config.depositRequiredBeforeAds) }
                            var epAccountName by remember { mutableStateOf(config.depositAccountName) }
                            var epAccountNumber by remember { mutableStateOf(config.depositAccountNumber) }

                            PakAdsCard {
                                Text(
                                    text = "Watch & Earn Settings",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = dailyAds,
                                    onValueChange = { dailyAds = it },
                                    label = { Text("Daily Ads Limit (Default: 5)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = rewardPerAd,
                                    onValueChange = { rewardPerAd = it },
                                    label = { Text("Reward Per Ad PKR (Default: 50)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Financial Thresholds",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = minDeposit,
                                    onValueChange = { minDeposit = it },
                                    label = { Text("Minimum Deposit PKR (Default: 300)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = minWithdrawal,
                                    onValueChange = { minWithdrawal = it },
                                    label = { Text("Minimum Withdrawal PKR (Default: 800)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = welcomeBonus,
                                    onValueChange = { welcomeBonus = it },
                                    label = { Text("Welcome Bonus PKR (Default: 100)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = dailyBonus,
                                    onValueChange = { dailyBonus = it },
                                    label = { Text("Daily Bonus PKR (Default: 25)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Require Deposit Before Ads",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "Users must have an approved deposit to watch ads",
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )
                                    }

                                    Switch(
                                        checked = depositReq,
                                        onCheckedChange = { depositReq = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Easypaisa Receiving Account",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = epAccountName,
                                    onValueChange = { epAccountName = it },
                                    label = { Text("Receiving Account Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = epAccountNumber,
                                    onValueChange = { epAccountNumber = it },
                                    label = { Text("Receiving Account Number") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                PakAdsPrimaryButton(
                                    text = "SAVE PLATFORM CONFIGURATION",
                                    onClick = {
                                        val updated = config.copy(
                                            dailyAdsLimit = dailyAds.toIntOrNull() ?: 5,
                                            rewardPerAd = rewardPerAd.toDoubleOrNull() ?: 50.0,
                                            minDeposit = minDeposit.toDoubleOrNull() ?: 300.0,
                                            minWithdrawal = minWithdrawal.toDoubleOrNull() ?: 800.0,
                                            welcomeBonus = welcomeBonus.toDoubleOrNull() ?: 100.0,
                                            dailyBonus = dailyBonus.toDoubleOrNull() ?: 25.0,
                                            depositRequiredBeforeAds = depositReq,
                                            depositAccountName = epAccountName.trim(),
                                            depositAccountNumber = epAccountNumber.trim()
                                        )
                                        onUpdateConfig(updated)
                                    },
                                    icon = Icons.Default.Save,
                                    testTag = "admin_save_settings_button"
                                )
                            }
                        }
                    }

                    AdminSection.TRANSACTIONS -> {
                        // All Transactions Ledger
                        item {
                            Text(
                                text = "GLOBAL AUDIT LEDGER (${transactions.size} ENTRIES)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        items(transactions, key = { it.id }) { trx ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = trx.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text(text = "User: ${trx.userMobile} • TRX: ${trx.trxId}", fontSize = 11.sp, color = GoldYellow)
                                        Text(
                                            text = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date(trx.timestamp)),
                                            fontSize = 10.sp,
                                            color = TextGray
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${trx.amount.toInt()} PKR",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (trx.amount >= 0) NeonGreen else ErrorRed
                                        )
                                        Text(
                                            text = trx.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextGrayLight
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AdminSection.BROADCAST -> {
                        // Broadcast Notification Section
                        item {
                            Text(
                                text = "SEND BROADCAST ANNOUNCEMENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            var broadcastTitle by remember { mutableStateOf("") }
                            var broadcastMessage by remember { mutableStateOf("") }

                            PakAdsCard {
                                OutlinedTextField(
                                    value = broadcastTitle,
                                    onValueChange = { broadcastTitle = it },
                                    label = { Text("Announcement Title") },
                                    placeholder = { Text("e.g. Server Maintenance Notice") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = broadcastMessage,
                                    onValueChange = { broadcastMessage = it },
                                    label = { Text("Announcement Message") },
                                    placeholder = { Text("Message will be broadcast to all users...") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                PakAdsPrimaryButton(
                                    text = "SEND BROADCAST TO ALL USERS",
                                    onClick = {
                                        if (broadcastTitle.trim().isNotEmpty() && broadcastMessage.trim().isNotEmpty()) {
                                            onBroadcast(broadcastTitle.trim(), broadcastMessage.trim())
                                            broadcastTitle = ""
                                            broadcastMessage = ""
                                        }
                                    },
                                    icon = Icons.Default.Campaign,
                                    testTag = "admin_send_broadcast_button"
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal to View Full Payment Screenshot
    if (previewScreenshotDeposit != null) {
        val dep = previewScreenshotDeposit!!
        Dialog(onDismissRequest = { previewScreenshotDeposit = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EasypaisaGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payment Receipt Verification",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        IconButton(onClick = { previewScreenshotDeposit = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Realistic Easypaisa Receipt Card Graphic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF032612))
                            .border(1.5.dp, EasypaisaGreen, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(EasypaisaGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Transaction Successful",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )

                            Text(
                                text = "Easypaisa Money Transfer",
                                fontSize = 11.sp,
                                color = TextGrayLight
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${dep.amount.toInt()} PKR",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(DarkBorderHighlight)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Sent To:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "SAHANA SAHANA", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Receiver Number:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "03174254463", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Sender Name:", color = TextGray, fontSize = 12.sp)
                                    Text(text = dep.senderName, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Sender Mobile:", color = TextGray, fontSize = 12.sp)
                                    Text(text = dep.senderMobile, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Transaction ID:", color = TextGray, fontSize = 12.sp)
                                    Text(text = dep.trxId, color = GoldYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Date & Time:", color = TextGray, fontSize = 12.sp)
                                    Text(text = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.US).format(Date(dep.createdAt)), color = TextWhite, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onReviewDeposit(dep.id, "APPROVED", null)
                                previewScreenshotDeposit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("APPROVE NOW", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onReviewDeposit(dep.id, "REJECTED", "Screenshot invalid or unverified")
                                previewScreenshotDeposit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal to manually adjust balance for a user
    if (adjustUserModal != null) {
        val targetUser = adjustUserModal!!
        var adjustAmount by remember { mutableStateOf("100") }
        var isCredit by remember { mutableStateOf(true) }
        var reason by remember { mutableStateOf("Admin adjustment") }

        AlertDialog(
            onDismissRequest = { adjustUserModal = null },
            title = {
                Text(
                    text = "Adjust Balance for ${targetUser.mobileNumber}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current Available Balance: ${targetUser.availableBalance.toInt()} PKR",
                        color = NeonGreen,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isCredit = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCredit) NeonGreen else DarkSurfaceBorder,
                                contentColor = if (isCredit) Color.Black else TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CREDIT (+)")
                        }

                        Button(
                            onClick = { isCredit = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isCredit) ErrorRed else DarkSurfaceBorder,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DEBIT (-)")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adjustAmount,
                        onValueChange = { adjustAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Amount PKR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason / Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = adjustAmount.toDoubleOrNull() ?: 0.0
                        val delta = if (isCredit) amount else -amount
                        onAdjustBalance(targetUser.id, delta, reason)
                        adjustUserModal = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCredit) NeonGreen else ErrorRed,
                        contentColor = if (isCredit) Color.Black else Color.White
                    )
                ) {
                    Text("Apply Adjustment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustUserModal = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
