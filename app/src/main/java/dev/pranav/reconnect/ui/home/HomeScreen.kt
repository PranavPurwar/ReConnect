package dev.pranav.reconnect.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.UpcomingEvent
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.theme.*

@Composable
private fun TimelineReminderCard(
    event: UpcomingEvent.TimelineReminder,
    onContactClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = PurpleText,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = event.duration,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = CharcoalText
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onContactClick(event.contactId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleText.copy(alpha = 0.15f),
                    contentColor = PurpleText
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = event.actionLabel,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun QuickCatchUpsHeader(onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "QUICK CATCH-UPS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = CharcoalText,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onViewAllClick) {
            Text("View all", color = GoldPrimary)
        }
    }
}


@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
            color = IconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.AllInclusive,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = "ReConnect",
                style = MaterialTheme.typography.displaySmallEmphasized.copy(
                    fontFamily = UltraFamily,
                    color = IconBackground,
                    letterSpacing = 1.sp
                )
            )
        }
}

@Composable
private fun EmptyHomeState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.AllInclusive,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Your circle is empty",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add connections to start ReConnecting.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = PlayfairFamily,
            fontWeight = FontWeight.Bold
        ),
        color = CharcoalText,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun HomeScreen(
    onContactClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onViewAllCatchUpsClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = viewModel(factory = dev.pranav.reconnect.di.AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val birthdayEvents = state.upcomingEvents.filterIsInstance<UpcomingEvent.Birthday>()
    val connectionEvents = state.upcomingEvents.filter { it !is UpcomingEvent.Birthday }

    val isEmpty = state.upcomingEvents.isEmpty() && state.quickCatchUps.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = 100.dp
                )
            ) {
                item {
                    HomeHeader()
                }

                if (state.isLoading) {
                    // Do not show empty state while loading
                } else if (isEmpty) {
                    item {
                        EmptyHomeState()
                    }
                } else {
                    if (birthdayEvents.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Upcoming Birthdays")
                            val pagerState = rememberPagerState(pageCount = { birthdayEvents.size })
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                pageSpacing = 12.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val event = birthdayEvents[page]
                                BirthdayBashCard(event, onContactClick)
                            }
                        }
                    }

                    if (connectionEvents.isNotEmpty()) {
                        item {
                            SectionHeader(title = "To Reconnect")
                            val pagerState =
                                rememberPagerState(pageCount = { connectionEvents.size })
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                pageSpacing = 12.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                when (val event = connectionEvents[page]) {
                                    is UpcomingEvent.CatchUp -> CatchUpCard(event)
                                    is UpcomingEvent.TimelineReminder -> TimelineReminderCard(
                                        event,
                                        onContactClick
                                    )

                                    else -> {}
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    if (state.quickCatchUps.isNotEmpty()) {
                        item {
                            QuickCatchUpsHeader(onViewAllClick = onViewAllCatchUpsClick)
                        }

                        items(
                            items = state.quickCatchUps.take(5),
                            key = { pair -> pair.first.id }
                        ) { (contact, subtitle) ->
                            QuickCatchUpRow(
                                contact = contact,
                                subtitle = subtitle,
                                onClick = onContactClick,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            containerColor = GoldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Contact", fontWeight = FontWeight.Bold)
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayBashCard(event: UpcomingEvent.Birthday, onContactClick: (String) -> Unit) {
    val seedColor = event.seedColorArgb?.let { Color(it) } ?: AmberCardStart
    val startColor = lerp(Color.White, seedColor, 0.4f)
    val endColor = lerp(Color.White, seedColor, 0.1f)
    val accentColor = lerp(seedColor, CharcoalText, 0.4f)

    Card(
        onClick = { onContactClick(event.contactId) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(startColor, endColor)))
        ) {
            // Subtle aesthetic glow / circle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
                    .size(120.dp)
                    .background(seedColor.copy(alpha = 0.25f), CircleShape)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BIRTHDAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = accentColor
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = event.contactName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = PlayfairFamily,
                            fontWeight = FontWeight.Black
                        ),
                        color = CharcoalText,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Send a wish",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "${event.day}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = UltraFamily
                        ),
                        color = accentColor
                    )
                    Text(
                        text = event.month.take(3).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = CharcoalText
                    )
                }
            }
        }
    }
}

@Composable
private fun CatchUpCard(event: UpcomingEvent.CatchUp) {
    val backgroundColor = event.seedColorArgb?.let { Color(it) } ?: BlueCard
    val contentColor = if (backgroundColor.luminance() > 0.62f) CharcoalText else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(Icons.Default.Coffee, null, tint = contentColor, modifier = Modifier.size(24.dp))

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Catch up with\n${event.contactName}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                ),
                color = contentColor,
                maxLines = 2
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${event.day}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = UltraFamily
                    ),
                    color = contentColor.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = event.dayOfWeek.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}


@Composable
private fun QuickCatchUpRow(
    contact: Contact,
    subtitle: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(contact.id) }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val state = rememberAsyncImageState()
        val seedColor = contact.seedColorArgb?.let { Color(it) } ?: DefaultSeedColor
        val scheme = colorSchemeFromSeed(seedColor)

        SeedColorTheme(colors = scheme) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (state.painterState !is PainterState.Success) {
                    val initials = contact.name.split(" ").take(2)
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
                        .takeIf { it.isNotEmpty() } ?: "?"
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                AsyncImage(
                    uri = AppContainer.photoResolver.resolveContactPhoto(contact.id),
                    state = state,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Black
                )
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Surface(
            shape = CircleShape,
            color = AmberCardStart.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = GoldPrimary)
            }
        }
    }
}
