package com.qrforge.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrGenerator @Inject constructor() {

    suspend fun generateBitmap(
        content: String,
        fgColor: Int = 0xFF000000.toInt(),
        bgColor: Int = 0xFFFFFFFF.toInt(),
        dotStyle: String = "ROUNDED",
        eyeShape: String = "MODERN",
        frameStyle: String = "NONE",
        logoPath: String? = null,
        gradientType: String = "NONE",
        eyeColor: Int? = null,
        frameColor: Int? = null,
        size: Int = 1024
    ): Bitmap = withContext(Dispatchers.Default) {
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        // Force H (High) error correction for all templates to maximize scanning redundancy
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        // Generate with 0 margin from ZXing, we will manually render the quiet zone margin of 4 modules
        hints[EncodeHintType.MARGIN] = 0

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)

        val gridSize = bitMatrix.width
        // Total grid size includes a 4-module quiet zone on all 4 sides (gridSize + 8)
        val totalGridSize = gridSize + 8

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Calculate layout coordinates
        val hasFrame = frameStyle != "NONE"
        // Shrink active QR area slightly if it has a frame to draw it outside the quiet zone
        val qrAreaSize = if (hasFrame) size * 0.74f else size * 0.88f
        val qrOffset = (size - qrAreaSize) / 2f
        val moduleSize = qrAreaSize / totalGridSize

        // Paint configurations
        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        if (gradientType == "LINEAR") {
            fgPaint.shader = LinearGradient(
                qrOffset, qrOffset, qrOffset + qrAreaSize, qrOffset + qrAreaSize,
                fgColor, getGradientEndColor(fgColor),
                Shader.TileMode.CLAMP
            )
        } else if (gradientType == "RADIAL") {
            fgPaint.shader = RadialGradient(
                size / 2f, size / 2f, qrAreaSize / 1.1f,
                fgColor, getGradientEndColor(fgColor),
                Shader.TileMode.CLAMP
            )
        } else {
            fgPaint.color = fgColor
        }

        val finalEyeColor = eyeColor ?: fgColor
        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalEyeColor
            style = Paint.Style.FILL
        }

        // Calculate Center Logo clearing box in modules
        val centerCol = gridSize / 2
        val centerRow = gridSize / 2
        // Logo size in modules should be around 16% of gridSize (max 20% to keep alignment patterns safe)
        val logoModules = (gridSize * 0.16f).toInt().coerceAtLeast(3)
        val startLogoCol = centerCol - logoModules / 2
        val endLogoCol = startLogoCol + logoModules - 1
        val startLogoRow = centerRow - logoModules / 2
        val endLogoRow = startLogoRow + logoModules - 1

        // Draw active modules (shifted by 4 modules to preserve 4-module quiet zone)
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                // Skip finder pattern areas in the bitMatrix (always 0..6 bounds)
                val isFinder = (col in 0..6 && row in 0..6) ||
                        (col >= gridSize - 7 && row in 0..6) ||
                        (col in 0..6 && row >= gridSize - 7)
                if (isFinder) continue

                // Skip logo cutout area if logo is enabled
                if (logoPath != null) {
                    if (col in startLogoCol..endLogoCol && row in startLogoRow..endLogoRow) {
                        continue
                    }
                }

                if (bitMatrix.get(col, row)) {
                    val left = qrOffset + (col + 4) * moduleSize
                    val top = qrOffset + (row + 4) * moduleSize
                    val right = left + moduleSize
                    val bottom = top + moduleSize

                    when (dotStyle.uppercase()) {
                        "ROUNDED" -> {
                            val r = moduleSize * 0.35f
                            canvas.drawRoundRect(left + 0.5f, top + 0.5f, right - 0.5f, bottom - 0.5f, r, r, fgPaint)
                        }
                        "CIRCULAR" -> {
                            // Large circular modules to keep dots touching/scannable (90% size)
                            canvas.drawCircle(left + moduleSize / 2f, top + moduleSize / 2f, moduleSize * 0.45f, fgPaint)
                        }
                        "DIAMOND" -> {
                            // Diamonds covering 90% of module space
                            val path = Path().apply {
                                moveTo(left + moduleSize / 2f, top + 0.5f)
                                lineTo(right - 0.5f, top + moduleSize / 2f)
                                lineTo(left + moduleSize / 2f, bottom - 0.5f)
                                lineTo(left + 0.5f, top + moduleSize / 2f)
                                close()
                            }
                            canvas.drawPath(path, fgPaint)
                        }
                        else -> { // SQUARE
                            canvas.drawRect(left, top, right, bottom, fgPaint)
                        }
                    }
                }
            }
        }

        // Helper to draw a standard-compliant Finder Pattern (Eye)
        val eyeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        fun drawFinder(fCol: Float, fRow: Float) {
            // Shifted by 4 modules to align with the manually padded grid
            val left = qrOffset + (fCol + 4f) * moduleSize
            val top = qrOffset + (fRow + 4f) * moduleSize
            val outerSize = 7 * moduleSize
            val innerBgOffset = 1 * moduleSize
            val innerBgSize = 5 * moduleSize
            val eyeballOffset = 2 * moduleSize
            val eyeballSize = 3 * moduleSize

            // Outer frame corner radius must be kept safe (rounded squares) to ensure scanner detection
            when (eyeShape.uppercase()) {
                "ROUNDED" -> {
                    val rOuter = outerSize * 0.22f
                    canvas.drawRoundRect(RectF(left, top, left + outerSize, top + outerSize), rOuter, rOuter, eyePaint)
                    val rInnerBg = innerBgSize * 0.22f
                    canvas.drawRoundRect(RectF(left + innerBgOffset, top + innerBgOffset, left + innerBgOffset + innerBgSize, top + innerBgOffset + innerBgSize), rInnerBg, rInnerBg, eyeBgPaint)
                    val rEye = eyeballSize * 0.22f
                    canvas.drawRoundRect(RectF(left + eyeballOffset, top + eyeballOffset, left + eyeballOffset + eyeballSize, top + eyeballOffset + eyeballSize), rEye, rEye, eyePaint)
                }
                "CIRCLE" -> {
                    // Rounded outer frame + Circular eyeball (compliant & scannable)
                    val rOuter = outerSize * 0.35f
                    canvas.drawRoundRect(RectF(left, top, left + outerSize, top + outerSize), rOuter, rOuter, eyePaint)
                    val rInnerBg = innerBgSize * 0.35f
                    canvas.drawRoundRect(RectF(left + innerBgOffset, top + innerBgOffset, left + innerBgOffset + innerBgSize, top + innerBgOffset + innerBgSize), rInnerBg, rInnerBg, eyeBgPaint)
                    canvas.drawCircle(left + outerSize/2f, top + outerSize/2f, eyeballSize/2f, eyePaint)
                }
                "MODERN" -> {
                    // Squirclish outer frame + Circular eyeball
                    val rOuter = outerSize * 0.25f
                    canvas.drawRoundRect(RectF(left, top, left + outerSize, top + outerSize), rOuter, rOuter, eyePaint)
                    val rInnerBg = innerBgSize * 0.25f
                    canvas.drawRoundRect(RectF(left + innerBgOffset, top + innerBgOffset, left + innerBgOffset + innerBgSize, top + innerBgOffset + innerBgSize), rInnerBg, rInnerBg, eyeBgPaint)
                    canvas.drawCircle(left + outerSize/2f, top + outerSize/2f, eyeballSize/2f, eyePaint)
                }
                else -> { // SQUARE
                    canvas.drawRect(left, top, left + outerSize, top + outerSize, eyePaint)
                    canvas.drawRect(left + innerBgOffset, top + innerBgOffset, left + innerBgOffset + innerBgSize, top + innerBgOffset + innerBgSize, eyeBgPaint)
                    canvas.drawRect(left + eyeballOffset, top + eyeballOffset, left + eyeballOffset + eyeballSize, top + eyeballOffset + eyeballSize, eyePaint)
                }
            }
        }

        // Draw the three corner markers at their exact padded coordinates
        drawFinder(0f, 0f)
        drawFinder(gridSize - 7f, 0f)
        drawFinder(0f, gridSize - 7f)

        // Draw Center Logo
        if (logoPath != null) {
            // Keep logo box size exactly to 16% of the QR code area
            val logoBoxSize = qrAreaSize * (logoModules.toFloat() / totalGridSize)
            val cx = size / 2f
            val cy = size / 2f
            val l = cx - logoBoxSize / 2f
            val t = cy - logoBoxSize / 2f
            val r = cx + logoBoxSize / 2f
            val b = cy + logoBoxSize / 2f

            // Clear background area behind logo
            val logoBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(l - 6f, t - 6f, r + 6f, b + 6f, 16f, 16f, logoBgPaint)

            // Draw logo graphic
            if (logoPath.startsWith("/") && File(logoPath).exists()) {
                val logoBmp = android.graphics.BitmapFactory.decodeFile(logoPath)
                if (logoBmp != null) {
                    canvas.drawBitmap(logoBmp, null, RectF(l, t, r, b), null)
                }
            } else {
                val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = fgColor
                    style = Paint.Style.FILL
                }
                drawVectorLogo(canvas, logoPath, l, t, r, b, logoPaint)
            }
        }

        // Draw Frame Layout (fully outside the 4-module quiet zone boundary)
        if (hasFrame) {
            val finalFrameColor = frameColor ?: fgColor
            val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = finalFrameColor
                style = Paint.Style.STROKE
            }
            drawFrame(canvas, frameStyle, qrOffset, qrAreaSize, size, finalFrameColor, bgColor, framePaint)
        }

        bitmap
    }

    private fun getGradientEndColor(startColor: Int): Int {
        val r = (startColor shr 16) and 0xFF
        val g = (startColor shr 8) and 0xFF
        val b = startColor and 0xFF
        // Smooth gradient calculation
        val nr = (r * 0.4f + 0x8B * 0.6f).toInt().coerceIn(0, 255)
        val ng = (g * 0.4f + 0x5C * 0.6f).toInt().coerceIn(0, 255)
        val nb = (b * 0.4f + 0xF6 * 0.6f).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (nr shl 16) or (ng shl 8) or nb
    }

    private fun drawVectorLogo(canvas: Canvas, logo: String, l: Float, t: Float, r: Float, b: Float, paint: Paint) {
        val w = r - l
        val h = b - t
        val cx = l + w / 2f
        val cy = t + h / 2f

        when (logo.uppercase()) {
            "LINK", "URL" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.08f
                paint.strokeCap = Paint.Cap.ROUND
                val path = Path().apply {
                    moveTo(cx - w*0.15f, cy - h*0.15f)
                    lineTo(cx + w*0.15f, cy + h*0.15f)
                }
                canvas.drawPath(path, paint)
                canvas.drawCircle(cx - w*0.16f, cy - h*0.16f, w*0.12f, paint)
                canvas.drawCircle(cx + w*0.16f, cy + h*0.16f, w*0.12f, paint)
            }
            "WIFI" -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy + h*0.22f, w*0.08f, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.07f
                paint.strokeCap = Paint.Cap.ROUND
                
                val path1 = Path().apply {
                    addArc(RectF(cx - w*0.28f, cy - h*0.1f, cx + w*0.28f, cy + h*0.4f), -135f, 90f)
                }
                canvas.drawPath(path1, paint)

                val path2 = Path().apply {
                    addArc(RectF(cx - w*0.48f, cy - h*0.3f, cx + w*0.48f, cy + h*0.6f), -140f, 100f)
                }
                canvas.drawPath(path2, paint)
            }
            "INSTAGRAM" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = w * 0.08f
                canvas.drawRoundRect(RectF(cx - w*0.3f, cy - h*0.3f, cx + w*0.3f, cy + h*0.3f), w*0.15f, w*0.15f, paint)
                canvas.drawCircle(cx, cy, w*0.14f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(cx + w*0.18f, cy - h*0.18f, w*0.045f, paint)
            }
            "WHATSAPP" -> {
                paint.style = Paint.Style.FILL
                val path = Path().apply {
                    moveTo(cx - w*0.3f, cy - h*0.25f)
                    cubicTo(cx - w*0.4f, cy - h*0.1f, cx - w*0.4f, cy + h*0.1f, cx - w*0.3f, cy + h*0.25f)
                    lineTo(cx - w*0.35f, cy + h*0.35f)
                    lineTo(cx - w*0.22f, cy + h*0.28f)
                    cubicTo(cx - w*0.1f, cy + h*0.35f, cx + w*0.1f, cy + h*0.35f, cx + w*0.3f, cy + h*0.25f)
                    cubicTo(cx + w*0.4f, cy + h*0.1f, cx + w*0.4f, cy - h*0.1f, cx + w*0.3f, cy - h*0.25f)
                    cubicTo(cx + w*0.2f, cy - h*0.35f, cx - w*0.2f, cy - h*0.35f, cx - w*0.3f, cy - h*0.25f)
                    close()
                }
                canvas.drawPath(path, paint)
                val innerPaint = Paint(paint).apply { color = Color.WHITE }
                canvas.drawCircle(cx, cy, w*0.1f, innerPaint)
            }
            "YOUTUBE" -> {
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(RectF(cx - w*0.36f, cy - h*0.24f, cx + w*0.36f, cy + h*0.24f), w*0.12f, w*0.12f, paint)
                val triPaint = Paint(paint).apply { color = Color.WHITE }
                val path = Path().apply {
                    moveTo(cx - w*0.08f, cy - h*0.12f)
                    lineTo(cx + w*0.14f, cy)
                    lineTo(cx - w*0.08f, cy + h*0.12f)
                    close()
                }
                canvas.drawPath(path, triPaint)
            }
            else -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy, w * 0.2f, paint)
            }
        }
    }

    private fun drawFrame(
        canvas: Canvas,
        frameStyle: String,
        qrOffset: Float,
        qrAreaSize: Float,
        size: Int,
        frameColor: Int,
        bgColor: Int,
        paint: Paint
    ) {
        val s = size.toFloat()
        paint.style = Paint.Style.STROKE
        
        // Frames are pushed out by pad to remain strictly outside the 4-module quiet zone
        when (frameStyle.uppercase()) {
            "THIN" -> {
                paint.strokeWidth = s * 0.008f
                val pad = s * 0.015f
                canvas.drawRect(
                    qrOffset - pad, qrOffset - pad,
                    qrOffset + qrAreaSize + pad, qrOffset + qrAreaSize + pad,
                    paint
                )
            }
            "THICK" -> {
                paint.strokeWidth = s * 0.024f
                val pad = s * 0.02f
                canvas.drawRect(
                    qrOffset - pad, qrOffset - pad,
                    qrOffset + qrAreaSize + pad, qrOffset + qrAreaSize + pad,
                    paint
                )
            }
            "ROUNDED" -> {
                paint.strokeWidth = s * 0.012f
                val pad = s * 0.018f
                val r = s * 0.04f
                canvas.drawRoundRect(
                    RectF(qrOffset - pad, qrOffset - pad, qrOffset + qrAreaSize + pad, qrOffset + qrAreaSize + pad),
                    r, r,
                    paint
                )
            }
            "BADGE" -> {
                val pad = s * 0.018f
                val frameLeft = qrOffset - pad
                val frameTop = qrOffset - pad
                val frameRight = qrOffset + qrAreaSize + pad
                val frameBottom = s - pad * 1.5f

                paint.strokeWidth = s * 0.012f
                val r = s * 0.04f
                canvas.drawRoundRect(
                    RectF(frameLeft, frameTop, frameRight, frameBottom),
                    r, r,
                    paint
                )

                // Fill capsule base
                val capWidth = s * 0.38f
                val capHeight = s * 0.065f
                val capLeft = (s - capWidth) / 2f
                val capTop = frameBottom - capHeight / 2f - s * 0.01f
                val capRight = capLeft + capWidth
                val capBottom = capTop + capHeight

                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = frameColor
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    RectF(capLeft, capTop, capRight, capBottom),
                    capHeight / 2f, capHeight / 2f,
                    fillPaint
                )

                // Draw white SCAN ME text
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgColor
                    textSize = capHeight * 0.45f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val textY = capTop + capHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText("SCAN ME", s / 2f, textY, textPaint)
            }
            "STICKER" -> {
                paint.strokeWidth = s * 0.035f
                val pad = s * 0.01f
                val r = s * 0.05f
                
                // Draw sticker white base with drop shadow
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgColor
                    style = Paint.Style.FILL
                    setShadowLayer(16f, 0f, 8f, 0x3F000000)
                }
                canvas.drawRoundRect(
                    RectF(qrOffset - pad, qrOffset - pad, qrOffset + qrAreaSize + pad, qrOffset + qrAreaSize + pad),
                    r, r,
                    fillPaint
                )
                
                // Draw frame border
                canvas.drawRoundRect(
                    RectF(qrOffset - pad, qrOffset - pad, qrOffset + qrAreaSize + pad, qrOffset + qrAreaSize + pad),
                    r, r,
                    paint
                )
            }
        }
    }

    suspend fun exportPng(bitmap: Bitmap, outputFile: File): File = withContext(Dispatchers.IO) {
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        outputFile
    }

    fun clearExportMessage() {}
}
