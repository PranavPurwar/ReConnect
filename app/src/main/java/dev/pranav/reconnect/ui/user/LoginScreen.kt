package dev.pranav.reconnect.ui.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pranav.reconnect.di.AppContainer
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
internal fun ActionButton(text: String, isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
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
internal fun BackgroundOrbs(colorScheme: ColorScheme) {
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
internal fun CustomTextField(
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
internal fun LabelText(text: String) {
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
internal fun OrDivider() {
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
internal fun SocialButton(label: String, modifier: Modifier = Modifier, isDark: Boolean) {
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
