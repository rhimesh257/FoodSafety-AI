package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CitizenUser
import com.example.data.local.entity.GrievanceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenHomeScreen(
    viewModel: FoodSafetyViewModel,
    onNavigateNewGrievance: () -> Unit,
    onNavigateVerificationDetails: (GrievanceEntity) -> Unit,
    onNavigateOfficialDashboard: () -> Unit,
    onNavigateWallet: () -> Unit,
    onNavigateStateAuthority: () -> Unit
) {
    val currentCitizen by viewModel.currentCitizen.collectAsState()
    val allGrievances by viewModel.allGrievances.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredGrievances = remember(allGrievances, selectedFilter) {
        when (selectedFilter) {
            "SETTLED" -> allGrievances.filter { it.verificationStatus == "SETTLED" }
            "IN_PROGRESS" -> allGrievances.filter { it.verificationStatus in listOf("VERIFIED", "FORWARDED_TO_OFFICIAL", "INSPECTION_IN_PROGRESS", "OFFICIAL_RESOLVED_PENDING_AUDIT") }
            "REJECTED" -> allGrievances.filter { it.verificationStatus in listOf("REJECTED_CUSTOMER_SERVICE", "REJECTED_IMAGE_FAIL", "DUPLICATE_MERGED") }
            else -> allGrievances
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Navy800,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("FoodSafe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Citizen Safety Pipeline", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                },
                actions = {
                    // Quick navigation to Official Portal & State Authority
                    IconButton(onClick = onNavigateStateAuthority, modifier = Modifier.testTag("state_authority_nav_button")) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "State Authority", tint = Navy800)
                    }
                    IconButton(onClick = onNavigateOfficialDashboard, modifier = Modifier.testTag("official_portal_nav_button")) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Official Dashboard", tint = Navy800)
                    }
                    // Wallet quick balance button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EmeraldContainer,
                        modifier = Modifier
                            .clickable { onNavigateWallet() }
                            .padding(end = 12.dp)
                            .testTag("wallet_balance_header_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "₹${currentCitizen?.walletBalanceINR?.toInt() ?: 0}",
                                fontWeight = FontWeight.Bold,
                                color = OnEmeraldContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateNewGrievance,
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                text = { Text("Report Issue", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_report_grievance")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // Citizen Identity & Status Card
            item {
                CitizenHeaderCard(
                    citizen = currentCitizen,
                    onWalletClick = onNavigateWallet
                )
            }

            // Quick Camera Food Report Action Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateNewGrievance() }
                        .testTag("quick_snap_food_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Take Food Photo & Report",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Snap phone camera photos of spoiled food, foreign objects, or hygiene violations",
                                color = Slate300,
                                fontSize = 11.5.sp
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = EmeraldLight)
                    }
                }
            }

            // Dual Verification Pipeline Banner
            item {
                PipelineOverviewBanner(
                    onOfficialClick = onNavigateOfficialDashboard
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${allGrievances.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "IN_PROGRESS",
                        onClick = { selectedFilter = "IN_PROGRESS" },
                        label = { Text("In Progress") }
                    )
                    FilterChip(
                        selected = selectedFilter == "SETTLED",
                        onClick = { selectedFilter = "SETTLED" },
                        label = { Text("Settled & Rewarded") }
                    )
                    FilterChip(
                        selected = selectedFilter == "REJECTED",
                        onClick = { selectedFilter = "REJECTED" },
                        label = { Text("Filtered / Closed") }
                    )
                }
            }

            // Grievances list
            if (filteredGrievances.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Slate500, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No grievances found in this category", color = Slate700, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap 'Report Issue' to submit a photo-verified food grievance.", fontSize = 12.sp, color = Slate500)
                        }
                    }
                }
            } else {
                items(filteredGrievances, key = { it.grievanceId }) { grievance ->
                    GrievanceCard(
                        grievance = grievance,
                        onClick = {
                            viewModel.selectGrievance(grievance)
                            onNavigateVerificationDetails(grievance)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CitizenHeaderCard(
    citizen: CitizenUser?,
    onWalletClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = citizen?.fullName ?: "Citizen Complainant",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "AADHAR: ${citizen?.aadharNumber ?: "Verified"} • PAN: ${citizen?.panNumber ?: "Active"}",
                        color = Slate200,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary,
                    modifier = Modifier.clickable { onWalletClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wallet", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun PipelineOverviewBanner(
    onOfficialClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dual-Ended Verification Pipeline", fontWeight = FontWeight.Bold, color = OnEmeraldContainer, fontSize = 13.5.sp)
                Text("Citizen reports are verified via CV & FSSAI 2006 laws, then officially inspected with photo evidence.", fontSize = 11.5.sp, color = OnEmeraldContainer.copy(alpha = 0.85f))
            }
            TextButton(onClick = onOfficialClick) {
                Text("Officer View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
            }
        }
    }
}

@Composable
fun GrievanceCard(
    grievance: GrievanceEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("grievance_card_${grievance.grievanceId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = grievance.grievanceId,
                        fontWeight = FontWeight.Bold,
                        color = Navy800,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (grievance.issueType == "ONLINE") BlueContainer else Slate200
                    ) {
                        Text(
                            text = grievance.issueType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (grievance.issueType == "ONLINE") BlueInfo else Slate700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Priority Badge
                val (priorityColor, priorityBg) = when (grievance.priority) {
                    "CRITICAL" -> RedCritical to RedContainer
                    "HIGH" -> AmberWarning to AmberContainer
                    "MEDIUM" -> BlueInfo to BlueContainer
                    else -> Slate700 to Slate200
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = priorityBg
                ) {
                    Text(
                        text = "${grievance.priority} PRIORITY",
                        color = priorityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = grievance.foodBusinessName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900
            )

            Text(
                text = grievance.keyword,
                fontSize = 13.sp,
                color = Slate700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Verification & Pipeline Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val statusConfig = getStatusDisplay(grievance.verificationStatus)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusConfig.second
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusConfig.first, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusConfig.third,
                            color = statusConfig.first,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (grievance.verificationStatus == "SETTLED" && grievance.rewardAmountINR > 0) {
                    Text(
                        text = "Reward: ₹${grievance.rewardAmountINR.toInt()}",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${grievance.photosCount} angles", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }
    }
}

fun getStatusDisplay(status: String): Triple<Color, Color, String> {
    return when (status) {
        "SETTLED" -> Triple(EmeraldPrimary, EmeraldContainer, "Settled & Verified")
        "OFFICIAL_RESOLVED_PENDING_AUDIT" -> Triple(AmberWarning, AmberContainer, "Official Inspection Pending Audit")
        "FORWARDED_TO_OFFICIAL", "INSPECTION_IN_PROGRESS" -> Triple(BlueInfo, BlueContainer, "Assigned for Physical Inspection")
        "VERIFIED" -> Triple(EmeraldDark, EmeraldContainer, "Gatekeeper Verified")
        "REJECTED_CUSTOMER_SERVICE" -> Triple(Slate700, Slate200, "Commercial Customer Issue (Non-FSSAI)")
        "REJECTED_IMAGE_FAIL" -> Triple(RedCritical, RedContainer, "Rejected (Structural Image Fail)")
        "DUPLICATE_MERGED" -> Triple(AmberWarning, AmberContainer, "Duplicate Merged & Priority Bumped")
        else -> Triple(Slate700, Slate100, "Under Verification")
    }
}
