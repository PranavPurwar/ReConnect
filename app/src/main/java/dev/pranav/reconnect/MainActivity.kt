package dev.pranav.reconnect

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.add.AddConnectionScreen
import dev.pranav.reconnect.ui.circle.SocialCircleScreen
import dev.pranav.reconnect.ui.detail.PersonDetailScreen
import dev.pranav.reconnect.ui.gallery.GalleryScreen
import dev.pranav.reconnect.ui.gallery.ImagePreviewScreen
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.journey.JourneyScreen
import dev.pranav.reconnect.ui.navigation.AppRoute
import dev.pranav.reconnect.ui.onboarding.OnboardingScreen
import dev.pranav.reconnect.ui.picker.ContactPickerScreen
import dev.pranav.reconnect.ui.theme.ReConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(this)
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
    HISTORY("Journey", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

private const val PREFS_NAME = "reconnect_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"

@Composable
fun ReConnectApp() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val onboardingDone = remember { prefs.getBoolean(KEY_ONBOARDING_DONE, false) }
    val startDestination = if (onboardingDone) AppRoute.MAIN else AppRoute.ONBOARDING

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(250)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(250)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(250)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(250)
            )
        }
    ) {
        composable(AppRoute.ONBOARDING) {
            OnboardingScreen(
                onPermissionGranted = { navController.navigate(AppRoute.PICKER) },
                onSkip = {
                    prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
                    navController.navigate(AppRoute.MAIN) {
                        popUpTo(AppRoute.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.PICKER) {
            ContactPickerScreen(
                onContinue = {
                    prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
                    navController.navigate(AppRoute.MAIN) {
                        popUpTo(AppRoute.ONBOARDING) { inclusive = true }
                    }
                },
                onSkip = {
                    prefs.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
                    navController.navigate(AppRoute.MAIN) {
                        popUpTo(AppRoute.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppRoute.MAIN,
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            }
        ) {
            MainScreen(navController)
        }

        composable(
            route = AppRoute.PERSON_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStack ->
            PersonDetailScreen(
                contactId = backStack.arguments!!.getString("contactId")!!,
                onBack = { navController.popBackStack() },
                onEditDetails = { id -> navController.navigate(AppRoute.addConnection(id)) },
                onOpenGallery = { title, uris ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("gallery_title", title)
                    navController.currentBackStackEntry?.savedStateHandle?.set("gallery_uris", ArrayList(uris))
                    navController.navigate(AppRoute.GALLERY)
                }
            )
        }

        composable(
            route = AppRoute.ADD_CONNECTION,
            arguments = listOf(navArgument("contactId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStack ->
            AddConnectionScreen(
                contactIdToEdit = backStack.arguments?.getString("contactId"),
                onBack = { navController.popBackStack() },
                onAdded = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.GALLERY,
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            }
        ) {
            val handle = navController.previousBackStackEntry?.savedStateHandle
            val title = handle?.get<String>("gallery_title") ?: ""
            val uris = handle?.get<ArrayList<String>>("gallery_uris") ?: arrayListOf()
            GalleryScreen(
                title = title,
                imageUris = uris,
                onBack = { navController.popBackStack() },
                onImageClick = { index ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("preview_uris", uris)
                    navController.navigate(AppRoute.imagePreview(index))
                }
            )
        }

        composable(
            route = AppRoute.IMAGE_PREVIEW,
            arguments = listOf(navArgument("index") { type = NavType.IntType }),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) }
        ) { backStack ->
            val index = backStack.arguments!!.getInt("index")
            val uris = navController.previousBackStackEntry?.savedStateHandle
                ?.get<ArrayList<String>>("preview_uris") ?: arrayListOf()
            ImagePreviewScreen(
                imageUris = uris,
                initialIndex = index,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainScreen(navController: NavController) {
    var selectedTab by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach { dest ->
                val resolvedTab = if (dest == AppDestination.SETTINGS) AppDestination.HOME else dest
                item(
                    selected = selectedTab == resolvedTab,
                    onClick = { selectedTab = resolvedTab },
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) }
                )
            }
        }
    ) {
        when (selectedTab) {
            AppDestination.CIRCLE -> SocialCircleScreen(
                onContactClick = { id -> navController.navigate(AppRoute.personDetail(id)) },
                onAddClick = { navController.navigate(AppRoute.addConnection(null)) }
            )
            AppDestination.HISTORY -> JourneyScreen(
                onOpenGallery = { title, uris ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("gallery_title", title)
                    navController.currentBackStackEntry?.savedStateHandle?.set("gallery_uris", ArrayList(uris))
                    navController.navigate(AppRoute.GALLERY)
                }
            )
            else -> HomeScreen(
                onContactClick = { id -> navController.navigate(AppRoute.personDetail(id)) },
                onAddClick = { navController.navigate(AppRoute.addConnection(null)) },
                onViewAllCatchUpsClick = { selectedTab = AppDestination.CIRCLE }
            )
        }
    }
}
