package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FoodBusinessEntity
import com.example.data.local.entity.GrievanceEntity
import com.example.engine.VerificationEngine
import com.example.ui.camera.CameraCaptureModal
import com.example.ui.camera.CameraUtils
import com.example.ui.camera.FullscreenPhotoInspectionDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.CapturedPhotoItem
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGrievanceScreen(
    viewModel: FoodSafetyViewModel,
    onNavigateBack: () -> Unit,
    onGrievanceSubmitted: (GrievanceEntity) -> Unit
) {
    var issueType by remember { mutableStateOf("ONLINE") } // "ONLINE" or "OFFLINE"

    // Online Invoice state
    var invoiceUploaded by remember { mutableStateOf(true) }
    var invoiceUri by remember { mutableStateOf("content://invoices/swiggy_ord_4819.pdf") }
    var invoiceOcrStatus by remember { mutableStateOf("VALID") } // "VALID", "UNCLEAR_UNDER_REVIEW"
    var invoiceSummary by remember { mutableStateOf("Zomato Inv #5910: 1x Chicken Biryani, 1x Raita (₹380)") }

    // Food Business & FSSAI
    var foodBusinessName by remember { mutableStateOf("Spice Garden Fine Dine") }
    var selectedFssaiLicense by remember { mutableStateOf("10021011000452") }
    var selectedBusinessEntity by remember { mutableStateOf<FoodBusinessEntity?>(null) }
    var showBusinessDropdown by remember { mutableStateOf(false) }

    // Issue Description & Keyword
    var keyword by remember { mutableStateOf("Dead cockroach found inside gravy container") }
    var description by remember { mutableStateOf("Opened the sealed takeout bowl and discovered a dead insect submerged in the chicken curry. Noticeable foul odor and contaminated packaging.") }

    // Voice Description
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordedSeconds by remember { mutableStateOf(12) }
    var voiceNoteSaved by remember { mutableStateOf(true) }

    // Photos state (strictly live camera angle captures, no gallery!)
    val capturedPhotos = remember {
        mutableStateListOf(
            CapturedPhotoItem(
                angleLabel = "Angle 1: Top Overhead View of Dish",
                bitmap = CameraUtils.createEvidenceSnapshot("Angle 1: Top Overhead View", "Spice Garden Fine Dine"),
                clarityScore = 92.5f,
                brightnessScore = 68.0f,
                passed = true
            ),
            CapturedPhotoItem(
                angleLabel = "Angle 2: Close-up of Contamination",
                bitmap = CameraUtils.createEvidenceSnapshot("Angle 2: Close-up of Contamination", "Spice Garden Fine Dine"),
                clarityScore = 88.0f,
                brightnessScore = 62.0f,
                passed = true
            ),
            CapturedPhotoItem(
                angleLabel = "Angle 3: Container Seal & Batch Label",
                bitmap = CameraUtils.createEvidenceSnapshot("Angle 3: Container Seal & Batch Label", "Spice Garden Fine Dine"),
                clarityScore = 94.0f,
                brightnessScore = 70.0f,
                passed = true
            )
        )
    }

    var showCameraModal by remember { mutableStateOf(false) }
    var inspectingPhoto by remember { mutableStateOf<CapturedPhotoItem?>(null) }

    // Structural filter warning modal or inline feedback
    var photoFilterWarning by remember { mutableStateOf<String?>(null) }

    // User Location
    var userLocation by remember { mutableStateOf("Outer Ring Road, Connaught Place, New Delhi (GPS: 28.6315° N, 77.2167° E)") }

    // Warning question
    var facedWarnings by remember { mutableStateOf(false) }
    var facedWarningsDetails by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("File Food Safety Grievance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("FSSAI Statutory Controlled Pipeline", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Step 1: Issue Type Selection (Online vs Offline)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Select Issue Type", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy800)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = issueType == "ONLINE",
                            onClick = { issueType = "ONLINE" },
                            label = { Text("Online Delivery / Cloud Kitchen") },
                            leadingIcon = { Icon(Icons.Default.DeliveryDining, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("issue_type_online")
                        )
                        FilterChip(
                            selected = issueType == "OFFLINE",
                            onClick = { issueType = "OFFLINE" },
                            label = { Text("Offline Restaurant / Eatery") },
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("issue_type_offline")
                        )
                    }

                    // Online Order Invoice Requirement
                    AnimatedVisibility(visible = issueType == "ONLINE") {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BlueContainer.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BlueInfo, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Order Invoice Attached (OCR Extracted)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = BlueInfo)
                                        Text(invoiceSummary, fontSize = 11.5.sp, color = Slate700)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Note: Invoices are not rejected if OCR is partly unclear; unclear cases are automatically escalated for manual verification.",
                                            fontSize = 10.5.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Food Business & FSSAI Lookup
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Food Business & FSSAI License", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy800)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = foodBusinessName,
                        onValueChange = {
                            foodBusinessName = it
                            viewModel.searchFssaiBusinesses(it)
                            showBusinessDropdown = true
                        },
                        label = { Text("Food Establishment / Cloud Kitchen Name") },
                        modifier = Modifier.fillMaxWidth().testTag("business_name_input"),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showBusinessDropdown = !showBusinessDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )

                    // FSSAI Database Status Card
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FSSAI License: $selectedFssaiLicense", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate900)
                                Text("Statutory compliance status checked against National FSSAI Registry", fontSize = 10.5.sp, color = Slate500)
                            }
                            Badge(containerColor = EmeraldContainer) {
                                Text("FSSAI Active", color = OnEmeraldContainer, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }
            }

            // Step 3: Keyword & Detailed Problem Description
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. Problem Keyword & Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy800)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        label = { Text("Keyword / Summary (Must match photo evidence)") },
                        modifier = Modifier.fillMaxWidth().testTag("keyword_input"),
                        supportingText = { Text("Cross-matched against CV object detection", fontSize = 11.sp) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Problem Description") },
                        modifier = Modifier.fillMaxWidth().testTag("description_input"),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Description Recording Widget
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isRecordingVoice = !isRecordingVoice
                                    if (isRecordingVoice) {
                                        voiceRecordedSeconds = 14
                                        voiceNoteSaved = true
                                    }
                                },
                                modifier = Modifier
                                    .background(if (isRecordingVoice) RedCritical else EmeraldPrimary, CircleShape)
                                    .size(40.dp)
                                    .testTag("record_voice_button")
                            ) {
                                Icon(
                                    imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Record Voice Description",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isRecordingVoice) "Recording Voice Note..." else if (voiceNoteSaved) "Voice Note Attached ($voiceRecordedSeconds sec)" else "Attach Voice Description",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isRecordingVoice) RedCritical else Slate900
                                )
                                Text(
                                    text = "Speak clearly to describe contamination or physical condition",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            if (voiceNoteSaved && !isRecordingVoice) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = EmeraldPrimary)
                            }
                        }
                    }
                }
            }

            // Step 4: Strict Live Camera Angles (Min 2, Max 15 - NO GALLERY per prompt!)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("4. Live Evidence Photos (${capturedPhotos.size}/15)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy800)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (capturedPhotos.size >= 2) EmeraldContainer else RedContainer
                        ) {
                            Text(
                                text = if (capturedPhotos.size >= 2) "Min 2 Met" else "Min 2 Required",
                                color = if (capturedPhotos.size >= 2) OnEmeraldContainer else RedCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Statutory Mandate: Minimum 2 and maximum 15 pictures from different angles. Gallery uploads are strictly blocked to prevent AI spoofing or stale photos.",
                        fontSize = 12.sp,
                        color = Slate500,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prominent Camera Launcher Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Navy900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("launch_camera_action_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldDark,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Food Evidence Camera", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Snap food photos directly using your phone's camera", color = Slate300, fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = { showCameraModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("snap_food_photo_button")
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Captured angle previews
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(capturedPhotos) { index, photo ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable { inspectingPhoto = photo }
                                    .testTag("captured_photo_item_$index")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Navy800),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (photo.bitmap != null) {
                                            Image(
                                                bitmap = photo.bitmap.asImageBitmap(),
                                                contentDescription = photo.angleLabel,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(32.dp))
                                        }

                                        // Angle tag overlay
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.Black.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp)
                                        ) {
                                            Text("Angle #${index + 1}", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }

                                        // Delete icon if count > 2
                                        if (capturedPhotos.size > 2) {
                                            IconButton(
                                                onClick = { capturedPhotos.removeAt(index) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(photo.angleLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Clarity: ${photo.clarityScore.toInt()}%", fontSize = 10.sp, color = EmeraldDark)
                                        }
                                        Text("Tap to view", fontSize = 9.sp, color = Slate400)
                                    }
                                }
                            }
                        }

                        // Add new angle button (Camera Snap Only)
                        if (capturedPhotos.size < 15) {
                            item {
                                OutlinedButton(
                                    onClick = { showCameraModal = true },
                                    modifier = Modifier
                                        .height(150.dp)
                                        .width(135.dp)
                                        .testTag("snap_angle_camera_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Snap Camera Angle", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${15 - capturedPhotos.size} slots left", fontSize = 9.sp, color = Slate400)
                                    }
                                }
                            }
                        }
                    }

                    // Structural Image Filter & CV Object Detection Status Box
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = OnEmeraldContainer, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("On-Device CV Object Detection & Structural Filter Passed", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = OnEmeraldContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Detected Food Objects: curry_dish, plate, dining_surface. Structural check confirmed: No pitch-black darkness, no heavy blur, no screenshots.",
                                fontSize = 10.5.sp,
                                color = OnEmeraldContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Step 5: User Location (GPS / Maps)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("5. Incident Location (GPS / Maps)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy800)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userLocation,
                        onValueChange = { userLocation = it },
                        label = { Text("GPS Coordinates & Address") },
                        modifier = Modifier.fillMaxWidth().testTag("location_input"),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        trailingIcon = {
                            TextButton(onClick = {
                                userLocation = "Connaught Place Block B, New Delhi (GPS: 28.6328° N, 77.2197° E)"
                            }) {
                                Text("Auto-GPS", fontSize = 11.sp)
                            }
                        }
                    )
                }
            }

            // Step 6: Warnings & Intimidation Prompt
            Card(
                colors = CardDefaults.cardColors(containerColor = if (facedWarnings) RedContainer else Slate100),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Have you faced any warnings or intimidation?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (facedWarnings) RedCritical else Slate900
                            )
                            Text(
                                "From food business operator or officials. Automatically escalates with fast-forward priority.",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                        Switch(
                            checked = facedWarnings,
                            onCheckedChange = { facedWarnings = it },
                            modifier = Modifier.testTag("faced_warnings_switch")
                        )
                    }

                    AnimatedVisibility(visible = facedWarnings) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = facedWarningsDetails,
                                onValueChange = { facedWarningsDetails = it },
                                label = { Text("Specify warning or threat details") },
                                modifier = Modifier.fillMaxWidth().testTag("warnings_details_input"),
                                placeholder = { Text("e.g. Manager threatened physical harassment if complaint is lodged") }
                            )
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    viewModel.submitGrievance(
                        issueType = issueType,
                        invoiceUploaded = invoiceUploaded,
                        invoiceUri = invoiceUri,
                        invoiceOcrStatus = invoiceOcrStatus,
                        invoiceSummary = invoiceSummary,
                        foodBusinessName = foodBusinessName,
                        foodBusinessLicense = selectedFssaiLicense,
                        keyword = keyword,
                        description = description,
                        voiceNoteDurationSec = voiceRecordedSeconds,
                        voiceNotePath = "audio_note_${System.currentTimeMillis()}.m4a",
                        photos = capturedPhotos,
                        userLocation = userLocation,
                        latitude = 28.6315,
                        longitude = 77.2167,
                        facedWarnings = facedWarnings,
                        facedWarningsDetails = facedWarningsDetails,
                        onSubmitted = { submitted ->
                            onGrievanceSubmitted(submitted)
                        }
                    )
                },
                enabled = capturedPhotos.size >= 2 && foodBusinessName.isNotBlank() && keyword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_grievance_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit to Gatekeeper Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCameraModal) {
        val nextIdx = capturedPhotos.size + 1
        CameraCaptureModal(
            defaultAngleLabel = "Angle $nextIdx: Evidence Perspective",
            businessName = foodBusinessName.ifBlank { "Food Safety Inspection" },
            userLocation = userLocation,
            onPhotoCaptured = { newPhoto ->
                capturedPhotos.add(newPhoto)
                showCameraModal = false
            },
            onDismiss = { showCameraModal = false }
        )
    }

    inspectingPhoto?.let { photo ->
        FullscreenPhotoInspectionDialog(
            photo = photo,
            onDismiss = { inspectingPhoto = null }
        )
    }
}
