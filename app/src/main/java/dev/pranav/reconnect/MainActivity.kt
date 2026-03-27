package dev.pranav.reconnect

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
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.data.remote.SupabaseAuthManager
import dev.pranav.reconnect.data.session.AppSessionStore
import dev.pranav.reconnect.ui.add.AddConnectionScreen
import dev.pranav.reconnect.ui.circle.SocialCircleScreen
import dev.pranav.reconnect.ui.detail.PersonDetailScreen
import dev.pranav.reconnect.ui.gallery.GalleryScreen
import dev.pranav.reconnect.ui.gallery.ImagePreviewScreen
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.journey.JourneyScreen
import dev.pranav.reconnect.ui.navigation.*
import dev.pranav.reconnect.ui.onboarding.OnboardingScreen
import dev.pranav.reconnect.ui.picker.ContactPickerScreen
import dev.pranav.reconnect.ui.theme.CreamBackground
import dev.pranav.reconnect.ui.theme.CreamLight
import dev.pranav.reconnect.ui.theme.ReConnectTheme
import dev.pranav.reconnect.ui.user.EmailVerificationScreen
import dev.pranav.reconnect.ui.user.LoginScreen
import dev.pranav.reconnect.ui.user.SignUpScreen

class MainActivity : ComponentActivity() {
    private var pendingIntent: android.content.Intent? = null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(this)
        SupabaseAuthManager.getCurrentSession()
        enableEdgeToEdge()
        setContent {
            ReConnectTheme {
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

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        val data = intent.data
        println("MainActivity: Received intent data: $data")

        val isSupabaseLink = data?.scheme == "reconnect" && data?.host == "confirm"

        if (isSupabaseLink) {
            SupabaseAuthManager.handleDeepLink(intent)

            // Verification UI is hidden from normal flow, so we don't navigate there here either.
            // If the user has disabled email verification in Supabase, the account will be active immediately.
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
fun ReConnectApp(onNavControllerReady: (NavController) -> Unit = {}) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }
    val loginRequired = BuildConfig.ENABLE_LOGIN_GATE
    val startDestination = remember(loginRequired) {
        sessionStore.resolveStartDestination(loginRequired)
    }

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

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
        composable(AppRoute.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    sessionStore.setLoginDone(true)
                    val destination = if (sessionStore.isOnboardingDone()) {
                        AppRoute.MAIN
                    } else {
                        AppRoute.ONBOARDING
                    }
                    navController.navigate(destination) {
                        popUpTo(AppRoute.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(AppRoute.SIGNUP)
                }
            )
        }

        composable(AppRoute.SIGNUP) {
            SignUpScreen(
                onSignUpSuccess = {
                    sessionStore.setLoginDone(true)
                    navController.navigate(AppRoute.ONBOARDING) {
                        popUpTo(AppRoute.SIGNUP) { inclusive = true }
                        popUpTo(AppRoute.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.VERIFY_EMAIL) {
            EmailVerificationScreen(
                onVerificationSuccess = {
                    sessionStore.setLoginDone(true)
                    val destination = if (sessionStore.isOnboardingDone()) {
                        AppRoute.MAIN
                    } else {
                        AppRoute.ONBOARDING
                    }
                    navController.navigate(destination) {
                        popUpTo(AppRoute.VERIFY_EMAIL) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(AppRoute.VERIFY_EMAIL) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.ONBOARDING) {
            OnboardingScreen(
                onPermissionGranted = { navController.navigate(AppRoute.PICKER) },
                onSkip = {
                    sessionStore.setOnboardingDone(true)
                    navController.navigate(AppRoute.MAIN) {
                        popUpTo(AppRoute.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.PICKER) {
            ContactPickerScreen(
                onContinue = {
                    sessionStore.setOnboardingDone(true)
                    navController.navigate(AppRoute.MAIN) {
                        popUpTo(AppRoute.ONBOARDING) { inclusive = true }
                    }
                },
                onSkip = {
                    sessionStore.setOnboardingDone(true)
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
                    navController.openGallery(title, uris)
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
            val (title, uris) = navController.galleryPayload()
            GalleryScreen(
                title = title,
                imageUris = uris,
                onBack = { navController.popBackStack() },
                onImageClick = { index ->
                    navController.openImagePreview(index, uris)
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
            val uris = navController.previewPayload()
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
        containerColor = CreamBackground,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = CreamLight,
            navigationRailContainerColor = CreamLight,
            navigationDrawerContainerColor = CreamLight
        ),
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
                    navController.openGallery(title, uris)
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
