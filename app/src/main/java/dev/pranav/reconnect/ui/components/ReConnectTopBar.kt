package dev.pranav.reconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.UltraFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReConnectTopBar(
    showLogo: Boolean = true,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            if (showLogo) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BubbleChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "ReConnect",
                        fontFamily = UltraFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        color = GoldPrimary
                    )
                }
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}


