package dev.pranav.reconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.StartDestination
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.circle.SocialCircleScreen
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.journey.JourneyScreen
import dev.pranav.reconnect.ui.navigation.AppRoute
import dev.pranav.reconnect.ui.navigation.ReConnectNavGraph
import dev.pranav.reconnect.ui.navigation.openGallery
import dev.pranav.reconnect.ui.settings.SettingsScreen
import dev.pranav.reconnect.ui.settings.SettingsViewModel
import dev.pranav.reconnect.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private var pendingIntent: Intent? = null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(this)
        AppContainer.authStore.getCurrentSession()
        enableEdgeToEdge()
        setContent {
            AppTheme {
                ReConnectApp { controller ->
                    navController = controller
                    pendingIntent?.let {
                        handleIntent(it)
                        pendingIntent = null
                    }
                }
            }
        }
        if (intent?.data != null) {
            pendingIntent = intent
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        println("MainActivity: Received intent data: $data")

        val isSupabaseLink = data?.scheme == "reconnect" && data.host == "confirm"

        if (isSupabaseLink) {
            AppContainer.authStore.handleDeepLink(intent)
        }
    }
}

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CIRCLE("Circle", Icons.Default.People),
    HISTORY("Journey", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun ReConnectApp(
    onNavControllerReady: (NavHostController) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }

    val startDest = remember {
        val dest = sessionStore.resolveStartDestination(true)
        when (dest) {
            StartDestination.LOGIN -> AppRoute.Login
            StartDestination.MAIN -> AppRoute.Main
            StartDestination.ONBOARDING -> AppRoute.Onboarding
        }
    }

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

    ReConnectNavGraph(
        navController = navController,
        startDestination = startDest
    )
}

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }
    var selectedTab by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    NavigationSuiteScaffold(
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationRailContainerColor = MaterialTheme.colorScheme.surface,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.surface
        ),
        navigationSuiteItems = {
            AppDestination.entries.forEach { dest ->
                item(
                    selected = selectedTab == dest,
                    onClick = { selectedTab = dest },
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) }
                )
            }
        }
    ) {
        AnimatedContent(
            targetState = selectedTab,
            label = "MainTabs",
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
            }
        ) { targetTab ->
            when (targetTab) {
                AppDestination.CIRCLE -> SocialCircleScreen(
                    onContactClick = { id -> navController.navigate(AppRoute.ConnectionDetail(id)) },
                    onAddClick = { navController.navigate(AppRoute.AddConnection(null)) }
                )

                AppDestination.HISTORY -> JourneyScreen(
                    onOpenGallery = { title, uris ->
                        navController.openGallery(title, uris)
                    }
                )

                AppDestination.SETTINGS -> {
                    val settingsViewModel = remember { SettingsViewModel(sessionStore) }
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onEditProfileClick = { navController.navigate(AppRoute.EditProfile) },
                        onSignOutSuccess = {
                            navController.navigate(AppRoute.Login) {
                                popUpTo<AppRoute.Main> { inclusive = true }
                            }
                        },
                        onPrivacyPolicyClick = { navController.navigate(AppRoute.PrivacyPolicy) }
                    )
                }

                else -> HomeScreen(
                    onContactClick = { id -> navController.navigate(AppRoute.ConnectionDetail(id)) },
                    onAddClick = { navController.navigate(AppRoute.AddConnection(null)) },
                    onViewAllCatchUpsClick = { selectedTab = AppDestination.CIRCLE }
                )
            }
        }
    }
}
