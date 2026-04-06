package dev.pranav.reconnect.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.panpf.sketch.AsyncImage
import dev.pranav.reconnect.ui.theme.UltraFamily
import dev.pranav.reconnect.ui.user.ActionButton
import dev.pranav.reconnect.ui.user.CustomTextField
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.coil.asSketchUri
import io.github.jan.supabase.storage.authenticatedStorageItem

@Composable
private fun EditProfileBackgroundOrbs(colorScheme: ColorScheme) {
    Box(
        Modifier
            .offset((-100).dp, (-50).dp)
            .size(300.dp)
            .background(colorScheme.primary.copy(alpha = 0.12f), CircleShape)
            .blur(80.dp)
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(100.dp, 100.dp)
                .size(400.dp)
                .background(colorScheme.tertiary.copy(alpha = 0.15f), CircleShape)
                .blur(100.dp)
        )
    }
}

@Composable
private fun EditProfileLabelText(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.3.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, SupabaseExperimental::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onBack: () -> Unit
) {
    val initialName by viewModel.initialName.collectAsStateWithLifecycle()
    val initialEmail by viewModel.initialEmail.collectAsStateWithLifecycle()
    val currentAvatarUrl by viewModel.currentAvatarUrl.collectAsStateWithLifecycle()
    val updateResult by viewModel.updateResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var name by remember(initialName) { mutableStateOf(initialName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBytes by remember { mutableStateOf<ByteArray?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
        if (uri != null) {
            avatarBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }

    LaunchedEffect(updateResult) {
        updateResult?.let {
            if (it.isSuccess) {
                snackbarHostState.showSnackbar("Profile updated successfully")
            } else {
                val errorMsg = it.exceptionOrNull()?.message ?: "Update failed"
                snackbarHostState.showSnackbar(errorMsg)
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {

        EditProfileBackgroundOrbs(colorScheme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .offset(x = (-12).dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Edit Profile",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = UltraFamily,
                    fontSize = 38.sp
                ),
                color = colorScheme.onSurface
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer)
                    .clickable { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri == null && currentAvatarUrl == null) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Upload Photo",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (avatarUri != null) {
                    AsyncImage(
                        uri = avatarUri.toString(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (currentAvatarUrl != null) {
                    val publicOrAuthUri =
                        authenticatedStorageItem("avatars", currentAvatarUrl!!).asSketchUri()
                    AsyncImage(
                        uri = publicOrAuthUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(
                "Tap to change photo",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditProfileLabelText("Full Name")
                    CustomTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Your Name",
                        leadingIcon = Icons.Default.Person
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditProfileLabelText("Email Address")
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "your@email.com",
                        leadingIcon = Icons.Default.Email
                    )
                }

                Spacer(Modifier.height(12.dp))

                ActionButton(
                    text = "Save Changes",
                    isLoading = isLoading,
                    enabled = name.isNotBlank() && email.isNotBlank() && (name != initialName || email != initialEmail || avatarBytes != null)
                ) {
                    viewModel.updateProfile(name, email, avatarBytes)
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
