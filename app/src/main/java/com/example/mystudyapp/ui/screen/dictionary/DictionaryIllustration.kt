package com.example.mystudyapp.ui.screen.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * High-fidelity 2D vector illustrations for vocabulary items
 * Scalable to any size without raster pixelation.
 */
@Composable
fun DictionaryIllustration(
    word: String,
    modifier: Modifier = Modifier
) {
    val clean = word.trim().lowercase()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (clean) {
                "apple" -> {
                    // 1. Apple Body (Two overlapping lobes for cute organic apple shape)
                    val appleRedTop = Color(0xFFEF4444)
                    val appleRedBottom = Color(0xFFDC2626)
                    val brush = Brush.verticalGradient(
                        colors = listOf(appleRedTop, appleRedBottom),
                        startY = h * 0.2f,
                        endY = h * 0.9f
                    )

                    val bodyPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.32f)
                        cubicTo(w * 0.38f, h * 0.18f, w * 0.08f, h * 0.28f, w * 0.12f, h * 0.56f)
                        cubicTo(w * 0.14f, h * 0.78f, w * 0.32f, h * 0.92f, w * 0.5f, h * 0.90f)
                        cubicTo(w * 0.68f, h * 0.92f, w * 0.86f, h * 0.78f, w * 0.88f, h * 0.56f)
                        cubicTo(w * 0.92f, h * 0.28f, w * 0.62f, h * 0.18f, w * 0.5f, h * 0.32f)
                        close()
                    }
                    drawPath(bodyPath, brush = brush)

                    // Apple glossy highlight
                    val highlightPath = Path().apply {
                        moveTo(w * 0.24f, h * 0.40f)
                        cubicTo(w * 0.20f, h * 0.50f, w * 0.22f, h * 0.64f, w * 0.28f, h * 0.72f)
                    }
                    drawPath(
                        highlightPath,
                        color = Color.White.copy(alpha = 0.45f),
                        style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
                    )

                    // Brown Stem
                    val stemPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.34f)
                        cubicTo(w * 0.51f, h * 0.20f, w * 0.56f, h * 0.14f, w * 0.62f, h * 0.10f)
                    }
                    drawPath(
                        stemPath,
                        color = Color(0xFF78350F),
                        style = Stroke(width = w * 0.065f, cap = StrokeCap.Round)
                    )

                    // Green Leaf
                    val leafPath = Path().apply {
                        moveTo(w * 0.52f, h * 0.24f)
                        cubicTo(w * 0.60f, h * 0.12f, w * 0.78f, h * 0.15f, w * 0.82f, h * 0.22f)
                        cubicTo(w * 0.75f, h * 0.32f, w * 0.60f, h * 0.30f, w * 0.52f, h * 0.24f)
                        close()
                    }
                    drawPath(leafPath, color = Color(0xFF22C55E))
                }

                "house" -> {
                    // House Wall
                    val wallRect = Size(w * 0.64f, h * 0.46f)
                    drawRoundRect(
                        color = Color(0xFFFEF3C7),
                        topLeft = Offset(w * 0.18f, h * 0.44f),
                        size = wallRect,
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )

                    // Roof (Red/Orange triangle)
                    val roofPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.16f)
                        lineTo(w * 0.12f, h * 0.46f)
                        lineTo(w * 0.88f, h * 0.46f)
                        close()
                    }
                    drawPath(roofPath, color = Color(0xFFEA580C))

                    // Door (Brown)
                    drawRoundRect(
                        color = Color(0xFF92400E),
                        topLeft = Offset(w * 0.42f, h * 0.62f),
                        size = Size(w * 0.16f, h * 0.28f),
                        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
                    )

                    // Window (Sky blue with white frame)
                    drawRoundRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.24f, h * 0.52f),
                        size = Size(w * 0.14f, h * 0.14f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )
                    drawRoundRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.62f, h * 0.52f),
                        size = Size(w * 0.14f, h * 0.14f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )
                }

                "cat" -> {
                    // Cat Head (Warm orange)
                    val headCenter = Offset(w * 0.5f, h * 0.56f)
                    val headRadius = w * 0.34f

                    // Left Ear
                    val leftEar = Path().apply {
                        moveTo(w * 0.24f, h * 0.45f)
                        lineTo(w * 0.18f, h * 0.20f)
                        lineTo(w * 0.42f, h * 0.32f)
                        close()
                    }
                    drawPath(leftEar, color = Color(0xFFFB923C))

                    // Right Ear
                    val rightEar = Path().apply {
                        moveTo(w * 0.76f, h * 0.45f)
                        lineTo(w * 0.82f, h * 0.20f)
                        lineTo(w * 0.58f, h * 0.32f)
                        close()
                    }
                    drawPath(rightEar, color = Color(0xFFFB923C))

                    // Head Circle
                    drawCircle(color = Color(0xFFF97316), radius = headRadius, center = headCenter)

                    // Eyes
                    drawCircle(color = Color(0xFF1E293B), radius = w * 0.045f, center = Offset(w * 0.38f, h * 0.52f))
                    drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.39f, h * 0.51f))
                    drawCircle(color = Color(0xFF1E293B), radius = w * 0.045f, center = Offset(w * 0.62f, h * 0.52f))
                    drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.63f, h * 0.51f))

                    // Pink Nose
                    val nose = Path().apply {
                        moveTo(w * 0.5f, h * 0.61f)
                        lineTo(w * 0.46f, h * 0.58f)
                        lineTo(w * 0.54f, h * 0.58f)
                        close()
                    }
                    drawPath(nose, color = Color(0xFFF472B6))

                    // Whiskers
                    drawLine(Color(0xFFEA580C), Offset(w * 0.20f, h * 0.58f), Offset(w * 0.34f, h * 0.60f), strokeWidth = w * 0.02f)
                    drawLine(Color(0xFFEA580C), Offset(w * 0.80f, h * 0.58f), Offset(w * 0.66f, h * 0.60f), strokeWidth = w * 0.02f)
                }

                "dog" -> {
                    // Dog Head (Golden brown)
                    val headCenter = Offset(w * 0.5f, h * 0.52f)

                    // Left Ear
                    val leftEar = Path().apply {
                        moveTo(w * 0.22f, h * 0.36f)
                        cubicTo(w * 0.10f, h * 0.44f, w * 0.12f, h * 0.72f, w * 0.26f, h * 0.68f)
                        close()
                    }
                    drawPath(leftEar, color = Color(0xFF9A3412))

                    // Right Ear
                    val rightEar = Path().apply {
                        moveTo(w * 0.78f, h * 0.36f)
                        cubicTo(w * 0.90f, h * 0.44f, w * 0.88f, h * 0.72f, w * 0.74f, h * 0.68f)
                        close()
                    }
                    drawPath(rightEar, color = Color(0xFF9A3412))

                    // Head Circle
                    drawCircle(color = Color(0xFFD97706), radius = w * 0.32f, center = headCenter)

                    // Snout Circle
                    drawCircle(color = Color(0xFFFEF3C7), radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.62f))

                    // Eyes
                    drawCircle(color = Color(0xFF1E293B), radius = w * 0.045f, center = Offset(w * 0.38f, h * 0.46f))
                    drawCircle(color = Color(0xFF1E293B), radius = w * 0.045f, center = Offset(w * 0.62f, h * 0.46f))

                    // Nose
                    drawCircle(color = Color(0xFF1E293B), radius = w * 0.045f, center = Offset(w * 0.5f, h * 0.59f))

                    // Tongue
                    val tongue = Path().apply {
                        moveTo(w * 0.47f, h * 0.68f)
                        cubicTo(w * 0.46f, h * 0.76f, w * 0.54f, h * 0.76f, w * 0.53f, h * 0.68f)
                        close()
                    }
                    drawPath(tongue, color = Color(0xFFF43F5E))
                }

                "sun" -> {
                    val sunCenter = Offset(w * 0.5f, h * 0.5f)
                    val sunRadius = w * 0.25f

                    // Rays
                    val rayCount = 8
                    for (i in 0 until rayCount) {
                        val angle = (i * (360f / rayCount)) * (Math.PI / 180f)
                        val startX = sunCenter.x + (sunRadius + w * 0.06f) * Math.cos(angle).toFloat()
                        val startY = sunCenter.y + (sunRadius + w * 0.06f) * Math.sin(angle).toFloat()
                        val endX = sunCenter.x + (sunRadius + w * 0.18f) * Math.cos(angle).toFloat()
                        val endY = sunCenter.y + (sunRadius + w * 0.18f) * Math.sin(angle).toFloat()

                        drawLine(
                            color = Color(0xFFF59E0B),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = w * 0.06f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Sun Circle
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B))),
                        radius = sunRadius,
                        center = sunCenter
                    )
                }

                "tree" -> {
                    // Trunk
                    drawRoundRect(
                        color = Color(0xFF78350F),
                        topLeft = Offset(w * 0.43f, h * 0.52f),
                        size = Size(w * 0.14f, h * 0.40f),
                        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
                    )

                    // Foliage (Tiered green circles)
                    drawCircle(color = Color(0xFF16A34A), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.42f))
                    drawCircle(color = Color(0xFF22C55E), radius = w * 0.22f, center = Offset(w * 0.40f, h * 0.35f))
                    drawCircle(color = Color(0xFF4ADE80), radius = w * 0.20f, center = Offset(w * 0.60f, h * 0.33f))
                    drawCircle(color = Color(0xFF15803D), radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.25f))
                }

                "book" -> {
                    // Blue Book Body
                    val bookPath = Path().apply {
                        moveTo(w * 0.22f, h * 0.24f)
                        lineTo(w * 0.78f, h * 0.18f)
                        lineTo(w * 0.78f, h * 0.76f)
                        lineTo(w * 0.22f, h * 0.82f)
                        close()
                    }
                    drawPath(bookPath, color = Color(0xFF2563EB))

                    // Book Pages (White)
                    val pagesPath = Path().apply {
                        moveTo(w * 0.26f, h * 0.28f)
                        lineTo(w * 0.74f, h * 0.23f)
                        lineTo(w * 0.74f, h * 0.72f)
                        lineTo(w * 0.26f, h * 0.77f)
                        close()
                    }
                    drawPath(pagesPath, color = Color.White)

                    // Bookmark Ribbon (Red)
                    val ribbonPath = Path().apply {
                        moveTo(w * 0.48f, h * 0.24f)
                        lineTo(w * 0.56f, h * 0.23f)
                        lineTo(w * 0.56f, h * 0.54f)
                        lineTo(w * 0.52f, h * 0.48f)
                        lineTo(w * 0.48f, h * 0.54f)
                        close()
                    }
                    drawPath(ribbonPath, color = Color(0xFFEF4444))
                }

                "school" -> {
                    // School Bag (Bright Blue with orange accents)
                    val bagRect = Size(w * 0.60f, h * 0.52f)
                    drawRoundRect(
                        color = Color(0xFF0284C7),
                        topLeft = Offset(w * 0.20f, h * 0.36f),
                        size = bagRect,
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                    )

                    // Front Pocket (Orange)
                    drawRoundRect(
                        color = Color(0xFFF97316),
                        topLeft = Offset(w * 0.28f, h * 0.56f),
                        size = Size(w * 0.44f, h * 0.24f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )

                    // Handle
                    val handlePath = Path().apply {
                        moveTo(w * 0.36f, h * 0.36f)
                        cubicTo(w * 0.36f, h * 0.20f, w * 0.64f, h * 0.20f, w * 0.64f, h * 0.36f)
                    }
                    drawPath(
                        handlePath,
                        color = Color(0xFF0369A1),
                        style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
                    )
                }

                else -> {
                    // Fallback: Elegant book with initial letter
                    val defaultBook = Path().apply {
                        moveTo(w * 0.20f, h * 0.22f)
                        lineTo(w * 0.80f, h * 0.18f)
                        lineTo(w * 0.80f, h * 0.78f)
                        lineTo(w * 0.20f, h * 0.82f)
                        close()
                    }
                    drawPath(defaultBook, color = Color(0xFF3B82F6))

                    val defaultPages = Path().apply {
                        moveTo(w * 0.25f, h * 0.27f)
                        lineTo(w * 0.75f, h * 0.23f)
                        lineTo(w * 0.75f, h * 0.73f)
                        lineTo(w * 0.25f, h * 0.77f)
                        close()
                    }
                    drawPath(defaultPages, color = Color.White)

                    // Decorative bookmark
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(w * 0.48f, h * 0.25f),
                        end = Offset(w * 0.48f, h * 0.60f),
                        strokeWidth = w * 0.05f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
