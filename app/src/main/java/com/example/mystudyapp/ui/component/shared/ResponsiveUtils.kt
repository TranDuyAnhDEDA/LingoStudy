package com.example.mystudyapp.ui.component.shared

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen size classification following project responsive specification:
 * - COMPACT: width < 360dp (Small phones 320-359dp)
 * - STANDARD: width 360..599dp (Typical & large phones)
 * - TABLET: width >= 600dp (Tablets & unfolded foldables)
 */
enum class ScreenType {
    COMPACT,
    STANDARD,
    TABLET
}

data class WindowInfo(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val screenType: ScreenType,
    val isCompact: Boolean,
    val isStandard: Boolean,
    val isTablet: Boolean,
    val isShortScreen: Boolean // e.g. height < 680dp or keyboard/landscape
)

@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp
    val height = configuration.screenHeightDp.dp

    val screenType = when {
        width < 360.dp -> ScreenType.COMPACT
        width < 600.dp -> ScreenType.STANDARD
        else -> ScreenType.TABLET
    }

    return WindowInfo(
        screenWidthDp = width,
        screenHeightDp = height,
        screenType = screenType,
        isCompact = screenType == ScreenType.COMPACT,
        isStandard = screenType == ScreenType.STANDARD,
        isTablet = screenType == ScreenType.TABLET,
        isShortScreen = height < 680.dp
    )
}

/**
 * Caps maximum content width on wide screens (tablets) to 680dp,
 * keeping the layout centered and aesthetically proportional without over-stretching cards.
 */
fun Modifier.responsiveContentWidth(maxWidth: Dp = 680.dp): Modifier =
    this.widthIn(max = maxWidth)
