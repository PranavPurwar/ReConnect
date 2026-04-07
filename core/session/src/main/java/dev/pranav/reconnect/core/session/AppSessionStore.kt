package dev.pranav.reconnect.core.session

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "reconnect_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_LOGIN_DONE = "login_done"

enum class StartDestination {
    LOGIN, MAIN, ONBOARDING
}

class AppSessionStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun resolveStartDestination(loginRequired: Boolean): StartDestination = when {
        loginRequired && !isLoginDone() -> StartDestination.LOGIN
        isOnboardingDone() -> StartDestination.MAIN
        else -> StartDestination.ONBOARDING
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
