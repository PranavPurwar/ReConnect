package dev.pranav.reconnect.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.StartDestination
import dev.pranav.reconnect.ui.navigation.AppRoute
import dev.pranav.reconnect.ui.navigation.ReConnectNavGraph
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun ReConnectApp(
    onNavControllerReady: (NavHostController) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionStore = remember(context) { AppSessionStore(context) }

    val startDest: Any = remember {
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
