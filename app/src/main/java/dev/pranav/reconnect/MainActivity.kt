package dev.pranav.reconnect

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.ReConnectApp
import dev.pranav.reconnect.ui.theme.AppTheme
import dev.pranav.reconnect.worker.DailyReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var pendingIntent: Intent? = null
    private var navController: NavController? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupDailyReminders()
        }
    }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            setupDailyReminders()
        }
    }

    private fun setupDailyReminders() {
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyReminderWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
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
