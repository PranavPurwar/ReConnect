package dev.pranav.reconnect.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.UpcomingEvent
import dev.pranav.reconnect.ui.theme.*

@Composable
fun HomeScreen(
    onContactClick: (String) -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.padding(innerPadding),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = GoldPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HomeTopBar() }
            item { SectionHeader() }

            items(state.upcomingEvents) { event ->
                when (event) {
                    is UpcomingEvent.Birthday -> BirthdayBashCard(event, onContactClick)
                    is UpcomingEvent.CatchUp -> CatchUpCard(event, onContactClick)
                    is UpcomingEvent.TimelineReminder -> TimelineReminderCard(event, onContactClick)
                }
            }

            item { QuickCatchUpsHeader() }

            items(state.quickCatchUps) { (contact, subtitle) ->
                QuickCatchUpRow(contact, subtitle, onContactClick)
            }
        }
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = GoldPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("⚇", fontSize = 20.sp, color = GoldPrimary)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "ReConnect",
            style = MaterialTheme.typography.titleLarge,
            color = GoldPrimary
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = CharcoalText
            )
        }
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun SectionHeader() {
    Text(
        text = "Upcoming\nConnections",
        style = MaterialTheme.typography.displayMedium,
        color = CharcoalText,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun BirthdayBashCard(event: UpcomingEvent.Birthday, onContactClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(AmberCardStart, AmberCardEnd))
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "BIRTHDAY BASH",
                    style = MaterialTheme.typography.labelLarge,
                    color = CoralLabel
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = event.contactName,
                    style = MaterialTheme.typography.displayLarge,
                    color = CharcoalText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = event.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalText.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "${event.day}",
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp,
                            color = CharcoalText.copy(alpha = 0.15f)
                        )
                        Text(
                            text = event.month,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldDark
                        )
                    }
                    Button(
                        onClick = { onContactClick(event.contactId) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldDark,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Send\nWish", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun CatchUpCard(event: UpcomingEvent.CatchUp, onContactClick: (String) -> Unit) {
    Card(
        onClick = { onContactClick(event.contactId) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCard)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(
                Icons.Default.Checklist,
                contentDescription = null,
                tint = CharcoalText,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Catch up with ${event.contactName}",
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalText
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${event.day}",
                fontFamily = SerifFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = GoldPrimary
            )
            Text(
                text = event.dayOfWeek,
                style = MaterialTheme.typography.labelLarge,
                color = GoldPrimary
            )
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = BlueCard)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = CharcoalText,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = event.duration,
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalText
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { onContactClick(event.contactId) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = event.actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldPrimary
                )
            }
        }
    }
}

@Composable
private fun QuickCatchUpsHeader() {
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
        TextButton(onClick = {}) {
            Text("View all", color = GoldPrimary)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGray
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = "Log Note",
                    tint = CharcoalText
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Quick Send",
                    tint = CharcoalText
                )
            }
        }
    }
}

