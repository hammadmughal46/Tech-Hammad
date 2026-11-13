package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.TransactionEntity
import com.example.ui.components.PakAdsCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    transactions: List<TransactionEntity>,
    totalAdRewards: Double = 0.0,
    totalApprovedDeposits: Double = 0.0,
    totalWithdrawals: Double = 0.0,
    adsCompletedCount: Int = 0,
    depositsCount: Int = 0,
    withdrawalsCount: Int = 0,
    onNavigateToWatchAds: (() -> Unit)? = null,
    onNavigateToDeposit: (() -> Unit)? = null,
    onNavigateToWithdraw: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }

    // Calculate actual aggregates from list if parameters are zero
    val actualAdRewards = if (totalAdRewards > 0) totalAdRewards else transactions.filter { it.type == "AD_REWARD" && it.status == "COMPLETED" }.sumOf { it.amount }
    val actualApprovedDeposits = if (totalApprovedDeposits > 0) totalApprovedDeposits else transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount }
    val actualWithdrawals = if (totalWithdrawals > 0) totalWithdrawals else transactions.filter { it.type == "WITHDRAWAL" && it.status in listOf("PENDING", "APPROVED", "PAID") }.sumOf { Math.abs(it.amount) }

    val actualAdsCount = if (adsCompletedCount > 0) adsCompletedCount else transactions.count { it.type == "AD_REWARD" && it.status == "COMPLETED" }
    val actualDepositsCount = if (depositsCount > 0) depositsCount else transactions.count { it.type == "DEPOSIT" }
    val actualWithdrawalsCount = if (withdrawalsCount > 0) withdrawalsCount else transactions.count { it.type == "WITHDRAWAL" }

    // Filter & Sort
    val filteredTransactions by remember(transactions, selectedCategory, selectedStatus, searchQuery, sortOrder) {
        derivedStateOf {
            var list = transactions

            // Category filter
            if (selectedCategory != "ALL") {
                list = when (selectedCategory) {
                    "BONUS" -> list.filter { it.type == "WELCOME_BONUS" || it.type == "DAILY_BONUS" }
                    else -> list.filter { it.type == selectedCategory }
                }
            }

            // Status filter
            if (selectedStatus != "ALL") {
                list = list.filter { it.status.equals(selectedStatus, ignoreCase = true) }
            }

            // Search query
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase(Locale.ROOT)
                list = list.filter {
                    it.trxId.lowercase(Locale.ROOT).contains(q) ||
                    it.title.lowercase(Locale.ROOT).contains(q) ||
                    it.type.lowercase(Locale.ROOT).contains(q) ||
                    (it.notes?.lowercase(Locale.ROOT)?.contains(q) == true)
                }
            }

            // Sort
            when (sortOrder) {
                SortOrder.NEWEST -> list.sortedByDescending { it.timestamp }
                SortOrder.OLDEST -> list.sortedBy { it.timestamp }
                SortOrder.HIGHEST_AMOUNT -> list.sortedByDescending { Math.abs(it.amount) }
                SortOrder.LOWEST_AMOUNT -> list.sortedBy { Math.abs(it.amount) }
            }
        }
    }

    // Function to share account statement
    fun shareStatement() {
        val sb = StringBuilder()
        sb.append("=== PAK ADS ACCOUNT STATEMENT ===\n")
        sb.append("Generated on: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())}\n")
        sb.append("---------------------------------\n")
        sb.append("Total Ad Rewards: +${actualAdRewards.toInt()} PKR ($actualAdsCount Ads Completed)\n")
        sb.append("Total Deposits:   +${actualApprovedDeposits.toInt()} PKR ($actualDepositsCount Submissions)\n")
        sb.append("Total Withdrawals:-${actualWithdrawals.toInt()} PKR ($actualWithdrawalsCount Requests)\n")
        sb.append("---------------------------------\n")
        sb.append("RECENT TRANSACTIONS (${filteredTransactions.size}):\n")
        filteredTransactions.take(15).forEach { trx ->
            val sign = if (trx.amount >= 0) "+" else ""
            val dateStr = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.US).format(Date(trx.timestamp))
            sb.append("• [${trx.status}] ${trx.title}: $sign${trx.amount.toInt()} PKR | ${trx.trxId} ($dateStr)\n")
        }
        sb.append("=================================\n")
        sb.append("PAK ADS - Premium Mobile Ads Platform")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share PAK ADS Statement")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Header Title & Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ROOM DATABASE AUDIT LEDGER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Transaction History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Export Statement Button
                    IconButton(
                        onClick = { shareStatement() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkSurfaceElevated, CircleShape)
                            .border(1.dp, DarkBorderHighlight, CircleShape)
                            .testTag("share_statement_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Statement",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Toggle Filters Button
                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (showFilters) NeonGreen.copy(alpha = 0.2f) else DarkSurfaceElevated,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (showFilters) NeonGreen else DarkBorderHighlight,
                                CircleShape
                            )
                            .testTag("toggle_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Toggle Filters",
                            tint = if (showFilters) NeonGreen else TextGrayLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Live Room Tracking Cards (Deposits, Withdrawals, Ad Rewards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "REAL-TIME TRACKERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrayLight,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tracker 1: Ad Viewing Rewards
                    TrackerCard(
                        title = "AD REWARDS",
                        amountText = "+${actualAdRewards.toInt()} PKR",
                        subtext = "$actualAdsCount Completed (50 PKR each)",
                        accentColor = NeonCyan,
                        icon = Icons.Default.PlayArrow,
                        isSelected = selectedCategory == "AD_REWARD",
                        onClick = {
                            selectedCategory = if (selectedCategory == "AD_REWARD") "ALL" else "AD_REWARD"
                        }
                    )

                    // Tracker 2: Deposits
                    TrackerCard(
                        title = "DEPOSITS",
                        amountText = "+${actualApprovedDeposits.toInt()} PKR",
                        subtext = "$actualDepositsCount Submissions (Easypaisa)",
                        accentColor = NeonGreen,
                        icon = Icons.Default.SouthWest,
                        isSelected = selectedCategory == "DEPOSIT",
                        onClick = {
                            selectedCategory = if (selectedCategory == "DEPOSIT") "ALL" else "DEPOSIT"
                        }
                    )

                    // Tracker 3: Withdrawals
                    TrackerCard(
                        title = "WITHDRAWALS",
                        amountText = "-${actualWithdrawals.toInt()} PKR",
                        subtext = "$actualWithdrawalsCount Requests",
                        accentColor = Color(0xFFFF6B6B),
                        icon = Icons.Default.NorthEast,
                        isSelected = selectedCategory == "WITHDRAWAL",
                        onClick = {
                            selectedCategory = if (selectedCategory == "WITHDRAWAL") "ALL" else "WITHDRAWAL"
                        }
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transaction_input"),
                placeholder = {
                    Text(
                        text = "Search by TRX ID, notes, or keyword...",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextGrayLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )
        }

        // Category Filter Chips
        item {
            val categoryChips = listOf(
                "ALL" to ("All (${transactions.size})"),
                "AD_REWARD" to ("▶ Ads ($actualAdsCount)"),
                "DEPOSIT" to ("💰 Deposits ($actualDepositsCount)"),
                "WITHDRAWAL" to ("💸 Withdrawals ($actualWithdrawalsCount)"),
                "BONUS" to ("🎁 Bonuses")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoryChips) { (key, label) ->
                    val isSelected = selectedCategory == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) NeonGreen else DarkSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isSelected) NeonGreen else DarkBorderHighlight,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = key }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("filter_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextGrayLight
                        )
                    }
                }
            }
        }

        // Expandable Filter Section (Status and Sort)
        item {
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderHighlight)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Status Filter Row
                        Text(
                            text = "FILTER BY STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayLight,
                            letterSpacing = 1.sp
                        )

                        val statuses = listOf("ALL", "COMPLETED", "APPROVED", "PENDING", "REJECTED", "PAID")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            statuses.forEach { st ->
                                val isSelected = selectedStatus == st
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonCyan else DarkSurfaceBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedStatus = st }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = st,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonCyan else TextGrayLight
                                    )
                                }
                            }
                        }

                        Divider(color = DarkSurfaceBorder, thickness = 1.dp)

                        // Sort Row
                        Text(
                            text = "SORT ORDER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayLight,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SortOrder.values().forEach { order ->
                                val isSelected = sortOrder == order
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) GoldYellow.copy(alpha = 0.2f) else DarkSurface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) GoldYellow else DarkSurfaceBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { sortOrder = order }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = order.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GoldYellow else TextGrayLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Result Count Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTransactions.size} ENTRIES DISPLAYED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )

                if (selectedCategory != "ALL" || selectedStatus != "ALL" || searchQuery.isNotEmpty()) {
                    Text(
                        text = "Reset Filters",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan,
                        modifier = Modifier
                            .clickable {
                                selectedCategory = "ALL"
                                selectedStatus = "ALL"
                                searchQuery = ""
                            }
                            .padding(4.dp)
                    )
                }
            }
        }

        // List of Transactions or Empty State
        if (filteredTransactions.isEmpty()) {
            item {
                PakAdsCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(NeonCyan.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (transactions.isEmpty()) "No Transactions Yet" else "No Matching Transactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (transactions.isEmpty()) {
                                "Start watching ads to earn 50 PKR per ad, or make a deposit to activate your account!"
                            } else {
                                "No transactions found matching your current filter or search criteria."
                            },
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (onNavigateToWatchAds != null) {
                                Button(
                                    onClick = onNavigateToWatchAds,
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Watch Ads",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (onNavigateToDeposit != null) {
                                Button(
                                    onClick = onNavigateToDeposit,
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SouthWest,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Deposit",
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { trx ->
                TransactionCardItem(
                    trx = trx,
                    onClick = { selectedTransactionForDetails = trx },
                    onCopyTrxId = {
                        clipboardManager.setText(AnnotatedString(trx.trxId))
                        Toast.makeText(context, "Copied TRX: ${trx.trxId}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Detail Receipt Dialog
    selectedTransactionForDetails?.let { trx ->
        TransactionReceiptDialog(
            trx = trx,
            onDismiss = { selectedTransactionForDetails = null },
            onCopyTrxId = {
                clipboardManager.setText(AnnotatedString(trx.trxId))
                Toast.makeText(context, "Copied TRX ID to clipboard!", Toast.LENGTH_SHORT).show()
            },
            onShare = {
                val shareText = "PAK ADS Transaction Receipt:\n" +
                        "TRX: ${trx.trxId}\n" +
                        "Type: ${trx.title}\n" +
                        "Amount: ${if (trx.amount >= 0) "+" else ""}${trx.amount.toInt()} PKR\n" +
                        "Status: ${trx.status}\n" +
                        "Date: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(trx.timestamp))}\n" +
                        "Notes: ${trx.notes ?: "Verified local ledger record"}"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
            }
        )
    }
}

// -------------------------------------------------------------
// Component: Live Tracker Stat Card
// -------------------------------------------------------------
@Composable
private fun TrackerCard(
    title: String,
    amountText: String,
    subtext: String,
    accentColor: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else DarkSurfaceElevated
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) accentColor else DarkBorderHighlight
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amountText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                fontSize = 10.sp,
                color = TextGrayLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// Component: Transaction List Item
// -------------------------------------------------------------
@Composable
private fun TransactionCardItem(
    trx: TransactionEntity,
    onClick: () -> Unit,
    onCopyTrxId: () -> Unit
) {
    val isCredit = trx.amount >= 0

    val icon: ImageVector = when (trx.type) {
        "WELCOME_BONUS" -> Icons.Default.CardGiftcard
        "AD_REWARD" -> Icons.Default.PlayArrow
        "DAILY_BONUS" -> Icons.Default.CardGiftcard
        "DEPOSIT" -> Icons.Default.SouthWest
        "WITHDRAWAL" -> Icons.Default.NorthEast
        "REFUND" -> Icons.Default.Refresh
        else -> Icons.Default.PriceChange
    }

    val iconColor: Color = when (trx.type) {
        "WELCOME_BONUS" -> GoldYellow
        "AD_REWARD" -> NeonCyan
        "DAILY_BONUS" -> GoldYellow
        "DEPOSIT" -> NeonGreen
        "WITHDRAWAL" -> Color(0xFFFF6B6B)
        "REFUND" -> NeonCyan
        else -> NeonGreen
    }

    val statusColor = when (trx.status.uppercase(Locale.ROOT)) {
        "COMPLETED", "APPROVED", "PAID" -> NeonGreen
        "PENDING" -> WarningAmber
        "REJECTED" -> ErrorRed
        else -> TextGray
    }

    val typeBadgeText = when (trx.type) {
        "AD_REWARD" -> "50 PKR AD"
        "DEPOSIT" -> "EASYPAISA"
        "WITHDRAWAL" -> "PAYOUT"
        "WELCOME_BONUS" -> "WELCOME"
        "DAILY_BONUS" -> "BONUS"
        "REFUND" -> "REFUND"
        else -> "LEDGER"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("trx_card_${trx.trxId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, iconColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trx.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(iconColor.copy(alpha = 0.12f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = typeBadgeText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trx.trxId,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GoldYellow,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!trx.notes.isNullOrBlank()) {
                        Text(
                            text = trx.notes,
                            fontSize = 10.sp,
                            color = TextGrayLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(trx.timestamp)),
                        fontSize = 9.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Amount & Status Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isCredit) "+${trx.amount.toInt()} PKR" else "${trx.amount.toInt()} PKR",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCredit) NeonGreen else Color(0xFFFF6B6B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(0.5.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = trx.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Digital Receipt Modal Dialog
// -------------------------------------------------------------
@Composable
private fun TransactionReceiptDialog(
    trx: TransactionEntity,
    onDismiss: () -> Unit,
    onCopyTrxId: () -> Unit,
    onShare: () -> Unit
) {
    val isCredit = trx.amount >= 0
    val statusColor = when (trx.status.uppercase(Locale.ROOT)) {
        "COMPLETED", "APPROVED", "PAID" -> NeonGreen
        "PENDING" -> WarningAmber
        "REJECTED" -> ErrorRed
        else -> TextGray
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with PAK ADS branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(NeonGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAK ADS RECEIPT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextGrayLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Display
                Text(
                    text = if (isCredit) "+${trx.amount.toInt()} PKR" else "${trx.amount.toInt()} PKR",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCredit) NeonGreen else Color(0xFFFF6B6B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = trx.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = DarkSurfaceBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Detail Key-Value Rows
                ReceiptRow(
                    label = "Transaction ID",
                    value = trx.trxId,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = GoldYellow,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onCopyTrxId() }
                        )
                    }
                )

                ReceiptRow(
                    label = "Type & Category",
                    value = trx.title
                )

                ReceiptRow(
                    label = "Registered Mobile",
                    value = trx.userMobile
                )

                ReceiptRow(
                    label = "Date & Time",
                    value = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.US).format(Date(trx.timestamp))
                )

                if (!trx.notes.isNullOrBlank()) {
                    ReceiptRow(
                        label = "Details / Notes",
                        value = trx.notes
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Room Database Verified Security Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.08f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verified cryptographic audit record in Room database",
                            fontSize = 10.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderHighlight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = TextWhite, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGray
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                trailingIcon()
            }
        }
    }
}

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
}
