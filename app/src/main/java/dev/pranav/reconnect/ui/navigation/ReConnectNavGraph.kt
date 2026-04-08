package dev.pranav.reconnect.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.pranav.reconnect.MainScreen
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.di.AppViewModelProvider
import dev.pranav.reconnect.ui.add.AddConnectionScreen
import dev.pranav.reconnect.ui.detail.ConnectionDetailScreen
import dev.pranav.reconnect.ui.gallery.GalleryScreen
import dev.pranav.reconnect.ui.gallery.ImagePreviewScreen
import dev.pranav.reconnect.ui.onboarding.OnboardingScreen
import dev.pranav.reconnect.ui.picker.ContactPickerScreen
import dev.pranav.reconnect.ui.privacy.PrivacyPolicyScreen
import dev.pranav.reconnect.ui.settings.EditProfileScreen
import dev.pranav.reconnect.ui.settings.EditProfileViewModel
import dev.pranav.reconnect.ui.user.EmailVerificationScreen
import dev.pranav.reconnect.ui.user.LoginScreen
import dev.pranav.reconnect.ui.user.SignUpScreen

@Composable
fun ReConnectNavGraph(
    navController: NavHostController,
    startDestination: Any
) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }

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
        composable<AppRoute.Login> {
            LoginScreen(
                onLoginSuccess = {
                    sessionStore.setLoginDone(true)
                    val destination = if (sessionStore.isOnboardingDone()) {
                        AppRoute.Main
                    } else {
                        AppRoute.Onboarding
                    }
                    navController.navigate(destination) {
                        popUpTo(AppRoute.Login) { inclusive = true }
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(AppRoute.SignUp)
                }
            )
        }

        composable<AppRoute.SignUp> {
            SignUpScreen(
                onSignUpSuccess = {
                    sessionStore.setLoginDone(true)
                    navController.navigate(AppRoute.Onboarding) {
                        popUpTo<AppRoute.SignUp> { inclusive = true }
                        popUpTo<AppRoute.Login> { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.VerifyEmail> {
            EmailVerificationScreen(
                onVerificationSuccess = {
                    sessionStore.setLoginDone(true)
                    val destination = if (sessionStore.isOnboardingDone()) {
                        AppRoute.Main
                    } else {
                        AppRoute.Onboarding
                    }
                    navController.navigate(destination) {
                        popUpTo<AppRoute.VerifyEmail> { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo<AppRoute.VerifyEmail> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Onboarding> {
            OnboardingScreen(
                onPermissionGranted = { navController.navigate(AppRoute.Picker) },
                onSkip = {
                    sessionStore.setOnboardingDone(true)
                    navController.navigate(AppRoute.Main) {
                        popUpTo<AppRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Picker> {
            ContactPickerScreen(
                onContinue = {
                    sessionStore.setOnboardingDone(true)
                    navController.navigate(AppRoute.Main) {
                        popUpTo<AppRoute.Onboarding> { inclusive = true }
                    }
                },
                onSkip = {
                    sessionStore.setOnboardingDone(true)
                    navController.navigate(AppRoute.Main) {
                        popUpTo<AppRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.EditProfile> {
            val editProfileViewModel: EditProfileViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            EditProfileScreen(
                viewModel = editProfileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Main> {
            MainScreen(navController)
        }

        composable<AppRoute.ConnectionDetail> { backStack ->
            val route = backStack.toRoute<AppRoute.ConnectionDetail>()
            ConnectionDetailScreen(
                contactId = route.contactId,
                onBack = { navController.popBackStack() },
                onEditDetails = { id -> navController.navigate(AppRoute.AddConnection(id)) },
                onOpenGallery = { title, uris ->
                    navController.openGallery(title, uris)
                }
            )
        }

        composable<AppRoute.AddConnection> { backStack ->
            val route = backStack.toRoute<AppRoute.AddConnection>()
            AddConnectionScreen(
                contactIdToEdit = route.contactId,
                onBack = { navController.popBackStack() },
                onAdded = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Gallery> {
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

        composable<AppRoute.ImagePreview> { backStack ->
            val route = backStack.toRoute<AppRoute.ImagePreview>()
            val uris = navController.previewPayload()
            ImagePreviewScreen(
                imageUris = uris,
                initialIndex = route.index,
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.PrivacyPolicy> {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
