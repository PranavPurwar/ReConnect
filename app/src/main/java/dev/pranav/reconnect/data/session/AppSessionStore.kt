package dev.pranav.reconnect.data.session

import android.content.Context
import androidx.core.content.edit
import dev.pranav.reconnect.ui.navigation.AppRoute

private const val PREFS_NAME = "reconnect_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_LOGIN_DONE = "login_done"

class AppSessionStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun resolveStartDestination(loginRequired: Boolean): String = when {
        loginRequired && !isLoginDone() -> AppRoute.LOGIN
        isOnboardingDone() -> AppRoute.MAIN
        else -> AppRoute.ONBOARDING
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(done: Boolean = true) {
        prefs.edit { putBoolean(KEY_ONBOARDING_DONE, done) }
    }

    fun isLoginDone(): Boolean = prefs.getBoolean(KEY_LOGIN_DONE, false)

    fun setLoginDone(done: Boolean = true) {
        prefs.edit { putBoolean(KEY_LOGIN_DONE, done) }
    }
}

