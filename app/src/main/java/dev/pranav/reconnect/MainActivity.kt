package dev.pranav.reconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.StartDestination
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.add.AddConnectionScreen
import dev.pranav.reconnect.ui.circle.SocialCircleScreen
import dev.pranav.reconnect.ui.detail.ConnectionDetailScreen
import dev.pranav.reconnect.ui.gallery.GalleryScreen
import dev.pranav.reconnect.ui.gallery.ImagePreviewScreen
import dev.pranav.reconnect.ui.home.HomeScreen
import dev.pranav.reconnect.ui.journey.JourneyScreen
import dev.pranav.reconnect.ui.navigation.*
import dev.pranav.reconnect.ui.onboarding.OnboardingScreen
import dev.pranav.reconnect.ui.picker.ContactPickerScreen
import dev.pranav.reconnect.ui.settings.EditProfileScreen
import dev.pranav.reconnect.ui.settings.EditProfileViewModel
import dev.pranav.reconnect.ui.settings.SettingsScreen
import dev.pranav.reconnect.ui.settings.SettingsViewModel
import dev.pranav.reconnect.ui.theme.AppTheme
import dev.pranav.reconnect.ui.theme.CreamBackground
import dev.pranav.reconnect.ui.theme.CreamLight
import dev.pranav.reconnect.ui.user.EmailVerificationScreen
import dev.pranav.reconnect.ui.user.LoginScreen
import dev.pranav.reconnect.ui.user.SignUpScreen

class MainActivity : ComponentActivity() {
    private var pendingIntent: android.content.Intent? = null
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

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
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
    intent: Intent? = null,
    onNavControllerReady: (NavHostController) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }

    val startDest = remember {
        val dest = sessionStore.resolveStartDestination(BuildConfig.ENABLE_LOGIN_GATE)
        when (dest) {
            StartDestination.LOGIN -> AppRoute.LOGIN
            StartDestination.MAIN -> AppRoute.MAIN
            StartDestination.ONBOARDING -> AppRoute.ONBOARDING
        }
    }

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavControllerReady(navController)
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
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
            route = AppRoute.EDIT_PROFILE,
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
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
            val editProfileViewModel: EditProfileViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel()
            EditProfileScreen(
                viewModel = editProfileViewModel,
                onBack = { navController.popBackStack() }
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
            route = AppRoute.CONNECTION_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStack ->
            ConnectionDetailScreen(
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
            }),
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
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
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }
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
                    onContactClick = { id -> navController.navigate(AppRoute.connectionDetail(id)) },
                    onAddClick = { navController.navigate(AppRoute.addConnection(null)) }
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
                        onEditProfileClick = { navController.navigate(AppRoute.EDIT_PROFILE) },
                        onSignOutSuccess = {
                            navController.navigate(AppRoute.LOGIN) {
                                popUpTo(AppRoute.MAIN) { inclusive = true }
                            }
                        }
                    )
                }

                else -> HomeScreen(
                    onContactClick = { id -> navController.navigate(AppRoute.connectionDetail(id)) },
                    onAddClick = { navController.navigate(AppRoute.addConnection(null)) },
                    onViewAllCatchUpsClick = { selectedTab = AppDestination.CIRCLE }
                )
            }
        }
    }
}
