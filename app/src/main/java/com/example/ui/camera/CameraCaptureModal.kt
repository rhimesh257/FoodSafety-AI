package com.example.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.engine.VerificationEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.CapturedPhotoItem

@Composable
fun CameraCaptureModal(
    defaultAngleLabel: String,
    availableAnglePresets: List<String> = listOf(
        "Overhead Food Dish View",
        "Close-up of Contamination / Pest",
        "Packaging & Batch / Expiry Stamp",
        "Bill / Receipt Beside Dish",
        "Kitchen / Food Prep Area View",
        "FSSAI License Display Board"
    ),
    businessName: String = "Spice Garden Fine Dine",
    userLocation: String = "Connaught Place, New Delhi",
    onPhotoCaptured: (CapturedPhotoItem) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedAngle by remember { mutableStateOf(defaultAngleLabel) }
    var flashEnabled by remember { mutableStateOf(false) }
    var gridEnabled by remember { mutableStateOf(true) }

    // Captured preview state (null = viewfinder mode, non-null = review/verification mode)
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var structuralAnalysis by remember {
        mutableStateOf<com.example.engine.ImageStructuralCheckResult?>(null)
    }

    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Native Camera Launcher (captures real full-res photo via FileProvider)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            val decoded = CameraUtils.decodeBitmapFromUri(context, tempPhotoUri!!)
            if (decoded != null) {
                val stamped = CameraUtils.stampRealPhotoWithEvidenceWatermark(
                    sourceBitmap = decoded,
                    angleLabel = selectedAngle,
                    businessName = businessName,
                    userLocation = userLocation
                )
                capturedBitmap = stamped
                structuralAnalysis = VerificationEngine.analyzeCapturedBitmap(stamped, selectedAngle)
            }
        }
    }

    // Native Camera Launcher (Preview Fallback)
    val takePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val stamped = CameraUtils.stampRealPhotoWithEvidenceWatermark(
                sourceBitmap = bitmap,
                angleLabel = selectedAngle,
                businessName = businessName,
                userLocation = userLocation
            )
            capturedBitmap = stamped
            structuralAnalysis = VerificationEngine.analyzeCapturedBitmap(stamped, selectedAngle)
        }
    }

    // Camera Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            try {
                val (_, uri) = CameraUtils.createTempImageUri(context)
                tempPhotoUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                takePreviewLauncher.launch(null)
            }
        }
    }

    fun launchNativePhoneCamera() {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            try {
                val (_, uri) = CameraUtils.createTempImageUri(context)
                tempPhotoUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                takePreviewLauncher.launch(null)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("camera_modal_root")
        ) {
            if (capturedBitmap == null) {
                // ==================== VIEWFINDER MODE ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // Top HUD Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("camera_close_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "LIVE EVIDENCE CAMERA",
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "SEC 84 FSSAI STATUTORY EVIDENCE",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { gridEnabled = !gridEnabled },
                                modifier = Modifier
                                    .background(
                                        if (gridEnabled) EmeraldPrimary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.GridOn,
                                    contentDescription = "Toggle Grid",
                                    tint = if (gridEnabled) EmeraldLight else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { flashEnabled = !flashEnabled },
                                modifier = Modifier
                                    .background(
                                        if (flashEnabled) AmberWarning.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flash Toggle",
                                    tint = if (flashEnabled) AmberWarning else Color.White
                                )
                            }
                        }
                    }

                    // Main Viewfinder Frame
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Navy900)
                            .border(1.dp, Slate700, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Viewfinder background pattern & simulated focus target
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(110.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = selectedAngle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Frame the food article and any contamination clearly within the target reticle.",
                                color = Slate300,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { launchNativePhoneCamera() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("open_phone_camera_center_button")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Phone Camera", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // Rule-of-Thirds Grid Overlay
                        if (gridEnabled) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        // Top GPS Watermark Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "GPS: 28.6315° N, 77.2167° E",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Anti-Spoofing Statutory Notice Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            Text(
                                "Live Shutter Mandate • Gallery Selection Disabled",
                                color = EmeraldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Angle Selector Carousel
                    Text(
                        "SELECT EVIDENCE ANGLE:",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableAnglePresets) { preset ->
                            val isSelected = preset == selectedAngle
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) EmeraldPrimary else Navy800,
                                modifier = Modifier
                                    .clickable { selectedAngle = preset }
                                    .testTag("angle_preset_${preset.take(8)}")
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) Color.White else Slate300,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Shutter Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fallback Snapshot Simulator button (for emulators or test environments)
                        IconButton(
                            onClick = {
                                val bmp = CameraUtils.createEvidenceSnapshot(
                                    angleLabel = selectedAngle,
                                    businessName = businessName,
                                    userLocation = userLocation
                                )
                                capturedBitmap = bmp
                                structuralAnalysis = VerificationEngine.analyzeCapturedBitmap(bmp, selectedAngle)
                            },
                            modifier = Modifier
                                .background(Slate800, CircleShape)
                                .size(50.dp)
                                .testTag("fallback_snapshot_button")
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = "Snapshot Fallback",
                                tint = EmeraldLight
                            )
                        }

                        // Main Shutter Button (Triggers native phone camera hardware!)
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .border(4.dp, Color.White, CircleShape)
                                .padding(6.dp)
                                .background(EmeraldPrimary, CircleShape)
                                .clickable {
                                    launchNativePhoneCamera()
                                }
                                .testTag("camera_shutter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Camera, contentDescription = "Take Photo with Phone Camera", tint = Color.White, modifier = Modifier.size(34.dp))
                        }

                        // Flash or Lighting advice button
                        IconButton(
                            onClick = { flashEnabled = !flashEnabled },
                            modifier = Modifier
                                .background(Slate800, CircleShape)
                                .size(50.dp)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Lighting Guide",
                                tint = if (flashEnabled) AmberWarning else Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // ==================== REVIEW & STRUCTURAL CHECK MODE ====================
                val analysis = structuralAnalysis
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Text(
                        "EVIDENCE QUALITY INSPECTION",
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        selectedAngle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Captured Image View
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Navy900)
                            .border(1.dp, Slate700, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Evidence Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-time Structural Verification Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (analysis?.isValid == true) EmeraldContainer else RedContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (analysis?.isValid == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (analysis?.isValid == true) EmeraldDark else RedCritical
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (analysis?.isValid == true) "Structural Quality Passed" else "Quality Warning",
                                        fontWeight = FontWeight.Bold,
                                        color = if (analysis?.isValid == true) OnEmeraldContainer else RedCritical,
                                        fontSize = 14.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "${analysis?.clarityScore?.toInt() ?: 90}% Clarity",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy800,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (analysis?.isValid == true) {
                                    "No pitch-black darkness, no blur, authentic food perspective. Validated against FSSAI Act statutory evidentiary standards."
                                } else {
                                    analysis?.errorMessage ?: "Photo does not meet structural clarity threshold. Please retake under clear lighting."
                                },
                                color = if (analysis?.isValid == true) OnEmeraldContainer.copy(alpha = 0.9f) else RedCritical,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedBitmap = null
                                structuralAnalysis = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("camera_retake_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake")
                        }

                        Button(
                            onClick = {
                                val item = CapturedPhotoItem(
                                    angleLabel = selectedAngle,
                                    bitmap = capturedBitmap,
                                    clarityScore = analysis?.clarityScore ?: 90f,
                                    brightnessScore = analysis?.brightnessScore ?: 65f,
                                    isPitchBlack = analysis?.pitchBlackDetected ?: false,
                                    isBlurry = analysis?.blurDetected ?: false,
                                    passed = analysis?.isValid ?: true,
                                    statusFeedback = if (analysis?.isValid == true) "Quality Passed" else "Warning Passed"
                                )
                                onPhotoCaptured(item)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("camera_accept_button")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload & Attach Photo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen photo viewer to inspect high-resolution captured evidence photo.
 */
@Composable
fun FullscreenPhotoInspectionDialog(
    photo: CapturedPhotoItem,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            photo.angleLabel,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Clarity: ${photo.clarityScore.toInt()}% • Brightness: ${photo.brightnessScore.toInt()}%",
                            color = EmeraldLight,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Navy900),
                    contentAlignment = Alignment.Center
                ) {
                    if (photo.bitmap != null) {
                        Image(
                            bitmap = photo.bitmap.asImageBitmap(),
                            contentDescription = photo.angleLabel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Official Digital Stamp Evidence", color = Slate300, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Statutory Evidence Authenticated. Metadata locked to GPS & FSSAI Legal Pipeline.",
                            color = Slate300,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
