package dev.pranav.reconnect.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pranav.reconnect.core.session.ReminderFrequency
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.PlusJakartaSansFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notifyBirthdays by viewModel.notifyBirthdays.collectAsStateWithLifecycle()
    val notifyCatchUps by viewModel.notifyCatchUps.collectAsStateWithLifecycle()
    val reminderFrequency by viewModel.reminderFrequency.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFrequencySheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notification Settings",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "General") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    onClick = { viewModel.toggleNotifications(!notificationsEnabled) },
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoldPrimary
                            )
                        )
                    }
                )
            }

            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Events") {
                    SettingsItem(
                        icon = Icons.Default.Cake,
                        title = "Birthdays",
                        onClick = { viewModel.toggleNotifyBirthdays(!notifyBirthdays) },
                        trailingContent = {
                            Switch(
                                checked = notifyBirthdays,
                                onCheckedChange = { viewModel.toggleNotifyBirthdays(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldPrimary
                                )
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Coffee,
                        title = "Catch-ups",
                        onClick = { viewModel.toggleNotifyCatchUps(!notifyCatchUps) },
                        trailingContent = {
                            Switch(
                                checked = notifyCatchUps,
                                onCheckedChange = { viewModel.toggleNotifyCatchUps(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldPrimary
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Timing") {
                    SettingsItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Reminder Frequency",
                        onClick = { showFrequencySheet = true },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }

    if (showFrequencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showFrequencySheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                Text(
                    text = "Reminder Frequency",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                ReminderFrequency.entries.forEach { freq ->
                    val isSelected = freq == reminderFrequency
                    Surface(
                        onClick = {
                            viewModel.updateReminderFrequency(freq)
                            scope.launch {
                                sheetState.hide()
                                showFrequencySheet = false
                            }
                        },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = freq.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = PlusJakartaSansFamily,
                                color = if (isSelected) GoldPrimary else CharcoalText,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = GoldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
