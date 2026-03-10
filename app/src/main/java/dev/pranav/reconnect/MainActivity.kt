package dev.pranav.reconnect

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import dev.pranav.reconnect.ui.detail.PersonDetailScreen
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.onboarding.OnboardingScreen
import dev.pranav.reconnect.ui.picker.ContactPickerScreen
import dev.pranav.reconnect.ui.theme.CreamBackground
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.NavyDark
import dev.pranav.reconnect.ui.theme.ReConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReConnectTheme {
                ReConnectApp()
            }
        }
    }
}

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CIRCLE("Circle", Icons.Default.People),
    EVENTS("Events", Icons.Default.CalendarMonth),
    SETTINGS("Settings", Icons.Default.Settings)
}

private const val PREFS_NAME = "reconnect_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"

@Composable
fun ReConnectApp() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val onboardingDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    var currentScreen by rememberSaveable { mutableStateOf(if (onboardingDone) "home" else "onboarding") }
    var detailContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    val showBottomNav = currentScreen == "home" || currentScreen == "detail"

    BackHandler(enabled = currentScreen == "detail") {
        currentScreen = "home"
        detailContactId = null
    }

    val background = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(CreamBackground, Color.White)))

    if (!showBottomNav) {
        when (currentScreen) {
            "onboarding" -> OnboardingScreen(
                onPermissionGranted = { currentScreen = "picker" },
                onSkip = {
                    prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
                    currentScreen = "home"
                }
            )
            "picker" -> ContactPickerScreen(
                onContinue = {
                    prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
                    currentScreen = "home"
                },
                onSkip = {
                    prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
                    currentScreen = "home"
                }
            )
        }
    } else {
        Scaffold(
            modifier = background,
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = NavyDark) {
                    AppDestination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = dest == selectedTab,
                            onClick = { selectedTab = dest },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = {
                                Text(
                                    dest.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = NavyDark
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            when {
                currentScreen == "detail" && detailContactId != null -> {
                    PersonDetailScreen(
                        contactId = detailContactId!!,
                        onBack = {
                            currentScreen = "home"
                            detailContactId = null
                        },
                        innerPadding = innerPadding
                    )
                }
                else -> {
                    HomeScreen(
                        onContactClick = { id ->
                            detailContactId = id
                            currentScreen = "detail"
                        },
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}
