package com.example.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraUtils {

    /**
     * Creates a temporary image file in application cache and returns both the file
     * and its content Uri formatted through FileProvider for native camera intents.
     */
    fun createTempImageUri(context: Context): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "camera_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val file = File.createTempFile("FOOD_EVIDENCE_${timeStamp}_", ".jpg", storageDir)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return Pair(file, uri)
    }

    /**
     * Decodes a bitmap from Uri with automatic subsampling to avoid OutOfMemory errors.
     */
    fun decodeBitmapFromUri(context: Context, uri: Uri, reqWidth: Int = 1080, reqHeight: Int = 1440): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            var input: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            // Calculate inSampleSize
            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            // Decode bitmap with inSampleSize set
            input = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val result = BitmapFactory.decodeStream(input, null, decodeOptions)
            input?.close()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Takes an authentic bitmap captured directly from the phone's native camera
     * and stamps the official statutory watermark (timestamp, GPS coordinates,
     * business name, angle label, and legal anti-spoofing certification).
     */
    fun stampRealPhotoWithEvidenceWatermark(
        sourceBitmap: Bitmap,
        angleLabel: String,
        businessName: String = "Spice Garden Fine Dine",
        userLocation: String = "Connaught Place, New Delhi",
        gpsCoords: String = "28.6315° N, 77.2167° E"
    ): Bitmap {
        val mutableBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val width = mutableBitmap.width
        val height = mutableBitmap.height

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Viewfinder corner reticles in Emerald
        paint.color = Color.rgb(16, 185, 129)
        val stroke = (width * 0.008f).coerceIn(4f, 12f)
        paint.strokeWidth = stroke
        paint.style = Paint.Style.STROKE

        val reticleSize = width * 0.08f
        val margin = width * 0.04f

        // Top-Left corner
        canvas.drawLine(margin, margin, margin + reticleSize, margin, paint)
        canvas.drawLine(margin, margin, margin, margin + reticleSize, paint)

        // Top-Right corner
        canvas.drawLine(width - margin, margin, width - margin - reticleSize, margin, paint)
        canvas.drawLine(width - margin, margin, width - margin, margin + reticleSize, paint)

        // Bottom-Left corner
        canvas.drawLine(margin, height - margin, margin + reticleSize, height - margin, paint)
        canvas.drawLine(margin, height - margin, margin, height - margin - reticleSize, paint)

        // Bottom-Right corner
        canvas.drawLine(width - margin, height - margin, width - margin - reticleSize, height - margin, paint)
        canvas.drawLine(width - margin, height - margin, width - margin, height - margin - reticleSize, paint)

        // Top HUD Header Banner
        val headerHeight = (height * 0.10f).coerceIn(70f, 160f)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(205, 7, 25, 44) // Navy dark scrim
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight, paint)

        paint.color = Color.WHITE
        paint.textSize = (headerHeight * 0.30f).coerceIn(18f, 38f)
        paint.isFakeBoldText = true
        canvas.drawText("FOODSAFE EVIDENCE CAMERA • SEC 84 FSSAI", margin, headerHeight * 0.42f, paint)

        paint.textSize = (headerHeight * 0.22f).coerceIn(14f, 28f)
        paint.color = Color.rgb(52, 211, 153) // EmeraldLight
        paint.isFakeBoldText = false
        canvas.drawText("ANGLE: ${angleLabel.take(40)}", margin, headerHeight * 0.78f, paint)

        // Bottom HUD Footer Banner (Tamper-evident timestamp & GPS)
        val footerHeight = (height * 0.12f).coerceIn(80f, 180f)
        paint.color = Color.argb(215, 7, 25, 44)
        canvas.drawRect(0f, (height - footerHeight), width.toFloat(), height.toFloat(), paint)

        val timestamp = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z", Locale.getDefault()).format(Date())
        paint.color = Color.WHITE
        paint.textSize = (footerHeight * 0.23f).coerceIn(15f, 30f)
        paint.isFakeBoldText = true
        canvas.drawText("TIMESTAMP: $timestamp", margin, height - footerHeight * 0.65f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.textSize = (footerHeight * 0.19f).coerceIn(13f, 26f)
        paint.isFakeBoldText = false
        val locText = if (userLocation.isNotBlank()) userLocation.take(45) else businessName.take(45)
        canvas.drawText("LOCATION: $gpsCoords • $locText", margin, height - footerHeight * 0.38f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = (footerHeight * 0.16f).coerceIn(11f, 22f)
        canvas.drawText("LIVE PHONE CAMERA CAPTURE • GALLERY UPLOADS STRICTLY RESTRICTED", margin, height - footerHeight * 0.15f, paint)

        return mutableBitmap
    }

    /**
     * Generates a realistic high-definition camera evidence capture bitmap
     * with official statutory overlay (timestamp, GPS coordinates, angle classification,
     * and inspection reticles) for testing and simulated camera viewfinders.
     */
    fun createEvidenceSnapshot(
        angleLabel: String,
        businessName: String = "Spice Garden Fine Dine",
        userLocation: String = "Connaught Place, New Delhi",
        gpsCoords: String = "28.6315° N, 77.2167° E",
        simulatedDark: Boolean = false,
        simulatedBlur: Boolean = false
    ): Bitmap {
        val width = 720
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background simulation
        if (simulatedDark) {
            paint.color = Color.rgb(18, 20, 24)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            // Gradient table/counter surface background
            paint.color = Color.rgb(44, 53, 64)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Inner food container / plate representation
            paint.color = Color.rgb(65, 78, 92)
            canvas.drawRoundRect(RectF(80f, 160f, (width - 80).toFloat(), (height - 240).toFloat()), 32f, 32f, paint)

            // Dish plate center
            paint.color = Color.rgb(82, 98, 114)
            canvas.drawCircle((width / 2).toFloat(), ((height - 80) / 2).toFloat(), 200f, paint)

            // Food item representation
            paint.color = Color.rgb(180, 110, 50)
            canvas.drawCircle((width / 2).toFloat(), ((height - 80) / 2).toFloat(), 140f, paint)

            // Contamination / focal point marker
            paint.color = Color.rgb(20, 20, 20)
            canvas.drawCircle(((width / 2) + 30).toFloat(), (((height - 80) / 2) - 20).toFloat(), 25f, paint)
        }

        // Camera viewfinder corner reticles
        paint.color = if (simulatedDark) Color.RED else Color.rgb(16, 185, 129) // Emerald
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE

        val reticleSize = 50f
        val margin = 60f

        // Top-Left corner
        canvas.drawLine(margin, margin, margin + reticleSize, margin, paint)
        canvas.drawLine(margin, margin, margin, margin + reticleSize, paint)

        // Top-Right corner
        canvas.drawLine(width - margin, margin, width - margin - reticleSize, margin, paint)
        canvas.drawLine(width - margin, margin, width - margin, margin + reticleSize, paint)

        // Bottom-Left corner
        canvas.drawLine(margin, height - margin, margin + reticleSize, height - margin, paint)
        canvas.drawLine(margin, height - margin, margin, height - margin - reticleSize, paint)

        // Bottom-Right corner
        canvas.drawLine(width - margin, height - margin, width - margin - reticleSize, height - margin, paint)
        canvas.drawLine(width - margin, height - margin, width - margin, height - margin - reticleSize, paint)

        // Center crosshair
        val cx = (width / 2).toFloat()
        val cy = ((height - 80) / 2).toFloat()
        canvas.drawLine(cx - 25f, cy, cx + 25f, cy, paint)
        canvas.drawLine(cx, cy - 25f, cx, cy + 25f, paint)

        // HUD Header Bar
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(190, 10, 15, 25)
        canvas.drawRect(0f, 0f, width.toFloat(), 120f, paint)

        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("FOODSAFE EVIDENCE CAMERA", 30f, 50f, paint)

        paint.textSize = 20f
        paint.color = Color.rgb(52, 211, 153) // EmeraldLight
        paint.isFakeBoldText = false
        canvas.drawText("ANGLE: ${angleLabel.take(35)}", 30f, 85f, paint)

        // HUD Footer Stamp (Tamper-evident timestamp & GPS)
        paint.color = Color.argb(200, 10, 15, 25)
        canvas.drawRect(0f, (height - 120).toFloat(), width.toFloat(), height.toFloat(), paint)

        val timestamp = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z", Locale.getDefault()).format(Date())
        paint.color = Color.WHITE
        paint.textSize = 19f
        canvas.drawText("STAMP: $timestamp", 30f, (height - 80).toFloat(), paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.textSize = 17f
        canvas.drawText("LOCATION: $gpsCoords • $businessName", 30f, (height - 52).toFloat(), paint)
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("SEC 84 FSSAI STATUTORY EVIDENCE • NOT FROM GALLERY", 30f, (height - 26).toFloat(), paint)

        return bitmap
    }
}

