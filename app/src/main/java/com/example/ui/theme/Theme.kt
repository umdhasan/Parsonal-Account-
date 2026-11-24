package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = WarmGold,
    onSecondary = Color.White,
    secondaryContainer = WarmGoldContainer,
    onSecondaryContainer = OnWarmGoldContainer,
    tertiary = SlateTeal,
    onTertiary = Color.White,
    tertiaryContainer = SlateTealContainer,
    onTertiaryContainer = OnSlateTealContainer,
    error = ExpenseCoralLight,
    onError = Color.White,
    errorContainer = ExpenseCoralContainerLight,
    onErrorContainer = Color(0xFF5A110E),
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = CrispSurface,
    onSurface = InkPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLowest = CrispSurface,
    surfaceContainerLow = WarmSurfaceContainerLow,
    surfaceContainer = WarmSurfaceContainer,
    surfaceContainerHigh = WarmSurfaceContainerHigh,
    outline = HairlineOutline,
    outlineVariant = HairlineOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLightAccent,
    onPrimary = Color(0xFF003828),
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = EmeraldContainer,
    secondary = GoldLightAccent,
    onSecondary = Color(0xFF3E2900),
    secondaryContainer = Color(0xFF533A08),
    onSecondaryContainer = WarmGoldContainer,
    tertiary = Color(0xFF83CDD9),
    onTertiary = Color(0xFF00363E),
    tertiaryContainer = Color(0xFF154E59),
    onTertiaryContainer = SlateTealContainer,
    error = ExpenseCoralDark,
    onError = Color(0xFF570C09),
    errorContainer = ExpenseCoralContainerDark,
    onErrorContainer = ExpenseCoralContainerLight,
    background = ObsidianBackground,
    onBackground = CloudInk,
    surface = ObsidianSurface,
    onSurface = CloudInk,
    surfaceVariant = ObsidianSurfaceContainerHigh,
    onSurfaceVariant = MutedCloudInk,
    surfaceContainerLowest = Color(0xFF0A100E),
    surfaceContainerLow = ObsidianSurfaceContainerLow,
    surfaceContainer = ObsidianSurfaceContainer,
    surfaceContainerHigh = ObsidianSurfaceContainerHigh,
    outline = ObsidianOutline,
    outlineVariant = ObsidianOutlineVariant,
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun DailyLedgerTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkLedgerColors else LightLedgerColors

    CompositionLocalProvider(LocalLedgerColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
