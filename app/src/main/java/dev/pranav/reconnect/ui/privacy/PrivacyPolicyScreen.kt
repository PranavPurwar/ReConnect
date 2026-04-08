package dev.pranav.reconnect.ui.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pranav.reconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Privacy Promise",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlayfairFamily
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PrivacyHeroCard()

            Text(
                text = "Our Core Principles",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            )

            // Principle 1: Cloud Sync (Supabase)
            PrivacyPillarItem(
                icon = Icons.Default.CloudDone,
                title = "Secure Cloud Sync",
                description = "Your memories are securely synced to your account so you never lose them. We use industry-standard encryption to ensure your data stays private to you."
            )

            // Principle 2: No Selling
            PrivacyPillarItem(
                icon = Icons.Default.Payments,
                title = "Not For Sale",
                description = "We don't sell your data to advertisers, bidders, or third parties. ReConnect is funded by users, not by selling your friendship history."
            )

            // Principle 3: No Ads
            PrivacyPillarItem(
                icon = Icons.Default.VisibilityOff,
                title = "Zero Ad Tracking",
                description = "Your journals and logs are never scanned to serve you ads. Your personal life remains personal."
            )

            // Detailed Policy Section
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "The Details",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "To keep your circle organized, ReConnect asks for Contact permissions. This information is processed to create your dashboard and is never shared externally.\n\n" +
                                "By using an account, your data is stored using Supabase's secure infrastructure. You retain full ownership of your data—you can export or permanently delete your account and all associated memories at any time via the Settings menu.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CharcoalText.copy(alpha = 0.7f),
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Last updated: April 2026",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}


@Composable
private fun PrivacyHeroCard() {
    Surface(
        color = GoldPrimary,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    Icons.Default.GppGood,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column {
                Text(
                    "You're in Control",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "ReConnect is built with privacy by design.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
private fun PrivacyPillarItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = BlueCard // Using your theme's BlueCard
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = BlueText,
                modifier = Modifier.padding(12.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CharcoalText.copy(alpha = 0.6f),
                    lineHeight = 20.sp
                )
            )
        }
    }
}
