package dev.pranav.reconnect.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.core.session.MapStyle
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.components.AppTopBar
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.PlusJakartaSansFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onEditProfileClick: () -> Unit,
    onSignOutSuccess: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onNotificationsSettingsClick: () -> Unit,
    onSubscriptionPlanClick: () -> Unit,
) {
    val isLoginEnabled by viewModel.isLoginEnabled.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val signOutResult by viewModel.signOutResult.collectAsStateWithLifecycle()
    val mapStyle by viewModel.mapStyle.collectAsStateWithLifecycle()

    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userId by viewModel.userId.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showBackupSheet by remember { mutableStateOf(false) }
    var showMapStyleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingExportJson != null) {
            writeTextToUri(context, uri, pendingExportJson!!)
            backupMessage = "Backup exported successfully"
            pendingExportJson = null
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val backupJson = readTextFromUri(context, uri)
            if (backupJson != null) {
                viewModel.restoreBackupJson(backupJson)
                backupMessage = "Backup imported successfully"
            } else {
                backupMessage = "Unable to read backup file"
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(signOutResult) {
        signOutResult?.let {
            if (it.isSuccess) {
                onSignOutSuccess()
            } else {
                val errorMsg = it.exceptionOrNull()?.message ?: "Sign out failed"
                snackbarHostState.showSnackbar(errorMsg)
            }
        }
    }

    val imageUri = AppContainer.photoResolver.resolveUserAvatar(userId)

    val imageState = rememberAsyncImageState()
    val isSuccess = imageState.painterState is PainterState.Success

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Settings",
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoginEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .border(2.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isSuccess) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        AsyncImage(
                            uri = imageUri,
                            state = imageState,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userName.ifBlank { "ReConnect User" },
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )

                    if (userEmail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = PlusJakartaSansFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = onEditProfileClick,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CharcoalText,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Edit Profile",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSansFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Sync") {
                SettingsItem(
                    icon = Icons.Default.Cloud,
                    title = "Cloud sync",
                    onClick = { viewModel.refreshSyncStatus() },
                    trailingContent = {
                        Text(
                            text = syncStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                SettingsItem(
                    icon = Icons.Default.FileUpload,
                    title = "Backup & restore",
                    onClick = { showBackupSheet = true }
                )
            }

            SettingsSection(title = "Preferences") {
                if (isLoginEnabled) {
                    SettingsItem(
                        icon = Icons.Default.Subscriptions,
                        title = "Subscription Plan",
                        onClick = onSubscriptionPlanClick
                    )
                }
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = onNotificationsSettingsClick
                )
            }

            SettingsSection(title = "Map") {
                SettingsItem(
                    icon = Icons.Default.Map,
                    title = "Map theme",
                    onClick = { showMapStyleSheet = true },
                    trailingContent = {
                        Text(
                            text = mapStyle.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (showMapStyleSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showMapStyleSheet = false },
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
                            text = "Map theme",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )

                        MapStyle.entries.forEach { style ->
                            val isSelected = style == mapStyle
                            Surface(
                                onClick = {
                                    viewModel.updateMapStyle(style)
                                    scope.launch {
                                        sheetState.hide()
                                        showMapStyleSheet = false
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
                                        text = style.label,
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

            SettingsSection(title = "Resources") {
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    onClick = onPrivacyPolicyClick
                )
            }

            if (isLoginEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = null
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sign Out",
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp)) // Extra padding clear from bottom bar
        }

        if (showBackupSheet) {
            AlertDialog(
                onDismissRequest = { showBackupSheet = false },
                title = { Text("Backup & restore") },
                text = {
                    Column {
                        Text(
                            "Export your current contacts and moments as JSON, or import a previously saved backup. " +
                                    "This preserves your circle data and reconnect history."
                        )
                        backupMessage?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Green
                            )
                        }
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(onClick = {
                            showBackupSheet = false
                            backupMessage = null
                        }) {
                            Text("Close")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            viewModel.prepareExportJson(
                                onReady = { json ->
                                    pendingExportJson = json
                                    exportLauncher.launch("reconnect-backup.json")
                                }
                            )
                        }) {
                            Text("Export")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            backupMessage = null
                            importLauncher.launch(arrayOf("application/json", "text/*"))
                        }) {
                            Text("Import")
                        }
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState
        )
    }
}

private fun writeTextToUri(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(text.toByteArray())
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String? {
    return context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFamily,
            letterSpacing = 1.3.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 12.dp, top = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(GoldPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = PlusJakartaSansFamily,
            color = CharcoalText,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
