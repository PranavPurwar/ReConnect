package dev.pranav.reconnect

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import dev.pranav.reconnect.data.repository.SharedPrefsContactStore
import dev.pranav.reconnect.ui.add.AddConnectionScreen
import dev.pranav.reconnect.ui.detail.PersonDetailScreen
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.onboarding.OnboardingScreen
import dev.pranav.reconnect.ui.picker.ContactPickerScreen
import dev.pranav.reconnect.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPrefsContactStore.init(this)
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

    BackHandler(enabled = currentScreen == "add") {
        currentScreen = "home"
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
            "add" -> AddConnectionScreen(
                onBack = { currentScreen = "home" },
                onAdded = { currentScreen = "home" }
            )
        }
    } else {
        Scaffold(
            modifier = background,
            containerColor = Color.Transparent,
            bottomBar = {
                ReConnectBottomNav(
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
                )
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
                        onAddClick = { currentScreen = "add" },
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun ReConnectBottomNav(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(MediumGray.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    .align(Alignment.TopCenter)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDestination.entries.forEach { dest ->
                    val isSelected = dest == selected
                    val tint = if (isSelected) GoldPrimary else CharcoalText.copy(alpha = 0.45f)
                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = { onSelect(dest) }
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(dest.icon, contentDescription = dest.label, tint = tint, modifier = Modifier.size(24.dp))
                        Text(
                            text = dest.label.uppercase(),
                            color = tint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}
