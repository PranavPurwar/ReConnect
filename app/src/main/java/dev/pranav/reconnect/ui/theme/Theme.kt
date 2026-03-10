package dev.pranav.reconnect.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReConnectTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = ReConnectColorScheme,
        typography = Typography,
        shapes = ReConnectShapes,
        content = content
    )
}
