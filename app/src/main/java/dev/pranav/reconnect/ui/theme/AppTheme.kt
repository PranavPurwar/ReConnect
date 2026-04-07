package dev.pranav.reconnect.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val ReConnectColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberCardStart,
    onPrimaryContainer = CharcoalText,
    secondary = NavyDark,
    onSecondary = Color.White,
    secondaryContainer = BlueCard,
    onSecondaryContainer = CharcoalText,
    tertiary = CoralLabel,
    onTertiary = Color.White,
    tertiaryContainer = PurpleCard,
    onTertiaryContainer = PurpleText,
    background = CreamBackground,
    onBackground = CharcoalText,
    surface = CreamLight,
    onSurface = CharcoalText,
    surfaceVariant = LightGray,
    onSurfaceVariant = NavyMedium,
    outline = MediumGray,
    outlineVariant = Color(0xFFE0E0E0)
)

private val ReConnectShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val DefaultSeedColor = GoldPrimary

data class AddConnectionExpressiveColors(
    val avatarContainer: Color,
    val avatarIcon: Color,
    val syncChipContainer: Color,
    val syncChipIcon: Color,
    val syncChipText: Color,
    val relationshipFriendContainer: Color,
    val relationshipColleagueContainer: Color
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Suppress("unused")
fun colorSchemeFromSeed(seedColor: Color = DefaultSeedColor): ColorScheme {
    val harmonizedSeedColor = lerp(DefaultSeedColor, seedColor, 0.88f)
    val primary = lerp(Color.White, harmonizedSeedColor, 0.82f)
    val primaryContainer = lerp(Color.White, harmonizedSeedColor, 0.58f)
    val secondary = lerp(NavyDark, harmonizedSeedColor, 0.24f)
    val secondaryContainer = lerp(CreamLight, harmonizedSeedColor, 0.22f)
    val tertiary = lerp(CoralLabel, harmonizedSeedColor, 0.30f)
    val tertiaryContainer = lerp(PurpleCard, harmonizedSeedColor, 0.20f)
    val background = lerp(CreamBackground, harmonizedSeedColor, 0.09f)
    val surface = lerp(CreamLight, harmonizedSeedColor, 0.06f)

    val onPrimary = if (primary.luminance() < 0.48f) Color.White else CharcoalText
    val onSecondary = if (secondary.luminance() < 0.48f) Color.White else CharcoalText
    val onTertiary = if (tertiary.luminance() < 0.48f) Color.White else CharcoalText

    return expressiveLightColorScheme().copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        background = background,
        surface = surface,
        surfaceVariant = lerp(LightGray, harmonizedSeedColor, 0.12f),
        onSurfaceVariant = NavyMedium,
        outlineVariant = lerp(Color(0xFFE0E0E0), harmonizedSeedColor, 0.08f)
    )
}

@Suppress("unused")
fun addConnectionExpressiveColors(scheme: ColorScheme): AddConnectionExpressiveColors {
    return AddConnectionExpressiveColors(
        avatarContainer = scheme.primary.copy(alpha = 0.16f),
        avatarIcon = scheme.primary,
        syncChipContainer = scheme.secondaryContainer.copy(alpha = 0.82f),
        syncChipIcon = scheme.secondary,
        syncChipText = scheme.onSecondaryContainer,
        relationshipFriendContainer = scheme.tertiaryContainer.copy(alpha = 0.88f),
        relationshipColleagueContainer = scheme.secondaryContainer.copy(alpha = 0.78f)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = ReConnectColorScheme,
        typography = Typography,
        shapes = ReConnectShapes,
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Suppress("unused")
fun SeedColorTheme(colors: ColorScheme, content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = ReConnectShapes,
        content = content
    )
}


@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun SeedColorTheme(bitmap: Bitmap, content: @Composable () -> Unit) {

    MaterialExpressiveTheme(
        colorScheme = colorSchemeFromSeed(extractSeedColorOrDefault(bitmap, DefaultSeedColor)),
        typography = Typography,
        shapes = ReConnectShapes,
        content = content
    )
}
