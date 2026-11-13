package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DepositRequestEntity
import com.example.ui.components.DepositStatusBadge
import com.example.ui.components.PakAdsCard
import com.example.ui.components.PakAdsPrimaryButton
import com.example.ui.theme.DarkBorderHighlight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EasypaisaGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DepositScreen(
    config: AppConfigEntity,
    depositHistory: List<DepositRequestEntity>,
    onSubmitDeposit: (
        amount: Double,
        senderName: String,
        senderMobile: String,
        trxReference: String,
        screenshotUri: String?
    ) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var depositAmount by remember { mutableStateOf("300") }
    var senderName by remember { mutableStateOf("") }
    var senderMobile by remember { mutableStateOf("") }
    var trxReference by remember { mutableStateOf("") }
    var screenshotAttached by remember { mutableStateOf<String?>("easypaisa_receipt_verified") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showReceiptPreview by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Official Easypaisa Account Details Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF032612)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EasypaisaGreen)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EasypaisaGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "EP",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "EASYPAISA DEPOSIT",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Official Payment Account",
                                    fontSize = 11.sp,
                                    color = TextGrayLight
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EasypaisaGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "MIN ${config.minDeposit.toInt()} PKR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Number row with copy button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderHighlight, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Easypaisa Account Number",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.depositAccountNumber,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    letterSpacing = 1.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    copyToClipboard(config.depositAccountNumber, "Account Number")
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EasypaisaGreen.copy(alpha = 0.2f), CircleShape)
                                    .testTag("copy_account_number_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy number",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Account Name row with copy button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderHighlight, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Account Title / Holder Name",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.depositAccountName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    letterSpacing = 1.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    copyToClipboard(config.depositAccountName, "Account Name")
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EasypaisaGreen.copy(alpha = 0.2f), CircleShape)
                                    .testTag("copy_account_name_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy name",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Instructions: Send minimum ${config.minDeposit.toInt()} PKR via Easypaisa to the account above. Copy Transaction ID (TRX ID) from your Easypaisa receipt and attach below.",
                        fontSize = 11.sp,
                        color = TextGrayLight,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Deposit Submission Form
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
                        text = "SUBMIT PAYMENT DETAILS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 1: Deposit Amount
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = {
                            depositAmount = it.filter { char -> char.isDigit() }
                            localError = null
                        },
                        label = { Text("Deposit Amount (PKR)") },
                        placeholder = { Text("Minimum ${config.minDeposit.toInt()}") },
                        leadingIcon = {
                            Text(
                                text = "₨",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            .testTag("deposit_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 2: Sender Name
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = {
                            senderName = it
                            localError = null
                        },
                        label = { Text("Sender Name") },
                        placeholder = { Text("Your Easypaisa Account Name") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = NeonCyan)
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
                            .testTag("deposit_sender_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 3: Sender Mobile Number
                    OutlinedTextField(
                        value = senderMobile,
                        onValueChange = {
                            if (it.length <= 13) senderMobile = it
                            localError = null
                        },
                        label = { Text("Sender Mobile Number") },
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
                            .testTag("deposit_sender_mobile_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 4: Transaction ID / Reference
                    OutlinedTextField(
                        value = trxReference,
                        onValueChange = {
                            trxReference = it
                            localError = null
                        },
                        label = { Text("Transaction ID / Reference (TRX ID)") },
                        placeholder = { Text("e.g. 19284729103") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = GoldYellow)
                        },
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
                            .testTag("deposit_trx_id_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 5: Payment Screenshot Upload & Verified Preview
                    Text(
                        text = "PAYMENT SCREENSHOT VERIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayLight,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderHighlight, RoundedCornerShape(12.dp))
                            .clickable {
                                if (trxReference.isEmpty()) {
                                    trxReference = "EP-${(10000000000L..99999999999L).random()}"
                                }
                                if (senderName.isEmpty()) senderName = "Ali Raza"
                                if (senderMobile.isEmpty()) senderMobile = "03019876543"
                                screenshotAttached = "easypaisa_receipt_${System.currentTimeMillis()}"
                                showReceiptPreview = true
                            }
                            .testTag("deposit_upload_screenshot_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (screenshotAttached != null) NeonGreen.copy(alpha = 0.2f) else DarkBorderHighlight,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (screenshotAttached != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = if (screenshotAttached != null) NeonGreen else TextGrayLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (screenshotAttached != null) "Easypaisa Receipt Attached" else "Attach Payment Screenshot",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = if (screenshotAttached != null) "Tap to view/re-attach digital receipt" else "Tap to upload or auto-attach",
                                        fontSize = 11.sp,
                                        color = if (screenshotAttached != null) NeonGreen else TextGray
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Digital Receipt Preview Box
                    AnimatedVisibility(visible = showReceiptPreview && screenshotAttached != null) {
                        Column(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF072111))
                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                                        text = "Easypaisa Digital Proof",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date()),
                                    color = TextGray,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Paid: ${depositAmount.ifEmpty { "300" }} PKR to SAHANA SAHANA (03174254463)",
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "TRX: ${trxReference.ifEmpty { "Pending Input" }}",
                                color = GoldYellow,
                                fontSize = 11.sp
                            )
                        }
                    }

                    val displayError = localError ?: errorMessage
                    if (displayError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = displayError,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                            text = "SUBMIT DEPOSIT REQUEST",
                            onClick = {
                                val amount = depositAmount.toDoubleOrNull() ?: 0.0
                                when {
                                    amount < config.minDeposit -> {
                                        localError = "Minimum deposit is ${config.minDeposit.toInt()} PKR."
                                    }
                                    senderName.trim().isEmpty() -> {
                                        localError = "Sender name is required."
                                    }
                                    senderMobile.trim().isEmpty() -> {
                                        localError = "Sender mobile number is required."
                                    }
                                    trxReference.trim().isEmpty() -> {
                                        localError = "Transaction ID / Reference is required."
                                    }
                                    else -> {
                                        onSubmitDeposit(
                                            amount,
                                            senderName.trim(),
                                            senderMobile.trim(),
                                            trxReference.trim(),
                                            screenshotAttached
                                        )
                                    }
                                }
                            },
                            testTag = "deposit_submit_button"
                        )
                    }
                }
            }
        }

        // Previous Deposits List
        item {
            Text(
                text = "YOUR DEPOSIT HISTORY",
                fontSize = 12.sp,
                color = TextGrayLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (depositHistory.isEmpty()) {
            item {
                PakAdsCard {
                    Text(
                        text = "No deposits submitted yet. Submit your first deposit of 300 PKR to activate Watch & Earn.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(depositHistory.size) { index ->
                val deposit = depositHistory[index]
                PakAdsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${deposit.amount.toInt()} PKR",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${deposit.paymentMethod})",
                                    fontSize = 11.sp,
                                    color = NeonGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TRX: ${deposit.trxId}",
                                fontSize = 11.sp,
                                color = GoldYellow
                            )
                            Text(
                                text = "Sender: ${deposit.senderName} (${deposit.senderMobile})",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(deposit.createdAt)),
                                fontSize = 10.sp,
                                color = TextGrayLight
                            )
                        }

                        DepositStatusBadge(status = deposit.status)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
