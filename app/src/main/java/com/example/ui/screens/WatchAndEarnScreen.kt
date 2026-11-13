package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.UserEntity
import com.example.ui.components.DepositStatusBadge
import com.example.ui.components.LiveCountdownTimer
import com.example.ui.components.PakAdsCard
import com.example.ui.components.PakAdsPrimaryButton
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber

@Composable
fun WatchAndEarnScreen(
    user: UserEntity?,
    config: AppConfigEntity,
    onStartAd: () -> Unit,
    onNavigateDeposit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val depositApproved = user?.depositStatus == "APPROVED"
    val adsCompleted = user?.adsWatchedToday ?: 0
    val totalLimit = config.dailyAdsLimit
    val rewardPerAd = config.rewardPerAd
    val maxDaily = config.maxDailyAdEarnings

    val lastReset = user?.lastAdResetTime ?: System.currentTimeMillis()
    val nextResetEpoch = lastReset + (config.adResetPeriodHours * 3600 * 1000L)

    var showDepositRequirementDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Header Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WATCH & EARN SYSTEM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        DepositStatusBadge(status = user?.depositStatus ?: "NONE")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5 Daily Rewarded Ads",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Watch verified partner advertisements to completion to receive direct balance credits in PKR.",
                        fontSize = 12.sp,
                        color = TextGrayLight,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calculation breakdown: 5 ads x 50 PKR = 250 PKR maximum daily ad earnings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Ads Completed", fontSize = 10.sp, color = TextGray)
                            Text(
                                text = "$adsCompleted / $totalLimit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Column {
                            Text(text = "Reward Per Ad", fontSize = 10.sp, color = TextGray)
                            Text(
                                text = "${rewardPerAd.toInt()} PKR",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }

                        Column {
                            Text(text = "Today's Maximum", fontSize = 10.sp, color = TextGray)
                            Text(
                                text = "${maxDaily.toInt()} PKR",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Next 24-Hour Reset Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Reset Countdown:",
                            fontSize = 11.sp,
                            color = TextGrayLight
                        )
                        LiveCountdownTimer(targetEpochMillis = nextResetEpoch)
                    }
                }
            }
        }

        // Deposit Lock Notice (if deposit requirement is not met)
        if (config.depositRequiredBeforeAds && !depositApproved) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231805)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningAmber)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Deposit Required for Activation",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please make a deposit first. Your ad-watching feature will be activated after your deposit is approved by the Admin.",
                            fontSize = 12.sp,
                            color = TextGrayLight,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateDeposit,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningAmber,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("watch_ads_deposit_required_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DEPOSIT NOW (MIN ${config.minDeposit.toInt()} PKR)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 5 Daily Ads List
        item {
            Text(
                text = "AVAILABLE DAILY ADVERTISEMENTS",
                fontSize = 12.sp,
                color = TextGrayLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(totalLimit) { index ->
            val adNumber = index + 1
            val isCompleted = index < adsCompleted
            val isCurrentAd = index == adsCompleted && depositApproved
            val isLocked = index > adsCompleted || !depositApproved

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isCurrentAd) {
                            Modifier.clickable { onStartAd() }
                        } else if (!depositApproved) {
                            Modifier.clickable { showDepositRequirementDialog = true }
                        } else Modifier
                    )
                    .testTag("ad_card_$adNumber"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCompleted -> Color(0xFF0B1F13)
                        isCurrentAd -> Color(0xFF0B2433)
                        else -> DarkSurfaceElevated
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        isCompleted -> NeonGreen.copy(alpha = 0.5f)
                        isCurrentAd -> NeonCyan
                        else -> DarkSurfaceBorder
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Status Icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> NeonGreen.copy(alpha = 0.2f)
                                        isCurrentAd -> NeonCyan.copy(alpha = 0.2f)
                                        else -> Color(0xFF1E293B)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isCompleted -> Icons.Default.Check
                                    isCurrentAd -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Lock
                                },
                                contentDescription = null,
                                tint = when {
                                    isCompleted -> NeonGreen
                                    isCurrentAd -> NeonCyan
                                    else -> TextGray
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rewarded Ad #$adNumber",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted || isCurrentAd) TextWhite else TextGray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• 15s",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Reward: +${rewardPerAd.toInt()} PKR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) NeonGreen else if (isCurrentAd) NeonCyan else TextGray
                            )
                        }
                    }

                    // Right Button / Pill
                    when {
                        isCompleted -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "DONE",
                                        color = NeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        isCurrentAd -> {
                            Button(
                                onClick = onStartAd,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("watch_ad_button_$adNumber")
                            ) {
                                Text(
                                    text = "WATCH NOW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TextGray,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LOCKED",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal when user clicks locked ad without approved deposit
    if (showDepositRequirementDialog) {
        AlertDialog(
            onDismissRequest = { showDepositRequirementDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Deposit Required",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Please make a deposit first. Your ad-watching feature will be activated after your deposit is approved by the Admin.",
                    color = TextGrayLight,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDepositRequirementDialog = false
                        onNavigateDeposit()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Go to Deposit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositRequirementDialog = false }) {
                    Text("Close", color = TextGray)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
