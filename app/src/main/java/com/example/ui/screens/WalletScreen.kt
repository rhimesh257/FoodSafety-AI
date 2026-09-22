package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WalletTransactionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: FoodSafetyViewModel,
    onNavigateBack: () -> Unit
) {
    val citizen by viewModel.currentCitizen.collectAsState()
    val transactions by viewModel.walletTransactions.collectAsState()

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmountText by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("918284729104") }
    var accountHolderName by remember { mutableStateOf(citizen?.fullName ?: "Rohan Verma") }
    var ifscCode by remember { mutableStateOf("SBIN0001245") }
    var bankName by remember { mutableStateOf("State Bank of India") }
    var withdrawError by remember { mutableStateOf<String?>(null) }

    val walletBalance = citizen?.walletBalanceINR ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Citizen Safety Wallet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Statutory Grievance Settlement Escrow", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("wallet_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // Main Balance Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SETTLED CITIZEN BALANCE", color = EmeraldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary.copy(alpha = 0.2f)) {
                                Text("Convertible to INR", color = EmeraldLight, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "₹${walletBalance.toInt()}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Total Statutory Rewards Earned from Settled Grievances",
                            color = Slate200,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                withdrawAmountText = if (walletBalance > 0) walletBalance.toInt().toString() else "500"
                                showWithdrawDialog = true
                            },
                            enabled = walletBalance > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("convert_to_inr_button")
                        ) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Convert into INR to Bank Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Reward Rules Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Dual-Ended Settlement Guarantee", fontWeight = FontWeight.Bold, color = OnEmeraldContainer, fontSize = 13.sp)
                            Text(
                                "Critical: ₹1,500 • High: ₹750 • Medium: ₹400. Rewards are credited immediately upon supervisory approval.",
                                color = OnEmeraldContainer.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Text(
                    "Transaction & Reward History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Slate900,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No wallet transactions yet.", color = Slate700, fontSize = 13.sp)
                            Text("Settled grievances automatically credit your wallet.", color = Slate500, fontSize = 11.5.sp)
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemCard(tx = tx)
                }
            }
        }
    }

    // Convert into INR Dialog
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Direct Bank Payout (INR)")
                }
            },
            text = {
                Column {
                    Text(
                        "Convert citizen grievance settlement rewards to Indian Rupees. Provide valid bank details.",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it.filter { char -> char.isDigit() } },
                        label = { Text("Amount to Convert (Max: ₹${walletBalance.toInt()})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Bank Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("bank_account_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it.uppercase() },
                        label = { Text("Bank IFSC Code (11 alphanumeric)") },
                        modifier = Modifier.fillMaxWidth().testTag("ifsc_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth().testTag("bank_name_input"),
                        singleLine = true
                    )

                    withdrawError?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.5.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = withdrawAmountText.toDoubleOrNull() ?: 0.0
                        if (amount <= 0 || amount > walletBalance) {
                            withdrawError = "Please enter valid amount up to current balance."
                            return@Button
                        }
                        if (accountNumber.length < 9) {
                            withdrawError = "Enter valid bank account number."
                            return@Button
                        }
                        if (ifscCode.length != 11) {
                            withdrawError = "IFSC code must be exactly 11 characters."
                            return@Button
                        }

                        viewModel.withdrawWalletBalance(
                            amount = amount,
                            accountNumber = accountNumber,
                            accountHolderName = accountHolderName,
                            ifscCode = ifscCode,
                            bankName = bankName,
                            onSuccess = {
                                showWithdrawDialog = false
                            }
                        )
                    },
                    modifier = Modifier.testTag("confirm_payout_button")
                ) {
                    Text("Transfer to Bank")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(tx: WalletTransactionEntity) {
    val isCredit = tx.type == "CREDIT_REWARD"
    val dateStr = remember(tx.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(tx.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isCredit) EmeraldContainer else BlueContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) EmeraldDark else BlueInfo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Slate900)
                Text(tx.subtitle, fontSize = 11.5.sp, color = Slate700)
                Text(dateStr, fontSize = 10.5.sp, color = Slate500)
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${tx.amount.toInt()}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isCredit) EmeraldPrimary else Slate900
            )
        }
    }
}
