package dev.pranav.reconnect.ui.main

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.di.AppViewModelProvider
import dev.pranav.reconnect.ui.circle.SocialCircleScreen
import dev.pranav.reconnect.ui.circle.SocialCircleViewModel
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.home.HomeViewModel
import dev.pranav.reconnect.ui.journey.JourneyScreen
import dev.pranav.reconnect.ui.journey.JourneyViewModel
import dev.pranav.reconnect.ui.navigation.AppRoute
import dev.pranav.reconnect.ui.navigation.openGallery
import dev.pranav.reconnect.ui.settings.SettingsScreen
import dev.pranav.reconnect.ui.settings.SettingsViewModel
import kotlinx.coroutines.FlowPreview

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CIRCLE("Circle", Icons.Default.People),
    HISTORY("Journey", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(FlowPreview::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }
    val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val socialCircleViewModel: SocialCircleViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
    val journeyViewModel: JourneyViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val settingsViewModel = remember { SettingsViewModel(sessionStore) }
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
                    onAddClick = { navController.navigate(AppRoute.AddConnection(null)) },
                    viewModel = socialCircleViewModel
                )

                AppDestination.HISTORY -> JourneyScreen(
                    onOpenGallery = { title, uris ->
                        navController.openGallery(title, uris)
                    },
                    viewModel = journeyViewModel
                )

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onEditProfileClick = { navController.navigate(AppRoute.EditProfile) },
                        onSignOutSuccess = {
                            navController.navigate(AppRoute.Login) {
                                popUpTo<AppRoute.Main> { inclusive = true }
                            }
                        },
                        onPrivacyPolicyClick = { navController.navigate(AppRoute.PrivacyPolicy) },
                        onNotificationsSettingsClick = { navController.navigate(AppRoute.NotificationSettings) },
                        onSubscriptionPlanClick = { navController.navigate(AppRoute.SubscriptionPlan) }
                    )
                }

                else -> HomeScreen(
                    onContactClick = { id -> navController.navigate(AppRoute.ConnectionDetail(id)) },
                    onMomentClick = { id -> /* Need a moment preview screen first */ },
                    onAddClick = { navController.navigate(AppRoute.AddConnection(null)) },
                    onViewAllCatchUpsClick = { selectedTab = AppDestination.CIRCLE },
                    onMapClick = { navController.navigate(AppRoute.Map) },
                    viewModel = homeViewModel
                )
            }
        }
    }
}

