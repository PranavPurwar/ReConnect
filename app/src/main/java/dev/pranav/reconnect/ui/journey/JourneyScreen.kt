package dev.pranav.reconnect.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.SubcomposeAsyncImage
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.ui.components.UserAvatarBadge
import dev.pranav.reconnect.ui.theme.*

@Composable
fun JourneyScreen(
    innerPadding: PaddingValues = PaddingValues(),
    onOpenGallery: (title: String, uris: List<String>) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    viewModel: JourneyViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground),
        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
    ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onBackClick,
                            shape = CircleShape,
                            color = GoldPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .border(2.dp, GoldPrimary.copy(alpha = 0.2f), CircleShape),
                            shape = CircleShape,
                            color = Color.LightGray
                        ) {
                            UserAvatarBadge(
                                modifier = Modifier.size(48.dp),
                                showBorder = false
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Your Journey",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-2).sp,
                            fontSize = 48.sp
                        ),
                        color = CharcoalText
                    )

                    Spacer(Modifier.height(24.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            JourneyFilterChip(
                                selected = state.selectedCategory == null,
                                label = "All",
                                onClick = { viewModel.setFilter(null) }
                            )
                        }
                        items(MomentCategory.entries.toList()) { cat ->
                            JourneyFilterChip(
                                selected = state.selectedCategory == cat,
                                label = cat.displayName(),
                                onClick = { viewModel.setFilter(cat) }
                            )
                        }
                    }
                }
            }

            if (state.filteredItems.isEmpty()) {
                item {
                    EmptyJourneyState(state.selectedCategory?.displayName()?.lowercase())
                }
            } else {
                itemsIndexed(
                    items = state.filteredItems,
                    key = { _, item -> item.moment.id }
                ) { index, item ->
                    TimelineEntry(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        item = item,
                        isLast = index == state.filteredItems.lastIndex,
                        onOpenGallery = onOpenGallery
                    )
                }
            }
        }
    }

@Composable
private fun TimelineEntry(
    modifier: Modifier = Modifier,
    item: JourneyItem,
    isLast: Boolean,
    onOpenGallery: (title: String, uris: List<String>) -> Unit
) {
    val dotColor = item.moment.category.dotColor()

    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp).padding(top = 28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(22.dp)) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(dotColor.copy(alpha = 0.2f)))
                Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
            }
            if (!isLast) {
                Box(Modifier.width(2.dp).weight(1f).background(GoldPrimary.copy(alpha = 0.15f)))
            }
        }

        Spacer(Modifier.width(16.dp))

        Card(
            modifier = Modifier.weight(1f).padding(bottom = 24.dp),
            shape = RoundedCornerShape(48.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.moment.dateLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = dotColor
                    )
                    Icon(item.moment.category.icon(), null, tint = dotColor, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = item.contactName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic
                    ),
                    color = CharcoalText
                )

                if (item.moment.description.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = CharcoalText)) {
                                append("The Story: ")
                            }
                            append(item.moment.description)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalText.copy(alpha = 0.7f)
                    )
                }

                if (item.moment.imageUris.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    ImageGrid(
                        uris = item.moment.imageUris,
                        onClick = { onOpenGallery(item.moment.title, item.moment.imageUris) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageGrid(uris: List<String>, onClick: () -> Unit) {
    val displayCount = minOf(4, uris.size)
    val extraCount = uris.size - displayCount

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until displayCount) {
            val uri = uris[i]
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick() }
            ) {
                SubcomposeAsyncImage(
                    uri = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GoldPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = null,
                                tint = GoldPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                if (i == displayCount - 1 && extraCount > 0) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$extraCount",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JourneyFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) GoldPrimary else GoldPrimary.copy(alpha = 0.1f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = if (selected) Color.Black else CharcoalText.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EmptyJourneyState(categoryName: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = GoldPrimary.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = GoldPrimary
                    )
                }
            }

            Text(
                text = if (categoryName != null) "No $categoryName memories" else "Your journey is waiting",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlayfairFamily
                ),
                color = CharcoalText,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Log moments with your circle to see your timeline come to life.",
                style = MaterialTheme.typography.bodyLarge,
                color = CharcoalText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

private fun MomentCategory.dotColor(): Color = when (this) {
    MomentCategory.DINING -> GoldPrimary
    MomentCategory.ART -> Color(0xFF10B981)
    MomentCategory.OUTDOORS -> Color(0xFFF97316)
    MomentCategory.GENERAL -> Color(0xFF94A3B8)
}

private fun MomentCategory.icon(): ImageVector = when (this) {
    MomentCategory.DINING -> Icons.Default.Restaurant
    MomentCategory.ART -> Icons.Default.AutoAwesome
    MomentCategory.OUTDOORS -> Icons.Default.Park
    MomentCategory.GENERAL -> Icons.Default.ChatBubbleOutline
}

private fun MomentCategory.displayName(): String = when (this) {
    MomentCategory.DINING -> "Dining"
    MomentCategory.ART -> "Art & Culture"
    MomentCategory.OUTDOORS -> "Outdoors"
    MomentCategory.GENERAL -> "General"
}
