package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

data class AdCampaign(
    val brand: String,
    val headline: String,
    val description: String,
    val callToAction: String,
    val accentColor: Color,
    val tag: String
)

val sampleAdCampaigns = listOf(
    AdCampaign(
        brand = "Daraz Online Shopping",
        headline = "Mega Savings Week - 75% Off",
        description = "Discover 10 million products with 100% genuine warranty and Free Nationwide Delivery on thousands of top items.",
        callToAction = "Install App & Shop Now",
        accentColor = Color(0xFFFF6600),
        tag = "E-COMMERCE"
    ),
    AdCampaign(
        brand = "Foodpanda Pakistan",
        headline = "Craving Delicious Food? 50% Off",
        description = "Order hot meals, groceries from Pandamart, and essentials delivered in 20 minutes to your doorstep.",
        callToAction = "Order with Code FOODPAK",
        accentColor = Color(0xFFD70F64),
        tag = "FOOD & GROCERY"
    ),
    AdCampaign(
        brand = "Easypaisa Digital Payments",
        headline = "Scan QR & Get 10% CashBack",
        description = "Send money with zero fees, pay utility bills, mobile loads and earn daily profits with Digital Savings.",
        callToAction = "Open Easypaisa Account",
        accentColor = Color(0xFF00B14F),
        tag = "FINTECH"
    ),
    AdCampaign(
        brand = "Zong 4G Digital",
        headline = "100 GB Super Weekly Mega Offer",
        description = "Experience Pakistan's fastest 4G data network. Unlimited social packs, YouTube streaming, and super gaming latency.",
        callToAction = "Subscribe via *6464#",
        accentColor = Color(0xFF88C057),
        tag = "TELECOM"
    ),
    AdCampaign(
        brand = "Bykea Super App",
        headline = "Affordable Bike Rides & Express Courier",
        description = "Save travel costs in Karachi, Lahore, and Rawalpindi. Fast motorcycle rides and parcel dispatch in minutes.",
        callToAction = "Book Your First Ride",
        accentColor = Color(0xFF00C853),
        tag = "MOBILITY"
    )
)

@Composable
fun RewardedAdSimulatorDialog(
    adIndex: Int,
    rewardAmount: Double,
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit
) {
    val campaign = remember(adIndex) {
        sampleAdCampaigns[(adIndex.coerceAtLeast(0)) % sampleAdCampaigns.size]
    }

    val totalDurationSeconds = 15
    var secondsRemaining by remember { mutableIntStateOf(totalDurationSeconds) }
    var isCompleted by remember { mutableStateOf(false) }
    var showExitWarning by remember { mutableStateOf(false) }
    var isClaimed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        isCompleted = true
    }

    val progress = (totalDurationSeconds - secondsRemaining).toFloat() / totalDurationSeconds.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "ad_progress"
    )

    Dialog(
        onDismissRequest = {
            if (!isCompleted) {
                showExitWarning = true
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: Timer, Verified Ad label, Close/Skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reward Indicator chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0E2E1A))
                            .border(1.dp, NeonGreen, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reward: ${rewardAmount.toInt()} PKR",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Countdown Timer / Close button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkSurfaceElevated, CircleShape)
                                    .border(1.dp, DarkBorderHighlight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${secondsRemaining}s",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { showExitWarning = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkSurfaceElevated, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close ad",
                                    tint = TextGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NeonGreen.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, NeonGreen, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close ad",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonGreen,
                    trackColor = DarkSurfaceElevated,
                )

                // Main Ad Content Stage
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCompleted) NeonGreen else campaign.accentColor.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Tag chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(campaign.accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = campaign.tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = campaign.accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Animated Media Screen Mockup
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF0F172A),
                                            campaign.accentColor.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                                .border(1.dp, DarkBorderHighlight, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(campaign.accentColor.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.Verified else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isCompleted) NeonGreen else campaign.accentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isCompleted) "Video Finished" else "Playing Advertisement...",
                                    color = TextGrayLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = campaign.brand,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = campaign.headline,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = campaign.description,
                            fontSize = 13.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Bottom Action: Claim Reward or Watch Progress
                if (!isCompleted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.size(24.dp),
                                    color = NeonGreen,
                                    trackColor = DarkBorderHighlight,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Please watch until timer ends",
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Reward will be credited after completion",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = "${secondsRemaining}s",
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // Completed! Claim Reward Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = GoldYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ad Complete! Verified by PAK ADS",
                                color = NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (!isClaimed) {
                                    isClaimed = true
                                    onRewardEarned()
                                }
                            },
                            enabled = !isClaimed,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("claim_ad_reward_button")
                        ) {
                            Text(
                                text = if (isClaimed) "REWARD CREDITED ✓" else "CLAIM ${rewardAmount.toInt()} PKR REWARD 💰",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Exit confirmation warning dialog
    if (showExitWarning) {
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            title = {
                Text(
                    text = "Close Advertisement?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "If you close now before the remaining ${secondsRemaining}s, you will NOT receive the ${rewardAmount.toInt()} PKR reward.",
                    color = TextGrayLight,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showExitWarning = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                ) {
                    Text("Resume Ad", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitWarning = false
                        onDismiss()
                    }
                ) {
                    Text("Exit without Reward", color = ErrorRed)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
