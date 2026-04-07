package dev.pranav.reconnect.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.theme.UltraFamily
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val imageState = rememberAsyncImageState()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
        if (uri != null) {
            avatarBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }
    var isEmailSent by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        BackgroundOrbs(colorScheme)
        if (isEmailSent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    null,
                    modifier = Modifier.size(100.dp),
                    tint = colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Confirm your email",
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = UltraFamily),
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "We sent a link to $email.\nPlease check your inbox and click the link to activate your account.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Back to Sign In", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))
                Text(
                    "Create Account",
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
                    if (avatarUri == null || imageState.painterState !is PainterState.Success) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    if (avatarUri != null) {
                        AsyncImage(
                            uri = avatarUri.toString(),
                            state = imageState,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    "Tap to upload photo",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(32.dp))
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabelText("Full Name")
                        CustomTextField(
                            fullName,
                            { fullName = it },
                            "John Doe",
                            Icons.Default.Person
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabelText("Email Address")
                        CustomTextField(
                            email,
                            { email = it },
                            "email@example.com",
                            Icons.Default.Mail
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabelText("Password")
                        CustomTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Min. 6 characters",
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )
                    }
                    ActionButton(
                        "Sign Up",
                        isLoading,
                        email.isNotBlank() && password.length >= 6 && fullName.isNotBlank()
                    ) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = AppContainer.authStore.signUp(
                                email = email.trim(),
                                pass = password,
                                fullName = fullName.trim(),
                                avatar = avatarBytes
                            )
                            isLoading = false
                            if (result.isSuccess) {
                                onSignUpSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message
                            }
                        }
                    }
                    AnimatedVisibility(errorMessage != null) {
                        Text(
                            errorMessage ?: "",
                            color = colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                TextButton(onClick = onBackToLogin) {
                    Text(
                        "Already have an account? Sign In",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
