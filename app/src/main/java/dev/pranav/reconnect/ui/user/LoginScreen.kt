package dev.pranav.reconnect.ui.user

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.theme.SansFontFamily
import dev.pranav.reconnect.ui.theme.UltraFamily
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colorScheme.surface)) {
        BackgroundOrbs(colorScheme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Icon(
                Icons.Default.AllInclusive,
                null,
                tint = colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = UltraFamily,
                    fontSize = 42.sp
                ),
                color = colorScheme.onSurface
            )
            Text(
                "Join the ReConnect community",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SansFontFamily),
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText("Email Address")
                    CustomTextField(
                        email,
                        { email = it },
                        "hello@reconnect.com",
                        Icons.Default.Mail
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText("Password")
                    CustomTextField(
                        password,
                        { password = it },
                        "••••••••",
                        Icons.Default.Lock,
                        true,
                        passwordVisible,
                        { passwordVisible = !passwordVisible })
                }

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { }) {
                        Text(
                            "Forgot Password?",
                            color = colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                ActionButton("Sign In", isLoading, email.isNotBlank() && password.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = AppContainer.authStore.signIn(email.trim(), password)
                        isLoading = false
                        if (result.isSuccess) onLoginSuccess() else errorMessage =
                            result.exceptionOrNull()?.message
                    }
                }

                AnimatedVisibility(errorMessage != null) {
                    Text(
                        errorMessage ?: "",
                        color = colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                OrDivider()

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SocialButton("Google", Modifier.weight(1f), false)
                    SocialButton("Apple", Modifier.weight(1f), true)
                }
            }

            Spacer(Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New here?", color = colorScheme.onSurfaceVariant)
                TextButton(onClick = onCreateAccountClick) {
                    Text(
                        "Create Account",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colorScheme.surface)) {
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

@Composable
fun ActionButton(text: String, isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
    ) {
        if (isLoading) CircularProgressIndicator(
            Modifier.size(24.dp),
            color = colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        else Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BackgroundOrbs(colorScheme: ColorScheme) {
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
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = colorScheme.outline) },
        leadingIcon = { Icon(leadingIcon, null, tint = colorScheme.primary.copy(alpha = 0.6f)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null
                    )
                }
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email),
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.15f),
            unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.1f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun LabelText(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.3.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun OrDivider() {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        HorizontalDivider(Modifier.weight(1f), color = colorScheme.outlineVariant)
        Text("  OR  ", style = MaterialTheme.typography.labelSmall, color = colorScheme.outline)
        HorizontalDivider(Modifier.weight(1f), color = colorScheme.outlineVariant)
    }
}

@Composable
private fun SocialButton(label: String, modifier: Modifier = Modifier, isDark: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = { },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isDark) colorScheme.onSurface else colorScheme.surface.copy(alpha = 0.3f),
            contentColor = if (isDark) colorScheme.surface else colorScheme.onSurface
        )
    ) { Text(label, fontWeight = FontWeight.SemiBold) }
}
