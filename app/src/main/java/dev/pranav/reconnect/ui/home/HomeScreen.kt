package dev.pranav.reconnect.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.UpcomingEvent
import dev.pranav.reconnect.ui.theme.*

@Composable
private fun TimelineReminderCard(
    event: UpcomingEvent.TimelineReminder,
    onContactClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleCard)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = PurpleText,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = event.duration,
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalText
            )
            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = { onContactClick(event.contactId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = PurpleText
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
            .padding(horizontal = 20.dp),
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
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = GoldPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.BubbleChart, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Text(
                text = "ReConnect",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = UltraFamily,
                    color = GoldPrimary,
                    letterSpacing = (-1).sp
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.LightGray.copy(0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, null, tint = CharcoalText)
                }
            }
            Surface(
                modifier = Modifier.size(40.dp).border(2.dp, GoldPrimary, CircleShape),
                shape = CircleShape
            ) {
                AsyncImage(
                    uri = "https://lh3.googleusercontent.com/aida-public/AB6AXuAVIAW1MXyPH0lbiJSkVqCmrcUIjgB6FhHPLV4LUIGpUtDo0_Xcl_F79XMqd5l7Rgc7libSBX82F_9kKWvNfE5VSiHAqBRMNAJ-l7mL_JBxOj6SpHJ2aVxruUiJB-voIaiCFerz4DeyWMGyI7RR3I6aVVl9sb_8UnlNAMY688sDCX3pnaYW1JuiSJY3a1gEV5M_iWcMAK4xIH-7R8ZS6uOCaugX9OaRpNkbOcq8w1qrwApqIdq6klUSsVC7eG0McegEh2U8wRFj__bx",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onContactClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onViewAllCatchUpsClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val featuredBirthdayId = (state.upcomingEvents.firstOrNull() as? UpcomingEvent.Birthday)?.contactId

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HomeHeader() }

            item {
                Text(
                    text = "Upcoming\nConnections",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = UltraFamily,
                        fontWeight = FontWeight.Black,
                        lineHeight = 42.sp,
                        fontSize = 44.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = CharcoalText,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                )
            }

            items(state.upcomingEvents) { event ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    when (event) {
                        is UpcomingEvent.Birthday -> {
                            if (event.contactId == featuredBirthdayId) {
                                BirthdayBashCard(event, onContactClick)
                            } else {
                                BirthdayCompactCard(event, onContactClick)
                            }
                        }
                        is UpcomingEvent.CatchUp -> CatchUpCard(event)
                        is UpcomingEvent.TimelineReminder -> TimelineReminderCard(event, onContactClick)
                    }
                }
            }

            item {
                QuickCatchUpsHeader(onViewAllClick = onViewAllCatchUpsClick)
            }

            items(state.quickCatchUps.take(5)) { (contact, subtitle) ->
                QuickCatchUpRow(contact, subtitle, onContactClick)
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = GoldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BirthdayCompactCard(event: UpcomingEvent.Birthday, onContactClick: (String) -> Unit) {
    Card(
        onClick = { onContactClick(event.contactId) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "BIRTHDAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = GoldPrimary
                )
                Text(
                    text = event.contactName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CharcoalText
                )
            }

            Text(
                text = "${event.day} ${event.month}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = UltraFamily,
                    fontWeight = FontWeight.Black
                ),
                color = GoldPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BirthdayBashCard(event: UpcomingEvent.Birthday, onContactClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(48.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparent to show gradient
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AmberCardStart, AmberCardEnd)
                    )
                )
        ) {
            // Decorative blurred circle in the top right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .size(160.dp)
                    .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                    .blur(60.dp)
            )

            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = "BIRTHDAY BASH",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = GoldPrimary
                    )
                )

                Text(
                    text = event.contactName.replace(" ", "\n"),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Black,
                        lineHeight = 52.sp,
                        fontSize = 56.sp
                    ),
                    color = CharcoalText,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = "Turning 28. Don't forget the lilies!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalText.copy(0.7f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy((-20).dp)) {
                        Text(
                            text = "${event.day}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = UltraFamily,
                                fontSize = 90.sp,
                                color = GoldPrimary.copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = event.month.uppercase(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = UltraFamily,
                                fontWeight = FontWeight.Black
                            ),
                            color = CharcoalText
                        )
                    }

                    // Shadowed Button from mockup
                    Surface(
                        onClick = { onContactClick(event.contactId) },
                        color = GoldPrimary,
                        shape = RoundedCornerShape(32.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .height(64.dp)
                            .padding(start = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "Send Wish",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMediumEmphasized
                            )
                        }
                    }
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
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
            Icon(Icons.Default.Coffee, null, tint = contentColor, modifier = Modifier.size(20.dp))

            Text(
                text = "Catch up with ${event.contactName}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor,
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
            )

            Text(
                text = "${event.day}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = UltraFamily,
                    fontSize = 34.sp
                ),
                color = contentColor.copy(alpha = 0.6f)
            )
            Text(
                text = event.dayOfWeek.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
private fun QuickCatchUpRow(
    contact: Contact,
    subtitle: String,
    onContactClick: (String) -> Unit
) {
    Card(
        onClick = { onContactClick(contact.id) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                uri = contact.photoUri,
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )

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
}
