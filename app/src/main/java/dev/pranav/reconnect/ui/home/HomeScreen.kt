package dev.pranav.reconnect.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.components.CurrentUserAvatar
import dev.pranav.reconnect.ui.components.ScreenTitle
import dev.pranav.reconnect.ui.theme.*

@Composable
fun HomeScreen(
    onContactClick: (String) -> Unit,
    onMomentClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onViewAllCatchUpsClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = viewModel(factory = dev.pranav.reconnect.di.AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CreamBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = GoldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "New Connection",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 100.dp
            )
        ) {
            // --- Header ---
            item {
                HomeHeader(state.userName)
                Spacer(Modifier.height(32.dp))
            }

            // --- Suggestion Hero ---
            state.topSlot?.let { slot ->
                item {
                    SuggestionHero(slot, onContactClick)
                    Spacer(Modifier.height(40.dp))
                }
            }

            // --- Frequent Faces ---
            if (state.reconnectChips.isNotEmpty()) {
                item {
                    SectionHeader("Inner Circle", onAction = onViewAllCatchUpsClick)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        items(state.reconnectChips) { chip ->
                            FrequentFaceNode(chip, onContactClick)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // --- Daily Reconnects (Unified Sheet) ---
            if (state.quickCatchUps.isNotEmpty()) {
                item {
                    SectionHeader("Daily Reconnects", subtitle = state.reconnectSummary)
                    Spacer(Modifier.height(12.dp))
                    ReconnectActionSheet(state.quickCatchUps.take(5), onContactClick)
                    Spacer(Modifier.height(40.dp))
                }
            }

            state.recentMoment?.let { moment ->
                item {
                    SectionHeader("Recent Memory")
                    Spacer(Modifier.height(16.dp))
                    MemoryPreviewCard(moment) { onMomentClick(it) }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "RECONNECT",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldPrimary
                )
            )
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, GoldPrimary)
            ) {
                CurrentUserAvatar(modifier = Modifier.fillMaxSize(), showBorder = false)
            }
        }
        Spacer(Modifier.height(12.dp))
        ScreenTitle(text = "Hello, $userName")
    }
}

@Composable
private fun SuggestionHero(slot: HomeTopSlot, onContactClick: (String) -> Unit) {
    val (bgColor, accentColor, label) = when (slot) {
        is HomeTopSlot.Birthday -> Triple(AmberCardStart, GoldDark, "Upcoming Birthday")
        is HomeTopSlot.MemoryFlashback -> Triple(PurpleCard, PurpleText, "On this day")
        is HomeTopSlot.RelationshipSummary -> Triple(BlueCard, BlueText, "Connection Insight")
        is HomeTopSlot.SuggestedCatchUp -> Triple(Color.White, GoldPrimary, "Suggested Catch-up")
    }

    val contactId = when (slot) {
        is HomeTopSlot.Birthday -> slot.event.contactId
        is HomeTopSlot.SuggestedCatchUp -> slot.event.contactId
        is HomeTopSlot.MemoryFlashback -> slot.contactId
        else -> null
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(42.dp),
        color = bgColor,
        border = if (bgColor == Color.White) BorderStroke(1.dp, CreamBackground) else null,
        onClick = {
            when (slot) {
                is HomeTopSlot.Birthday -> onContactClick(slot.event.contactId)
                is HomeTopSlot.MemoryFlashback -> slot.contactId?.let { onContactClick(it) }
                is HomeTopSlot.SuggestedCatchUp -> onContactClick(slot.event.contactId)
                else -> {}
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = accentColor.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))

                val title = when (slot) {
                    is HomeTopSlot.Birthday -> slot.event.contactName
                    is HomeTopSlot.MemoryFlashback -> slot.title
                    is HomeTopSlot.SuggestedCatchUp -> slot.event.contactName
                    is HomeTopSlot.RelationshipSummary -> slot.title
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    ),
                    color = CharcoalText
                )

                val body = when (slot) {
                    is HomeTopSlot.Birthday -> "Turns ${slot.event.day} on ${slot.event.month}"
                    is HomeTopSlot.SuggestedCatchUp -> "It's been a while since your last chat."
                    is HomeTopSlot.MemoryFlashback -> slot.subtitle
                    is HomeTopSlot.RelationshipSummary -> slot.subtitle
                }

                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalText.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            contactId?.let { id ->
                Spacer(Modifier.width(16.dp))
                HomeAvatar(id, "", null, size = 80.dp)
            }
        }
    }
}

@Composable
private fun ReconnectActionSheet(
    items: List<Pair<Contact, String>>,
    onContactClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White
    ) {
        Column {
            items.forEachIndexed { index, (contact, status) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactClick(contact.id) }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeAvatar(contact.id, contact.name, contact.seedColorArgb, size = 44.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            contact.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CharcoalText
                        )
                        Text(
                            status,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalText.copy(0.4f)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        Modifier.size(16.dp),
                        tint = GoldPrimary.copy(0.6f)
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 20.dp),
                        thickness = 0.5.dp,
                        color = CreamBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequentFaceNode(chip: ReconnectChip, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(chip.contactId) }
    ) {
        HomeAvatar(chip.contactId, chip.name, chip.seedColorArgb, size = 68.dp, showBorder = true)
        Spacer(Modifier.height(8.dp))
        Text(
            chip.name.split(" ").first(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = CharcoalText
        )
    }
}

@Composable
private fun MemoryPreviewCard(moment: RecentMoment, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = RoundedCornerShape(48.dp),
        color = Color.White,
        onClick = { onClick(moment.id) }
    ) {
        Column(Modifier.padding(12.dp)) {
            AsyncImage(
                uri = moment.imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(40.dp)),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(24.dp)) {
                Text(
                    moment.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic
                    ),
                    color = CharcoalText
                )
                Text(
                    moment.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalText.copy(0.5f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Black
                ),
                color = CharcoalText
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalText.copy(0.4f)
                )
            }
        }
        if (onAction != null) {
            Text(
                "View All",
                Modifier.clickable { onAction() },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            )
        }
    }
}

@Composable
private fun HomeAvatar(
    id: String,
    name: String,
    seed: Int?,
    size: androidx.compose.ui.unit.Dp,
    showBorder: Boolean = false
) {
    val state = rememberAsyncImageState()
    val initials =
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }
            .uppercase()

    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (showBorder) Modifier
                    .background(GoldPrimary.copy(0.1f), CircleShape)
                    .padding(3.dp) else Modifier
            )
            .clip(CircleShape)
            .background(if (seed != null) Color(seed).copy(0.15f) else CreamBackground),
        contentAlignment = Alignment.Center
    ) {
        if (state.painterState !is PainterState.Success) {
            Text(
                initials,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            )
        }
        AsyncImage(
            uri = AppContainer.photoResolver.resolveContactPhoto(id),
            state = state,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
