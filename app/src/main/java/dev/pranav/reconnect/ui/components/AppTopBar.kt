package dev.pranav.reconnect.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.SansFontFamily
import dev.pranav.reconnect.ui.theme.UltraFamily

@Composable
fun ScreenTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium.copy(
            fontFamily = UltraFamily,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            lineHeight = 44.sp
        ),
        color = CharcoalText
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    hazeState: HazeState? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val collapsedFraction = (scrollBehavior?.state?.collapsedFraction ?: 0f).coerceIn(0f, 1f)

    val currentFontFamily = if (scrollBehavior != null && collapsedFraction > 0.6f) {
        SansFontFamily
    } else {
        UltraFamily
    }

    val backgroundColor = MaterialTheme.colorScheme.surface

    LargeTopAppBar(
        modifier = modifier
            .then(
                if (hazeState != null && collapsedFraction > 0f) {
                    Modifier.hazeEffect(hazeState) {
                        // Dynamically increase blur radius up to 16.dp as it collapses
                        blurRadius = 16.dp * collapsedFraction
                        // Mix in your subtle surface tint directly on the blurred surface
                        tints =
                            listOf(HazeDefaults.tint(backgroundColor.copy(alpha = collapsedFraction * 0.7f)))
                    }
                } else {
                    Modifier
                }
            ),
        title = {
            Text(
                text = title,
                fontFamily = currentFontFamily,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = CharcoalText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = navigationIcon,
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = CharcoalText
        ),
        scrollBehavior = scrollBehavior
    )
}
