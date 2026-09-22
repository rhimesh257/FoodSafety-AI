package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GrievanceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateAuthorityScreen(
    viewModel: FoodSafetyViewModel,
    onNavigateBack: () -> Unit,
    onViewGrievanceDetails: (GrievanceEntity) -> Unit
) {
    val escalations by viewModel.stateAuthorityEscalations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = RedCritical,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("State Authority Oversight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Apex Food Safety & Anti-Corruption Commission", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("state_authority_back")) {
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
            // Mandate Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RedCritical)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("State Level Accountability Escalations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Matters escalated due to prescribed timeline SLA expiration, repeated citizen intimidation warnings, or official non-compliance are directly monitored by the State Food Safety Commissioner.",
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (escalations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("All district grievances resolved within prescribed statutory SLA", fontWeight = FontWeight.SemiBold, color = Slate700)
                        }
                    }
                }
            } else {
                items(escalations, key = { it.grievanceId }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = RedContainer) {
                                    Text(
                                        text = "ESCALATED TO STATE COMMISSIONER",
                                        color = RedCritical,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("ID: ${item.grievanceId}", fontSize = 11.sp, color = Slate500)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.foodBusinessName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            Text(item.keyword, fontSize = 13.sp, color = Slate700)

                            if (item.facedWarnings) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = RedCritical, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Citizen Protection Alert: Intimidation Reported", fontSize = 11.5.sp, color = RedCritical, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onViewGrievanceDetails(item) },
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Inspect Complete Audit Trail", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
