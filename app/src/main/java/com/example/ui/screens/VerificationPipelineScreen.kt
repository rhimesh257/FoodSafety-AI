package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GrievanceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationPipelineScreen(
    viewModel: FoodSafetyViewModel,
    grievance: GrievanceEntity,
    onNavigateBack: () -> Unit,
    onNavigateOfficialDashboard: () -> Unit,
    onNavigateWallet: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Verification Gatekeeper", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Grievance #${grievance.grievanceId}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("verification_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateOfficialDashboard, modifier = Modifier.testTag("nav_to_official_from_verification")) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Official Dashboard", tint = Navy800)
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
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Core Pipeline Verification Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PIPELINE VERIFICATION STATE", color = EmeraldLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        val (priorityColor, priorityBg) = when (grievance.priority) {
                            "CRITICAL" -> RedCritical to RedContainer
                            "HIGH" -> AmberWarning to AmberContainer
                            "MEDIUM" -> BlueInfo to BlueContainer
                            else -> Slate700 to Slate200
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = priorityBg) {
                            Text(
                                text = "${grievance.priority} PRIORITY",
                                color = priorityColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = grievance.foodBusinessName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = grievance.keyword,
                        color = Slate200,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Dual Identifiers & Escrow Stats
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Grievance ID", color = Slate500, fontSize = 11.sp)
                            Text(grievance.grievanceId, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Citizen User ID", color = Slate500, fontSize = 11.sp)
                            Text(grievance.userId, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Assigned Officer ID", color = Slate500, fontSize = 11.sp)
                            Text(grievance.assignedOfficerId, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Reward / Cash Status", color = Slate500, fontSize = 11.sp)
                            Text("₹${grievance.rewardAmountINR.toInt()} (${grievance.cashStatus})", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Warnings Escalation Card (If reported)
            if (grievance.facedWarnings) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RedCritical, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Citizen Warning / Threat Flagged", fontWeight = FontWeight.Bold, color = RedCritical, fontSize = 13.5.sp)
                            Text("Directly forwarded to Higher Officials & State Authority Oversight with fast-forward priority.", fontSize = 11.5.sp, color = RedCritical)
                            if (grievance.facedWarningsDetails.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Details: \"${grievance.facedWarningsDetails}\"", fontSize = 11.sp, color = Slate900)
                            }
                        }
                    }
                }
            }

            // Gatekeeper Check 1: Structural Image Filtering
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterFrames, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Structural Image Filtering", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Navy800)
                        Spacer(modifier = Modifier.weight(1f))
                        Badge(containerColor = if (grievance.imageQualityPassed) EmeraldContainer else RedContainer) {
                            Text(if (grievance.imageQualityPassed) "PASSED" else "FAILED", color = if (grievance.imageQualityPassed) OnEmeraldContainer else RedCritical, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Very first line of defence: fast, light weight filters ensuring valid photos and blocking pitch black photos, heavy blur, or accidental screenshots.",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Clarity Metric Score:", fontSize = 12.sp, color = Slate700)
                        Text("${grievance.imageQualityScore.toInt()}% (Clear & Structured)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Angle Count:", fontSize = 12.sp, color = Slate700)
                        Text("${grievance.photosCount} Live Angles Captured", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                }
            }

            // Gatekeeper Check 2: Computer Vision Object Detection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. On-Device Computer Vision Detection", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Navy800)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lightweight on-device model ensures photo actually contains food, kitchen, plate, or dining related objects.",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Slate100, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Detected Objects: ${grievance.cvDetectedLabels}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Text("Keyword Match Correlation: ${grievance.keywordPhotoMatchScore.toInt()}%", fontSize = 11.5.sp, color = EmeraldDark)
                        }
                    }
                }
            }

            // Gatekeeper Check 3: Legal Scan under Food Safety & Standards Act, 2006
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. FSSAI Act 2006 Statutory Jurisdiction", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Navy800)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Scans text to verify whether grievance falls under jurisdiction of food safety laws or is a general customer service dispute (ref: https://fssai.gov.in/cms/food-safety-and-standards-act-2006.php).",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (grievance.isFssaiJurisdiction) EmeraldContainer else Slate200,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Statutory Section: ${grievance.foodLawSection}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = if (grievance.isFssaiJurisdiction) OnEmeraldContainer else Slate900)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (grievance.isFssaiJurisdiction) "Falls squarely under statutory penal liability of FSSA 2006."
                                else "Identified as general commercial dispute (e.g. delivery delay). Rejected from enforcement pipeline.",
                                fontSize = 11.sp,
                                color = if (grievance.isFssaiJurisdiction) OnEmeraldContainer.copy(alpha = 0.85f) else Slate700
                            )
                        }
                    }
                }
            }

            // Gatekeeper Check 4: Surge Cluster & Duplicate Hash Analysis
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("4. Surge Cluster & Duplicate Hash Check", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Navy800)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Analyzes target business reports within short window and checks duplicate hash codes across unresolved grievances to bump priority.",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hash Code Fingerprint:", fontSize = 11.5.sp, color = Slate700)
                        Text(grievance.hashFingerprint, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Navy800)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Surge Cluster Status:", fontSize = 11.5.sp, color = Slate700)
                        Text(if (grievance.isSurgeCluster) "Surge Cluster Detected" else "Standard Incident", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (grievance.isSurgeCluster) AmberWarning else Slate700)
                    }
                }
            }

            // Action Buttons: Navigate to Officer Dashboard or Back
            Button(
                onClick = onNavigateOfficialDashboard,
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("go_to_official_dashboard_button")
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Higher Official Enforcement View", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (grievance.verificationStatus == "SETTLED") {
                OutlinedButton(
                    onClick = onNavigateWallet,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("go_to_wallet_from_verification")
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Settled Reward in Citizen Wallet (₹${grievance.rewardAmountINR.toInt()})")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
