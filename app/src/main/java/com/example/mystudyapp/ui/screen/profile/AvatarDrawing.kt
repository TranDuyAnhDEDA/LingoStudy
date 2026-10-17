package com.example.mystudyapp.ui.screen.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

data class AvatarOption(
    val id: String,
    val name: String,
    val description: String
)

val AVAILABLE_AVATARS = listOf(
    AvatarOption("boy_hoodie", "Minh Anh", "Cậu bé áo hoodie năng động"),
    AvatarOption("girl_glasses", "Bảo An", "Cô bé đeo kính chăm chỉ"),
    AvatarOption("boy_sport", "Hải Đăng", "Cậu bé thể thao vui tính"),
    AvatarOption("girl_ribbon", "Tuệ Lâm", "Cô bé cài ruy băng dễ thương"),
    AvatarOption("parrot_lingo", "Vẹt Lingo", "Mascot LingoStudy đáng yêu"),
    AvatarOption("scholar_bear", "Gấu Chăm Chỉ", "Bạn gấu cử nhân tri thức")
)

object AvatarManager {
    fun copyUriToInternalStorage(context: Context, uid: String, uri: Uri): String? {
        return try {
            val destDir = File(context.filesDir, "avatars")
            if (!destDir.exists()) destDir.mkdirs()
            val destFile = File(destDir, "avatar_${uid}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun LingoAvatarWithBadge(
    avatarId: String,
    customAvatarPath: String? = null,
    size: Dp = 88.dp,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Main circular avatar container with white border
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE))
                .border(3.dp, Color.White, CircleShape)
                .clickable { onEditClick() }
        ) {
            LingoAvatarDisplay(
                avatarId = avatarId,
                customAvatarPath = customAvatarPath,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Camera badge at bottom-right corner (matches reference image)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(size * 0.32f)
                .clip(CircleShape)
                .background(Color(0xFF2563EB))
                .border(2.dp, Color.White, CircleShape)
                .clickable { onEditClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Đổi ảnh đại diện",
                tint = Color.White,
                modifier = Modifier.size(size * 0.18f)
            )
        }
    }
}

@Composable
fun LingoAvatarDisplay(
    avatarId: String,
    customAvatarPath: String? = null,
    modifier: Modifier = Modifier
) {
    val customBitmap = remember(customAvatarPath) {
        if (!customAvatarPath.isNullOrBlank()) {
            try {
                val file = File(customAvatarPath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap,
            contentDescription = "Ảnh đại diện",
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
    } else {
        LingoAvatarCanvas(
            avatarId = avatarId,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
fun LingoAvatarCanvas(
    avatarId: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (avatarId) {
            "girl_glasses" -> drawGirlGlassesAvatar(w, h)
            "boy_sport" -> drawBoySportAvatar(w, h)
            "girl_ribbon" -> drawGirlRibbonAvatar(w, h)
            "parrot_lingo" -> drawParrotMascotAvatar(w, h)
            "scholar_bear" -> drawScholarBearAvatar(w, h)
            else -> drawBoyHoodieAvatar(w, h) // Default 1:1 match with user reference
        }
    }
}

// 1. Boy with Blue Hoodie (Faithful to Reference Image)
private fun DrawScope.drawBoyHoodieAvatar(w: Float, h: Float) {
    // Background glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = w * 0.5f
        ),
        radius = w * 0.5f,
        center = Offset(w * 0.5f, h * 0.5f)
    )

    // Body / Blue Hoodie
    val hoodiePath = Path().apply {
        moveTo(w * 0.12f, h)
        cubicTo(w * 0.18f, h * 0.76f, w * 0.30f, h * 0.72f, w * 0.40f, h * 0.74f)
        lineTo(w * 0.60f, h * 0.74f)
        cubicTo(w * 0.70f, h * 0.72f, w * 0.82f, h * 0.76f, w * 0.88f, h)
        close()
    }
    drawPath(hoodiePath, Color(0xFF3B82F6))

    // Hoodie collar folds & hood outline
    val collarPath = Path().apply {
        moveTo(w * 0.32f, h * 0.75f)
        cubicTo(w * 0.42f, h * 0.86f, w * 0.58f, h * 0.86f, w * 0.68f, h * 0.75f)
        cubicTo(w * 0.60f, h * 0.92f, w * 0.40f, h * 0.92f, w * 0.32f, h * 0.75f)
        close()
    }
    drawPath(collarPath, Color(0xFF2563EB))

    // White Hoodie Drawstrings
    drawLine(
        color = Color.White,
        start = Offset(w * 0.44f, h * 0.82f),
        end = Offset(w * 0.44f, h * 0.96f),
        strokeWidth = w * 0.024f
    )
    drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.44f, h * 0.96f))

    drawLine(
        color = Color.White,
        start = Offset(w * 0.56f, h * 0.82f),
        end = Offset(w * 0.56f, h * 0.96f),
        strokeWidth = w * 0.024f
    )
    drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.56f, h * 0.96f))

    // Neck
    drawRect(
        color = Color(0xFFFED7AA),
        topLeft = Offset(w * 0.43f, h * 0.66f),
        size = Size(w * 0.14f, h * 0.12f)
    )

    // Ears
    drawCircle(Color(0xFFFED7AA), radius = w * 0.09f, center = Offset(w * 0.25f, h * 0.50f))
    drawCircle(Color(0xFFFED7AA), radius = w * 0.09f, center = Offset(w * 0.75f, h * 0.50f))
    drawCircle(Color(0xFFFDBA74), radius = w * 0.05f, center = Offset(w * 0.25f, h * 0.50f))
    drawCircle(Color(0xFFFDBA74), radius = w * 0.05f, center = Offset(w * 0.75f, h * 0.50f))

    // Cute chubby head/face
    drawRoundRect(
        color = Color(0xFFFFEDD5),
        topLeft = Offset(w * 0.26f, h * 0.28f),
        size = Size(w * 0.48f, h * 0.44f),
        cornerRadius = CornerRadius(w * 0.24f, w * 0.22f)
    )

    // Brown tousled anime hair (Back base)
    val hairBackPath = Path().apply {
        moveTo(w * 0.20f, h * 0.50f)
        cubicTo(w * 0.14f, h * 0.26f, w * 0.30f, h * 0.10f, w * 0.50f, h * 0.10f)
        cubicTo(w * 0.70f, h * 0.10f, w * 0.86f, h * 0.26f, w * 0.80f, h * 0.50f)
        lineTo(w * 0.76f, h * 0.38f)
        cubicTo(w * 0.72f, h * 0.20f, w * 0.28f, h * 0.20f, w * 0.24f, h * 0.38f)
        close()
    }
    drawPath(hairBackPath, Color(0xFF3E2723))

    // Front Hair Bangs with layered tufts
    val bangsPath = Path().apply {
        moveTo(w * 0.22f, h * 0.38f)
        cubicTo(w * 0.26f, h * 0.18f, w * 0.74f, h * 0.18f, w * 0.78f, h * 0.38f)
        lineTo(w * 0.72f, h * 0.34f)
        // Middle right tuft
        lineTo(w * 0.65f, h * 0.42f)
        lineTo(w * 0.58f, h * 0.35f)
        // Center bangs tuft
        lineTo(w * 0.48f, h * 0.44f)
        lineTo(w * 0.42f, h * 0.36f)
        // Left tufts
        lineTo(w * 0.34f, h * 0.42f)
        lineTo(w * 0.28f, h * 0.34f)
        close()
    }
    drawPath(bangsPath, Color(0xFF4E342E))

    // Hair highlights
    drawArc(
        color = Color(0xFF8D6E63),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.30f, h * 0.14f),
        size = Size(w * 0.40f, h * 0.16f),
        style = Stroke(width = w * 0.035f)
    )

    // Rosy Cheeks (Blush)
    drawCircle(Color(0xFFF472B6).copy(alpha = 0.5f), radius = w * 0.055f, center = Offset(w * 0.35f, h * 0.55f))
    drawCircle(Color(0xFFF472B6).copy(alpha = 0.5f), radius = w * 0.055f, center = Offset(w * 0.65f, h * 0.55f))

    // Anime Big Cute Eyes
    // Left eye
    drawRoundRect(
        color = Color(0xFF1E1B18),
        topLeft = Offset(w * 0.36f, h * 0.44f),
        size = Size(w * 0.08f, h * 0.11f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.055f)
    )
    // Left eye sparkles
    drawCircle(Color.White, radius = w * 0.024f, center = Offset(w * 0.385f, h * 0.465f))
    drawCircle(Color.White, radius = w * 0.012f, center = Offset(w * 0.415f, h * 0.515f))

    // Right eye
    drawRoundRect(
        color = Color(0xFF1E1B18),
        topLeft = Offset(w * 0.56f, h * 0.44f),
        size = Size(w * 0.08f, h * 0.11f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.055f)
    )
    // Right eye sparkles
    drawCircle(Color.White, radius = w * 0.024f, center = Offset(w * 0.585f, h * 0.465f))
    drawCircle(Color.White, radius = w * 0.012f, center = Offset(w * 0.615f, h * 0.515f))

    // Eyebrows
    drawArc(
        color = Color(0xFF3E2723),
        startAngle = 190f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.34f, h * 0.38f),
        size = Size(w * 0.11f, h * 0.06f),
        style = Stroke(width = w * 0.022f)
    )
    drawArc(
        color = Color(0xFF3E2723),
        startAngle = 210f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.55f, h * 0.38f),
        size = Size(w * 0.11f, h * 0.06f),
        style = Stroke(width = w * 0.022f)
    )

    // Sweet Happy Smile
    val smilePath = Path().apply {
        moveTo(w * 0.45f, h * 0.57f)
        cubicTo(w * 0.47f, h * 0.63f, w * 0.53f, h * 0.63f, w * 0.55f, h * 0.57f)
    }
    drawPath(smilePath, Color(0xFFBE123C), style = Stroke(width = w * 0.022f))
}

// 2. Girl with Glasses
private fun DrawScope.drawGirlGlassesAvatar(w: Float, h: Float) {
    drawCircle(Color(0xFFFCE7F3), radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.5f))

    // Lavender/Pink Sweater
    val shirtPath = Path().apply {
        moveTo(w * 0.14f, h)
        cubicTo(w * 0.20f, h * 0.76f, w * 0.30f, h * 0.72f, w * 0.40f, h * 0.74f)
        lineTo(w * 0.60f, h * 0.74f)
        cubicTo(w * 0.70f, h * 0.72f, w * 0.80f, h * 0.76f, w * 0.86f, h)
        close()
    }
    drawPath(shirtPath, Color(0xFFEC4899))

    // Neck
    drawRect(Color(0xFFFED7AA), topLeft = Offset(w * 0.44f, h * 0.68f), size = Size(w * 0.12f, h * 0.10f))

    // Twin hair tails behind
    drawCircle(Color(0xFF451A03), radius = w * 0.14f, center = Offset(w * 0.20f, h * 0.48f))
    drawCircle(Color(0xFF451A03), radius = w * 0.14f, center = Offset(w * 0.80f, h * 0.48f))

    // Head
    drawRoundRect(
        color = Color(0xFFFFEDD5),
        topLeft = Offset(w * 0.28f, h * 0.28f),
        size = Size(w * 0.44f, h * 0.44f),
        cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
    )

    // Bob bangs
    val bangs = Path().apply {
        moveTo(w * 0.24f, h * 0.40f)
        cubicTo(w * 0.28f, h * 0.16f, w * 0.72f, h * 0.16f, w * 0.76f, h * 0.40f)
        lineTo(w * 0.68f, h * 0.36f)
        lineTo(w * 0.50f, h * 0.42f)
        lineTo(w * 0.32f, h * 0.36f)
        close()
    }
    drawPath(bangs, Color(0xFF5C2C10))

    // Rosy Cheeks
    drawCircle(Color(0xFFF43F5E).copy(alpha = 0.45f), radius = w * 0.05f, center = Offset(w * 0.36f, h * 0.56f))
    drawCircle(Color(0xFFF43F5E).copy(alpha = 0.45f), radius = w * 0.05f, center = Offset(w * 0.64f, h * 0.56f))

    // Glasses Frame
    val glassesColor = Color(0xFF2563EB)
    drawRoundRect(
        color = glassesColor,
        topLeft = Offset(w * 0.33f, h * 0.42f),
        size = Size(w * 0.14f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = Stroke(width = w * 0.024f)
    )
    drawRoundRect(
        color = glassesColor,
        topLeft = Offset(w * 0.53f, h * 0.42f),
        size = Size(w * 0.14f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = Stroke(width = w * 0.024f)
    )
    drawLine(glassesColor, Offset(w * 0.47f, h * 0.47f), Offset(w * 0.53f, h * 0.47f), strokeWidth = w * 0.024f)

    // Smiling eyes inside glasses
    drawCircle(Color(0xFF1E1B18), radius = w * 0.035f, center = Offset(w * 0.40f, h * 0.48f))
    drawCircle(Color.White, radius = w * 0.014f, center = Offset(w * 0.39f, h * 0.47f))
    drawCircle(Color(0xFF1E1B18), radius = w * 0.035f, center = Offset(w * 0.60f, h * 0.48f))
    drawCircle(Color.White, radius = w * 0.014f, center = Offset(w * 0.59f, h * 0.47f))

    // Smile
    drawArc(
        color = Color(0xFFBE123C),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.45f, h * 0.56f),
        size = Size(w * 0.10f, h * 0.06f),
        style = Stroke(width = w * 0.02f)
    )
}

// 3. Boy with Cap
private fun DrawScope.drawBoySportAvatar(w: Float, h: Float) {
    drawCircle(Color(0xFFFEF3C7), radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.5f))

    // Sport Shirt (Teal)
    val shirt = Path().apply {
        moveTo(w * 0.14f, h)
        lineTo(w * 0.86f, h)
        lineTo(w * 0.72f, h * 0.75f)
        lineTo(w * 0.28f, h * 0.75f)
        close()
    }
    drawPath(shirt, Color(0xFF0D9488))

    // Neck & Face
    drawRoundRect(
        color = Color(0xFFFFEDD5),
        topLeft = Offset(w * 0.28f, h * 0.35f),
        size = Size(w * 0.44f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
    )

    // Eyes
    drawCircle(Color(0xFF1E1B18), radius = w * 0.036f, center = Offset(w * 0.40f, h * 0.52f))
    drawCircle(Color.White, radius = w * 0.014f, center = Offset(w * 0.39f, h * 0.51f))
    drawCircle(Color(0xFF1E1B18), radius = w * 0.036f, center = Offset(w * 0.60f, h * 0.52f))
    drawCircle(Color.White, radius = w * 0.014f, center = Offset(w * 0.59f, h * 0.51f))

    // Big grin smile
    val grin = Path().apply {
        moveTo(w * 0.42f, h * 0.61f)
        cubicTo(w * 0.46f, h * 0.69f, w * 0.54f, h * 0.69f, w * 0.58f, h * 0.61f)
        close()
    }
    drawPath(grin, Color(0xFFDC2626))

    // Sporty Yellow Cap (Crown & Visor)
    drawArc(
        color = Color(0xFFF59E0B),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.24f, h * 0.18f),
        size = Size(w * 0.52f, h * 0.36f)
    )
    // Cap visor (curved)
    val visor = Path().apply {
        moveTo(w * 0.20f, h * 0.36f)
        cubicTo(w * 0.40f, h * 0.32f, w * 0.60f, h * 0.32f, w * 0.82f, h * 0.36f)
        cubicTo(w * 0.65f, h * 0.44f, w * 0.35f, h * 0.44f, w * 0.20f, h * 0.36f)
        close()
    }
    drawPath(visor, Color(0xFFD97706))
}

// 4. Girl with Ribbon
private fun DrawScope.drawGirlRibbonAvatar(w: Float, h: Float) {
    drawCircle(Color(0xFFF3E8FF), radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.5f))

    // Coral Top
    drawRoundRect(
        color = Color(0xFFFB7185),
        topLeft = Offset(w * 0.20f, h * 0.74f),
        size = Size(w * 0.60f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
    )

    // Long dark brown hair sides
    drawRoundRect(
        color = Color(0xFF451A03),
        topLeft = Offset(w * 0.20f, h * 0.25f),
        size = Size(w * 0.60f, h * 0.55f),
        cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
    )

    // Face
    drawRoundRect(
        color = Color(0xFFFFEDD5),
        topLeft = Offset(w * 0.28f, h * 0.28f),
        size = Size(w * 0.44f, h * 0.44f),
        cornerRadius = CornerRadius(w * 0.22f, w * 0.22f)
    )

    // Cute pink ribbon on top right
    val ribbon = Path().apply {
        moveTo(w * 0.68f, h * 0.22f)
        lineTo(w * 0.60f, h * 0.16f)
        lineTo(w * 0.60f, h * 0.28f)
        close()
        moveTo(w * 0.68f, h * 0.22f)
        lineTo(w * 0.76f, h * 0.16f)
        lineTo(w * 0.76f, h * 0.28f)
        close()
    }
    drawPath(ribbon, Color(0xFFEC4899))
    drawCircle(Color(0xFFBE185D), radius = w * 0.03f, center = Offset(w * 0.68f, h * 0.22f))

    // Eyes with lashes
    drawCircle(Color(0xFF1E1B18), radius = w * 0.036f, center = Offset(w * 0.40f, h * 0.48f))
    drawCircle(Color.White, radius = w * 0.014f, center = Offset(w * 0.39f, h * 0.47f))
    drawCircle(Color(0xFF1E1B18), radius = w * 0.036f, center = Offset(w * 0.60f, h * 0.48f))
    drawCircle(Color.White, radius = w * 0.014f, center = Offset(w * 0.59f, h * 0.47f))

    // Smile
    drawArc(
        color = Color(0xFFBE123C),
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(w * 0.44f, h * 0.56f),
        size = Size(w * 0.12f, h * 0.06f),
        style = Stroke(width = w * 0.02f)
    )
}

// 5. Parrot Mascot
private fun DrawScope.drawParrotMascotAvatar(w: Float, h: Float) {
    drawCircle(Color(0xFFDCFCE7), radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.5f))

    // Green parrot head
    drawCircle(Color(0xFF22C55E), radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.48f))

    // Feather tuft on top (red & yellow)
    val tuft = Path().apply {
        moveTo(w * 0.48f, h * 0.18f)
        cubicTo(w * 0.44f, h * 0.08f, w * 0.56f, h * 0.08f, w * 0.52f, h * 0.18f)
        close()
    }
    drawPath(tuft, Color(0xFFEF4444))

    // White eye rings
    drawCircle(Color.White, radius = w * 0.10f, center = Offset(w * 0.38f, h * 0.44f))
    drawCircle(Color.White, radius = w * 0.10f, center = Offset(w * 0.62f, h * 0.44f))

    // Black pupils with sparkles
    drawCircle(Color(0xFF0F172A), radius = w * 0.05f, center = Offset(w * 0.39f, h * 0.44f))
    drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.38f, h * 0.43f))
    drawCircle(Color(0xFF0F172A), radius = w * 0.05f, center = Offset(w * 0.61f, h * 0.44f))
    drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.60f, h * 0.43f))

    // Golden Curved Beak
    val beak = Path().apply {
        moveTo(w * 0.44f, h * 0.50f)
        cubicTo(w * 0.50f, h * 0.48f, w * 0.56f, h * 0.50f, w * 0.52f, h * 0.64f)
        cubicTo(w * 0.50f, h * 0.68f, w * 0.46f, h * 0.66f, w * 0.44f, h * 0.50f)
        close()
    }
    drawPath(beak, Color(0xFFF59E0B))
}

// 6. Scholar Bear
private fun DrawScope.drawScholarBearAvatar(w: Float, h: Float) {
    drawCircle(Color(0xFFFEF3C7), radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.5f))

    // Bear Ears
    drawCircle(Color(0xFF92400E), radius = w * 0.11f, center = Offset(w * 0.28f, h * 0.30f))
    drawCircle(Color(0xFFFDE68A), radius = w * 0.06f, center = Offset(w * 0.28f, h * 0.30f))
    drawCircle(Color(0xFF92400E), radius = w * 0.11f, center = Offset(w * 0.72f, h * 0.30f))
    drawCircle(Color(0xFFFDE68A), radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.30f))

    // Bear Head
    drawCircle(Color(0xFFB45309), radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.52f))

    // Snout
    drawRoundRect(
        color = Color(0xFFFEF08A),
        topLeft = Offset(w * 0.41f, h * 0.54f),
        size = Size(w * 0.18f, h * 0.14f),
        cornerRadius = CornerRadius(w * 0.09f, w * 0.07f)
    )
    // Nose
    drawCircle(Color(0xFF1E1B18), radius = w * 0.028f, center = Offset(w * 0.50f, h * 0.58f))

    // Graduation Mortarboard Cap
    val cap = Path().apply {
        moveTo(w * 0.50f, h * 0.12f)
        lineTo(w * 0.76f, h * 0.22f)
        lineTo(w * 0.50f, h * 0.30f)
        lineTo(w * 0.24f, h * 0.22f)
        close()
    }
    drawPath(cap, Color(0xFF1E3A8A))
    // Gold tassel
    drawLine(Color(0xFFF59E0B), Offset(w * 0.50f, h * 0.21f), Offset(w * 0.72f, h * 0.32f), strokeWidth = w * 0.02f)
}
