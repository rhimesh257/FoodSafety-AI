package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.camera.CameraCaptureModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialDashboardScreen(
    viewModel: FoodSafetyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateStateAuthority: () -> Unit,
    onNavigateVerificationDetails: (GrievanceEntity) -> Unit
) {
    val grievances by viewModel.officialDashboardGrievances.collectAsState()
    var selectedGrievanceForAction by remember { mutableStateOf<GrievanceEntity?>(null) }
    var showInspectionDialog by remember { mutableStateOf(false) }
    var showSupervisoryAuditDialog by remember { mutableStateOf(false) }
    var showOfficerCameraModal by remember { mutableStateOf(false) }

    // Inspection Dialog State
    var selectedAction by remember { mutableStateOf("SEIZURE_AND_SEALING_SEC38") }
    var inspectionRemarks by remember { mutableStateOf("Inspected commercial premises. Unhygienic preparation found; seized contaminated batch and issued statutory compounding notice.") }
    val proofPhotos = remember { mutableStateListOf("Premises Inspection Memo", "Form VA Seizure & Seal Receipt", "Lab Sample Dispatch Form") }

    // Supervisory Audit State
    var auditNotes by remember { mutableStateOf("Verified complete physical raid evidence and statutory compounding receipt. Approved for citizen settlement.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Navy900,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Higher Officials Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Food Safety Officers (FSO) Enforcement Pipeline", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("official_dashboard_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateStateAuthority, modifier = Modifier.testTag("official_to_state_authority")) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "State Authority", tint = Navy800)
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
            // Priority Mandate Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dual-Ended Enforcement & Official Rights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Grievances are strictly sorted from Critical to Low priority. Officers cannot settle issues without on-site camera proofs subject to independent anti-corruption validation and supervisory dual-check.",
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Grievances sorted by priority
            if (grievances.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No pending grievances in enforcement queue", fontWeight = FontWeight.SemiBold, color = Slate700)
                        }
                    }
                }
            } else {
                items(grievances, key = { it.grievanceId }) { grievance ->
                    OfficialGrievanceCard(
                        grievance = grievance,
                        onInspectClick = {
                            selectedGrievanceForAction = grievance
                            showInspectionDialog = true
                        },
                        onAuditClick = {
                            selectedGrievanceForAction = grievance
                            showSupervisoryAuditDialog = true
                        },
                        onViewDetails = {
                            viewModel.selectGrievance(grievance)
                            onNavigateVerificationDetails(grievance)
                        }
                    )
                }
            }
        }
    }

    // Physical Inspection & Proof Upload Dialog
    if (showInspectionDialog && selectedGrievanceForAction != null) {
        val currentGrievance = selectedGrievanceForAction!!
        AlertDialog(
            onDismissRequest = { showInspectionDialog = false },
            title = { Text("Physical Inspection & Enforcement") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Location: ${currentGrievance.foodBusinessName} (${currentGrievance.userLocationText})", fontSize = 12.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Statutory Enforcement Action Taken:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val actionOptions = listOf(
                        "SEIZURE_AND_SEALING_SEC38" to "Seizure & Sealing (Sec 38)",
                        "IMPROVEMENT_NOTICE_SEC32" to "Statutory Improvement Notice (Sec 32)",
                        "SAMPLES_SENT_TO_FOOD_LAB" to "Food Sample Seizure for Lab Analysis",
                        "COMPOUNDING_FINE_COLLECTED" to "Compounding Spot Fine Collected (Sec 58)"
                    )
                    actionOptions.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAction = key }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedAction == key, onClick = { selectedAction = key })
                            Text(label, fontSize = 12.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inspectionRemarks,
                        onValueChange = { inspectionRemarks = it },
                        label = { Text("Inspection Findings & Raid Memo") },
                        modifier = Modifier.fillMaxWidth().testTag("inspection_remarks_input"),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Anti-corruption proof upload requirement
                    Surface(shape = RoundedCornerShape(8.dp), color = Slate100, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Independent Proof Validator (${proofPhotos.size} photos)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy800)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Weak accountability loop safeguard: blank or generic gallery images are blocked. Live inspection camera photos must be submitted.", fontSize = 10.5.sp, color = Slate500)
                            Spacer(modifier = Modifier.height(8.dp))
                            proofPhotos.forEach { photo ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(photo, fontSize = 11.sp, color = Slate700)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { showOfficerCameraModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("officer_camera_snap_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Snap On-Site Photo (Live Camera)", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitOfficialInspection(
                            grievanceId = currentGrievance.grievanceId,
                            actionTaken = selectedAction,
                            remarks = inspectionRemarks,
                            proofPhotosCount = proofPhotos.size,
                            proofsList = proofPhotos.toList(),
                            onSuccess = {
                                showInspectionDialog = false
                            }
                        )
                    },
                    modifier = Modifier.testTag("submit_inspection_proof_button")
                ) {
                    Text("Submit Validated Proof")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInspectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Supervisory Dual-Verification Human Audit Dialog
    if (showSupervisoryAuditDialog && selectedGrievanceForAction != null) {
        val currentGrievance = selectedGrievanceForAction!!
        AlertDialog(
            onDismissRequest = { showSupervisoryAuditDialog = false },
            title = { Text("Supervisory Dual-Verification Audit") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Grievance #${currentGrievance.grievanceId} • ${currentGrievance.foodBusinessName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Designated Officer / Commissioner reviews proof submitted by field team. If approved, grievance settles and ₹${currentGrievance.rewardAmountINR.toInt()} is credited to citizen wallet. If rejected, it reverts back to field dashboard.",
                        fontSize = 11.5.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Slate100, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Field Officer Action: ${currentGrievance.officerActionTaken ?: "Inspection Executed"}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Officer Remarks: \"${currentGrievance.officerRemarks ?: "Raid conducted with seizure"}\"", fontSize = 11.sp, color = Slate700)
                            Text("Proof Validation Score: ${currentGrievance.officerProofValidationScore.toInt()}%", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = auditNotes,
                        onValueChange = { auditNotes = it },
                        label = { Text("Supervisory Audit Findings & Order") },
                        modifier = Modifier.fillMaxWidth().testTag("supervisory_notes_input"),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.performSupervisoryAudit(
                            grievanceId = currentGrievance.grievanceId,
                            isApproved = true,
                            auditNotes = auditNotes,
                            onComplete = { showSupervisoryAuditDialog = false }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("supervisory_approve_button")
                ) {
                    Text("Approve & Settle (Credit ₹${currentGrievance.rewardAmountINR.toInt()})")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.performSupervisoryAudit(
                            grievanceId = currentGrievance.grievanceId,
                            isApproved = false,
                            auditNotes = "Evidence incomplete or generic. Reverting to field officers for rigorous reinspection.",
                            onComplete = { showSupervisoryAuditDialog = false }
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCritical),
                    modifier = Modifier.testTag("supervisory_revert_button")
                ) {
                    Text("Reject & Revert")
                }
            }
        )
    }

    if (showOfficerCameraModal && selectedGrievanceForAction != null) {
        val current = selectedGrievanceForAction!!
        CameraCaptureModal(
            defaultAngleLabel = "Premises Sanitation & Hygiene",
            availableAnglePresets = listOf(
                "Premises Sanitation & Hygiene",
                "Form VA Seizure & Seal Tag",
                "Tamper-Evident Lab Sample Bag",
                "Statutory Notice Served to FBO",
                "Spot Fine Challan / Receipt"
            ),
            businessName = current.foodBusinessName,
            userLocation = current.userLocationText,
            onPhotoCaptured = { photo ->
                proofPhotos.add("${photo.angleLabel} (${photo.clarityScore.toInt()}% Clarity)")
                showOfficerCameraModal = false
            },
            onDismiss = { showOfficerCameraModal = false }
        )
    }
}

@Composable
fun OfficialGrievanceCard(
    grievance: GrievanceEntity,
    onInspectClick: () -> Unit,
    onAuditClick: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("official_card_${grievance.grievanceId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Priority & SLA Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // SLA countdown
                val hoursRemaining = ((grievance.deadlineTimestamp - System.currentTimeMillis()) / (3600 * 1000)).coerceAtLeast(0)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (hoursRemaining < 12) RedContainer else Slate100
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = if (hoursRemaining < 12) RedCritical else Slate700, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${hoursRemaining}h SLA remaining",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hoursRemaining < 12) RedCritical else Slate700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = grievance.foodBusinessName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Slate900
            )

            Text(
                text = grievance.keyword,
                fontSize = 13.sp,
                color = Slate700
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Statutory Violation: ${grievance.foodLawSection}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Navy800
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Evidence", fontSize = 12.sp)
                }

                if (grievance.verificationStatus == "OFFICIAL_RESOLVED_PENDING_AUDIT") {
                    Button(
                        onClick = onAuditClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f).testTag("official_audit_button_${grievance.grievanceId}")
                    ) {
                        Text("Supervisory Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (grievance.verificationStatus != "SETTLED") {
                    Button(
                        onClick = onInspectClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f).testTag("official_action_button_${grievance.grievanceId}")
                    ) {
                        Text("Log Physical Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                            Text("Settled & Closed", color = OnEmeraldContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
