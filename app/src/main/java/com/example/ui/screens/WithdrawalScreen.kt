package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WithdrawalRequestEntity
import com.example.ui.components.PakAdsCard
import com.example.ui.components.PakAdsPrimaryButton
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

@Composable
fun WithdrawalScreen(
    user: UserEntity?,
    config: AppConfigEntity,
    withdrawHistory: List<WithdrawalRequestEntity>,
    onSubmitWithdrawal: (
        method: String,
        holderName: String,
        accountNumber: String,
        amount: Double,
        saveDetails: Boolean
    ) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val availableBalance = user?.availableBalance ?: 0.0

    var selectedMethod by remember { mutableStateOf(user?.savedPaymentMethod ?: "JazzCash") }
    var accountHolderName by remember { mutableStateOf(user?.savedAccountHolder ?: "") }
    var accountNumber by remember { mutableStateOf(user?.savedAccountNumber ?: "") }
    var withdrawAmount by remember { mutableStateOf(config.minWithdrawal.toInt().toString()) }
    var saveDetails by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Available Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE FOR WITHDRAWAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayLight,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "MIN ${config.minWithdrawal.toInt()} PKR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₨",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.0f".format(availableBalance),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                        Text(
                            text = " PKR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGrayLight,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Withdrawals are processed directly to JazzCash or Easypaisa accounts.",
                        fontSize = 11.sp,
                        color = TextGrayLight
                    )
                }
            }
        }

        // Withdrawal Request Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "REQUEST WITHDRAWAL",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Method Selector: JazzCash vs Easypaisa
                    Text(
                        text = "Select Payment Method",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGrayLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // JazzCash Selector
                        val isJazzCash = selectedMethod == "JazzCash"
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMethod = "JazzCash" }
                                .testTag("method_jazzcash_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isJazzCash) Color(0xFF2E090B) else DarkSurfaceElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isJazzCash) JazzCashRed else DarkBorderHighlight
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(JazzCashRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "JC",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "JazzCash",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isJazzCash) TextWhite else TextGray
                                )
                            }
                        }

                        // Easypaisa Selector
                        val isEasypaisa = selectedMethod == "Easypaisa"
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMethod = "Easypaisa" }
                                .testTag("method_easypaisa_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEasypaisa) Color(0xFF042714) else DarkSurfaceElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isEasypaisa) EasypaisaGreen else DarkBorderHighlight
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(EasypaisaGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "EP",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Easypaisa",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEasypaisa) TextWhite else TextGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Account Holder Name
                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = {
                            accountHolderName = it
                            localError = null
                        },
                        label = { Text("Account Holder Name") },
                        placeholder = { Text("Full Name on $selectedMethod") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = NeonCyan)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = TextGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_holder_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Number
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = {
                            if (it.length <= 13) accountNumber = it
                            localError = null
                        },
                        label = { Text("$selectedMethod Account Number") },
                        placeholder = { Text("03XXXXXXXXX") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = NeonGreen)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = NeonGreen,
                            unfocusedLabelColor = TextGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_number_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Withdrawal Amount
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = {
                            withdrawAmount = it.filter { char -> char.isDigit() }
                            localError = null
                        },
                        label = { Text("Withdrawal Amount (PKR)") },
                        placeholder = { Text("Min ${config.minWithdrawal.toInt()} PKR") },
                        leadingIcon = {
                            Text(
                                text = "₨",
                                color = GoldYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldYellow,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = GoldYellow,
                            unfocusedLabelColor = TextGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Payment Details Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { saveDetails = !saveDetails },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = saveDetails,
                            onCheckedChange = { saveDetails = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonGreen,
                                uncheckedColor = TextGray
                            ),
                            modifier = Modifier.testTag("withdraw_save_details_checkbox")
                        )
                        Text(
                            text = "Save payment details for faster withdrawals",
                            fontSize = 12.sp,
                            color = TextGrayLight
                        )
                    }

                    val displayError = localError ?: errorMessage
                    if (displayError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayError,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(28.dp))
                        }
                    } else {
                        PakAdsPrimaryButton(
                            text = "SUBMIT WITHDRAWAL",
                            onClick = {
                                val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
                                when {
                                    accountHolderName.trim().isEmpty() -> {
                                        localError = "Account holder name is required."
                                    }
                                    accountNumber.trim().isEmpty() -> {
                                        localError = "Account number is required."
                                    }
                                    amount < config.minWithdrawal -> {
                                        localError = "Minimum withdrawal is ${config.minWithdrawal.toInt()} PKR."
                                    }
                                    amount > availableBalance -> {
                                        localError = "Insufficient balance. Available: ${availableBalance.toInt()} PKR."
                                    }
                                    else -> {
                                        onSubmitWithdrawal(
                                            selectedMethod,
                                            accountHolderName.trim(),
                                            accountNumber.trim(),
                                            amount,
                                            saveDetails
                                        )
                                    }
                                }
                            },
                            testTag = "withdraw_submit_button"
                        )
                    }
                }
            }
        }

        // Previous Withdrawals List
        item {
            Text(
                text = "YOUR WITHDRAWAL HISTORY",
                fontSize = 12.sp,
                color = TextGrayLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (withdrawHistory.isEmpty()) {
            item {
                PakAdsCard {
                    Text(
                        text = "No withdrawal requests yet. Earn balance by watching ads and claim daily bonus to withdraw.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(withdrawHistory.size) { index ->
                val item = withdrawHistory[index]
                PakAdsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${item.amount.toInt()} PKR",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${item.paymentMethod})",
                                    fontSize = 11.sp,
                                    color = if (item.paymentMethod == "JazzCash") JazzCashRed else EasypaisaGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "To: ${item.accountHolderName} (${item.accountNumber})",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = "TRX: ${item.trxId}",
                                fontSize = 10.sp,
                                color = GoldYellow
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(item.createdAt)),
                                fontSize = 10.sp,
                                color = TextGrayLight
                            )
                        }

                        // Status Badge: PENDING, APPROVED, REJECTED, PAID
                        val (bgColor, textColor, border, label) = when (item.status) {
                            "PAID" -> Tuple4(NeonGreen.copy(alpha = 0.2f), NeonGreen, NeonGreen, "PAID")
                            "APPROVED" -> Tuple4(NeonCyan.copy(alpha = 0.2f), NeonCyan, NeonCyan, "APPROVED")
                            "REJECTED" -> Tuple4(ErrorRed.copy(alpha = 0.2f), ErrorRed, ErrorRed, "REJECTED")
                            else -> Tuple4(WarningAmber.copy(alpha = 0.2f), WarningAmber, WarningAmber, "PENDING")
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = textColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
