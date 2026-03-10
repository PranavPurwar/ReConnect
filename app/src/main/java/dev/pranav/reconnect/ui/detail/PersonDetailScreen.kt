package dev.pranav.reconnect.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    contactId: String,
    onBack: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: PersonDetailViewModel = viewModel()
) {
    LaunchedEffect(contactId) { viewModel.loadContact(contactId) }

    val state by viewModel.uiState.collectAsState()
    val contact = state.contact

    Scaffold(
        modifier = Modifier.padding(innerPadding),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ReConnect",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
        if (contact == null) return@Scaffold

        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Avatar with active dot
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .border(3.dp, Color(0xFFE0D0B8), CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                if (contact.isActive) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(20.dp)
                            .border(3.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(ActiveGreen)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            if (contact.title.isNotBlank() || contact.relationship.isNotBlank()) {
                val subtitle = listOfNotNull(
                    contact.title.takeIf { it.isNotBlank() },
                    contact.relationship.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = GoldDark,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Message")
                }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call")
                }
            }

            Spacer(Modifier.height(32.dp))

            // Next Talk
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Next Talk",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardYellowLight)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "TO DISCUSS",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldDark,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.toDiscuss,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SerifFontFamily
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = GoldDark
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = state.nextTalkDate,
                                style = MaterialTheme.typography.bodyLarge,
                                color = GoldDark
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyDark,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Calendar")
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Past Moments
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Past Moments",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(16.dp))

                state.pastMoments.forEachIndexed { index, moment ->
                    PastMomentItem(
                        moment = moment,
                        isLast = index == state.pastMoments.lastIndex
                    )
                    if (index != state.pastMoments.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PastMomentItem(moment: PastMoment, isLast: Boolean) {
    val icon = when (moment.category) {
        MomentCategory.DINING -> Icons.Default.Restaurant
        MomentCategory.ART -> Icons.Default.Palette
        MomentCategory.OUTDOORS -> Icons.Default.Park
        MomentCategory.GENERAL -> Icons.Default.ChatBubbleOutline
    }

    val iconBg = when (moment.category) {
        MomentCategory.DINING -> Color(0xFFFFF3E0)
        MomentCategory.ART -> Color(0xFFF3E5F5)
        MomentCategory.OUTDOORS -> Color(0xFFE8F5E9)
        MomentCategory.GENERAL -> Color(0xFFE3F2FD)
    }

    val iconTint = when (moment.category) {
        MomentCategory.DINING -> Color(0xFFE65100)
        MomentCategory.ART -> Color(0xFF7B1FA2)
        MomentCategory.OUTDOORS -> Color(0xFF2E7D32)
        MomentCategory.GENERAL -> Color(0xFF1565C0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left axis: icon + vertical line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconTint)
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MediumGray.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Right content
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = moment.dateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MediumGray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = moment.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SerifFontFamily
                    )
                )

                if (moment.imageUris.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(moment.imageUris) { uri ->
                            ImageThumbnailPlaceholder(uri)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = moment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImageThumbnailPlaceholder(tag: String) {
    val color = if (tag.contains("1")) Color(0xFFE8D5B7) else Color(0xFF2E7D6A)
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Palette,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
    }
}
