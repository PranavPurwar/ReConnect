package dev.pranav.reconnect.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReConnectTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        typography = Typography,
        content = content
    )
}
