package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: FoodSafetyViewModel,
    onAuthSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("Rohan Verma") }
    var aadharNumber by remember { mutableStateOf("5412-8921-3341") }
    var panNumber by remember { mutableStateOf("BQWPV4489J") }
    var email by remember { mutableStateOf("rohan.verma@citizenmail.in") }
    var mobileNumber by remember { mutableStateOf("+91 98765 43210") }
    var permanentAddress by remember { mutableStateOf("H-42, Shanti Kunj, South Extension, New Delhi - 110049") }
    var temporaryAddress by remember { mutableStateOf("Flat 304, Green Palms Residency, Saket, New Delhi - 110017") }
    var ageText by remember { mutableStateOf("26") }

    // OTP verification modal state
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var isOtpVerified by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "FoodSafe Logo",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("FoodSafe Citizen Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Public to Official Food Safety Pipeline", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .widthIn(max = 640.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Statutory Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Statutory Citizen Verification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("FSSAI Act 2006 Statutory Dual Verification", color = Slate200, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Under Section 84 of the Food Safety & Standards Act, all citizen complainants must be authenticated with valid national ID to prevent frivolous claims and ensure legal accountability.",
                        color = Slate200.copy(alpha = 0.85f),
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form inputs
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Citizen Full Name (as per Aadhar)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("fullname_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = aadharNumber,
                    onValueChange = { aadharNumber = it },
                    label = { Text("AADHAR Number") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.weight(1f).testTag("aadhar_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = panNumber,
                    onValueChange = { panNumber = it.uppercase() },
                    label = { Text("PAN Number") },
                    modifier = Modifier.weight(1f).testTag("pan_input"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { char -> char.isDigit() } },
                    label = { Text("Age (>= 18 required)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.9f).testTag("age_input"),
                    singleLine = true,
                    supportingText = {
                        val ageVal = ageText.toIntOrNull() ?: 0
                        if (ageVal > 0 && ageVal < 18) {
                            Text("Age must be >= 18", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Citizen eligibility age", color = Slate500)
                        }
                    }
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number (OTP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1.3f).testTag("mobile_input"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("email_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = permanentAddress,
                onValueChange = { permanentAddress = it },
                label = { Text("Permanent Address (with PIN)") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("perm_address_input"),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = temporaryAddress,
                onValueChange = { temporaryAddress = it },
                label = { Text("Temporary / Current Living Address") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("temp_address_input"),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            // OTP Authentication Trigger Card
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isOtpVerified) EmeraldContainer else Slate100),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOtpVerified) Icons.Default.CheckCircle else Icons.Default.Sms,
                        contentDescription = null,
                        tint = if (isOtpVerified) EmeraldDark else Navy800,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOtpVerified) "Mobile OTP Authenticated" else "Mobile Number OTP Verification",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOtpVerified) OnEmeraldContainer else Slate900,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isOtpVerified) "Verified via secure SMS code" else "Authenticates citizen identity for legal complaints",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    if (!isOtpVerified) {
                        Button(
                            onClick = {
                                otpSent = true
                                showOtpDialog = true
                                otpInput = "849201" // Demo pre-filled OTP
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                            modifier = Modifier.testTag("send_otp_button")
                        ) {
                            Text("Verify OTP", fontSize = 12.sp)
                        }
                    } else {
                        Badge(containerColor = EmeraldPrimary) {
                            Text("Verified", color = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val ageVal = ageText.toIntOrNull() ?: 0
            val isFormValid = fullName.isNotBlank() && aadharNumber.isNotBlank() && panNumber.isNotBlank() &&
                    mobileNumber.isNotBlank() && ageVal >= 18

            Button(
                onClick = {
                    if (!isOtpVerified) {
                        viewModel.showMessage("Please complete mobile OTP verification first.")
                        return@Button
                    }
                    viewModel.registerCitizen(
                        name = fullName,
                        aadhar = aadharNumber,
                        pan = panNumber,
                        email = email,
                        mobile = mobileNumber,
                        permAddress = permanentAddress,
                        tempAddress = temporaryAddress,
                        age = ageVal,
                        onSuccess = onAuthSuccess
                    )
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_submit_button")
            ) {
                Icon(Icons.Default.HowToReg, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Citizen Signup & Enter Portal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct portal switch for Higher Officials or instant demo
            OutlinedButton(
                onClick = {
                    isOtpVerified = true
                    viewModel.registerCitizen(
                        name = fullName,
                        aadhar = aadharNumber,
                        pan = panNumber,
                        email = email,
                        mobile = mobileNumber,
                        permAddress = permanentAddress,
                        tempAddress = temporaryAddress,
                        age = 26,
                        onSuccess = onAuthSuccess
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("quick_demo_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Instant Demo Citizen Login (Pre-authenticated)")
            }
        }
    }

    // OTP Dialog
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Enter Mobile OTP") },
            text = {
                Column {
                    Text("Enter the 6-digit OTP code sent to $mobileNumber (Auto-filled for demo: 849201).", fontSize = 13.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it.take(6) },
                        label = { Text("6-Digit OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("otp_input_field"),
                        singleLine = true
                    )
                    otpError?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInput.length >= 4) {
                            isOtpVerified = true
                            showOtpDialog = false
                            otpError = null
                        } else {
                            otpError = "Please enter the valid OTP"
                        }
                    },
                    modifier = Modifier.testTag("verify_otp_confirm_button")
                ) {
                    Text("Confirm OTP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
