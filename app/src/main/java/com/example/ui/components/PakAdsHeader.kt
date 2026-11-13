package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkSurfaceGlass
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite

@Composable
fun PakAdsHeader(
    title: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    balance: Double? = null,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceGlass)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Back button or Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkBorderHighlight.copy(alpha = 0.5f), CircleShape)
                            .testTag("header_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Brand Emblem + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = false) {}
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_pak_ads_logo),
                            contentDescription = "PAK ADS Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PAK",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ADS",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = NeonGreen,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(6.dp)
                                    .background(NeonCyan, CircleShape)
                            )
                        }
                        if (title != null) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                color = TextGrayLight,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Watch & Earn",
                                fontSize = 10.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right side: Balance chip + Notification bell + Admin shield
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Balance badge (if provided)
                if (balance != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF06331A),
                                        Color(0xFF092B3A)
                                    )
                                )
                            )
                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₨",
                                color = GoldYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.0f".format(balance),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Notifications icon
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(
                                containerColor = NeonGreen,
                                contentColor = Color.Black,
                                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkBorderHighlight.copy(alpha = 0.4f), CircleShape)
                            .testTag("header_notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadNotificationCount > 0) NeonGreen else TextGrayLight,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Discreet Admin Panel button
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
                        .testTag("admin_panel_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        tint = NeonCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PakAdsBrandEmblem(
    modifier: Modifier = Modifier,
    sizeDp: Int = 80
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Brush.linearGradient(listOf(NeonGreen, NeonCyan)), RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_pak_ads_logo),
                contentDescription = "PAK ADS Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(sizeDp.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "PAK",
                fontWeight = FontWeight.Black,
                fontSize = (sizeDp / 3.2).sp,
                color = TextWhite,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ADS",
                fontWeight = FontWeight.Black,
                fontSize = (sizeDp / 3.2).sp,
                color = NeonGreen,
                letterSpacing = 2.sp
            )
        }

        Text(
            text = "PREMIUM REWARD PLATFORM",
            fontSize = 10.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp
        )
    }
}
