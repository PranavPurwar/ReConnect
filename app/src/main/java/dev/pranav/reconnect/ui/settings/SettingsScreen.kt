package dev.pranav.reconnect.ui.settings

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.components.ScreenTitle
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.CreamBackground
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.PlusJakartaSansFamily

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onEditProfileClick: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoginEnabled by viewModel.isLoginEnabled.collectAsStateWithLifecycle()
    val signOutResult by viewModel.signOutResult.collectAsStateWithLifecycle()

    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userId by viewModel.userId.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(CreamBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                ScreenTitle(
                    text = "Settings",
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
            }

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
                            containerColor = Color.White
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

            SettingsSection(title = "Preferences") {
                if (isLoginEnabled) {
                    SettingsItem(
                        icon = Icons.Default.Subscriptions,
                        title = "Subscription Plan",
                        onClick = {
                            // TODO: Add screen
                        }
                    )
                }
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = {
                        // TODO: Add screen
                    }
                )
            }

            SettingsSection(title = "Resources") {
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    onClick = {
                        val uri = "https://example.com/privacy".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
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
                .background(Color.White),
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
    onClick: () -> Unit
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
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
