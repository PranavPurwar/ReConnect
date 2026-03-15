package dev.pranav.reconnect.ui.user

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pranav.reconnect.ui.theme.PlayfairFamily
import dev.pranav.reconnect.ui.theme.UltraFamily

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}, onCreateAccountClick: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFfce7f3),
                        Color(0xFFfef3c7),
                        Color(0xFFd1fae5)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFEEAD2B).copy(alpha = 0.2f),
                radius = 150.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(-100.dp.toPx(), -200.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFfce7f3).copy(alpha = 0.4f),
                radius = 200.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width + 100.dp.toPx(), size.height + 100.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.4f),
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    tint = Color(0xFFEEAD2B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = UltraFamily
                ),
                color = Color(0xFF1a1a2e),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join the ReConnect community",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp,
                    fontFamily = PlayfairFamily
                ),
                color = Color(0xFF5a5a7a),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.7f),
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    EmailField(email) { email = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    PasswordField(
                        password = password,
                        passwordVisible = passwordVisible,
                        onPasswordChange = { password = it },
                        onVisibilityToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "Forgot Password?",
                            color = Color(0xFFEEAD2B),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { isLoading = true; onLoginSuccess() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEAD2B)),
                        shape = RoundedCornerShape(32.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Sign In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(
                        color = Color(0xFFe2e8f0),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Or connect with",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9ca3af),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialLoginButton(
                            icon = Icons.Default.Person,
                            contentDescription = "Google Login"
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        SocialLoginButton(
                            icon = Icons.Default.Person,
                            contentDescription = "Apple Login",
                            isDark = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "New to ReConnect?",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4a5568),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCreateAccountClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    "Create Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1a1a2e)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmailField(email: String, onEmailChange: (String) -> Unit) {
    Column {
        Text(
            "Email Address",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4a5568),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("hello@reconnect.com") },
            leadingIcon = {
                Icon(
                    Icons.Default.Mail,
                    contentDescription = null,
                    tint = Color(0xFF9ca3af),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color(0xFF1a1a2e),
                unfocusedTextColor = Color(0xFF1a1a2e),
                focusedPlaceholderColor = Color(0xFF9ca3af),
                unfocusedPlaceholderColor = Color(0xFF9ca3af)
            ),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PasswordField(
    password: String,
    passwordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    Column {
        Text(
            "Password",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4a5568),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("••••••••") },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF9ca3af),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF9ca3af),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color(0xFF1a1a2e),
                unfocusedTextColor = Color(0xFF1a1a2e),
                focusedPlaceholderColor = Color(0xFF9ca3af),
                unfocusedPlaceholderColor = Color(0xFF9ca3af)
            ),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SocialLoginButton(
    icon: ImageVector,
    contentDescription: String,
    isDark: Boolean = false
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFe5e7eb),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color.Black else Color.White,
        shadowElevation = 2.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isDark) Color.White else Color(0xFF1a1a2e),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        )
    }
}
