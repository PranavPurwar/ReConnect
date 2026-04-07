package dev.pranav.reconnect.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.core.storage.AuthState
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.theme.SansFontFamily
import dev.pranav.reconnect.ui.theme.UltraFamily
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    onVerificationSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val authState by AppContainer.authStore.authState.collectAsState()
    var isVerifying by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Start a timer for verification
    var verificationTimeMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isVerifying) {
        while (isVerifying) {
            delay(100)
            verificationTimeMs += 100
        }
    }

    LaunchedEffect(authState) {
        if (authState == AuthState.Authenticated) {
            println("EmailVerificationScreen: Auth success. Navigating success.")
            isVerifying = false
            onVerificationSuccess()
        }
    }

    // Explicitly check for session on start and periodically
    LaunchedEffect(Unit) {
        while (isVerifying) {
            val session = AppContainer.authStore.getCurrentSession()
            // The authStore will update its state if session exists
            if (AppContainer.authStore.authState.value == AuthState.Authenticated) {
                println("EmailVerificationScreen: Found existing session. Navigating success.")
                isVerifying = false
                onVerificationSuccess()
                break
            }
            delay(1000) // Check every second as fallback
        }
    }

    // Timeout if verification takes too long (extended to 20s for slow networks)
    LaunchedEffect(Unit) {
        delay(20000)
        if (isVerifying) {
            println("EmailVerificationScreen: Verification timed out after 20s")
            isVerifying = false
            errorMessage =
                "Verification is taking longer than expected. Please try signing in manually if your account was already confirmed."
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isVerifying) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(24.dp))
                Text(
                    "Verifying your email...",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SansFontFamily),
                    color = colorScheme.onSurface
                )
            } else if (errorMessage != null) {
                Icon(
                    Icons.Default.Error,
                    null,
                    modifier = Modifier.size(80.dp),
                    tint = colorScheme.error
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Verification Failed",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = UltraFamily),
                    color = colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    errorMessage ?: "Unknown error",
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
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Email Verified!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = UltraFamily),
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Your account has been successfully verified. We're taking you to the app...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
