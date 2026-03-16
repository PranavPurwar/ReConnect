package dev.pranav.reconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.ui.theme.GoldPrimary

@Composable
fun UserAvatarBadge(
    modifier: Modifier = Modifier,
    showBorder: Boolean = true
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .then(
                if (showBorder) {
                    Modifier.border(2.dp, GoldPrimary.copy(alpha = 0.35f), CircleShape)
                } else {
                    Modifier
                }
            )
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color(0xFF6B6B6B)
        )
    }
}

