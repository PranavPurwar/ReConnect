package dev.pranav.reconnect.core.session

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "reconnect_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_LOGIN_DONE = "login_done"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
private const val KEY_NOTIFY_BIRTHDAYS = "notify_birthdays"
private const val KEY_NOTIFY_CATCHUPS = "notify_catchups"
private const val KEY_REMINDER_FREQUENCY = "reminder_frequency"

enum class StartDestination {
    LOGIN, MAIN, ONBOARDING
}

enum class ReminderFrequency(val label: String) {
    ON_DAY("On the day"),
    DAY_BEFORE_AND_ON_DAY("1 day before & on day"),
    WEEK_AND_DAY_BEFORE_AND_ON("1 week, 1 day before & on day")
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

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
    }

    fun isNotifyBirthdaysEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFY_BIRTHDAYS, true)

    fun setNotifyBirthdays(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFY_BIRTHDAYS, enabled) }
    }

    fun isNotifyCatchUpsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFY_CATCHUPS, true)

    fun setNotifyCatchUps(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFY_CATCHUPS, enabled) }
    }

    fun getReminderFrequency(): ReminderFrequency {
        val name =
            prefs.getString(KEY_REMINDER_FREQUENCY, ReminderFrequency.DAY_BEFORE_AND_ON_DAY.name)
        return try {
            ReminderFrequency.valueOf(name ?: ReminderFrequency.DAY_BEFORE_AND_ON_DAY.name)
        } catch (e: Exception) {
            ReminderFrequency.DAY_BEFORE_AND_ON_DAY
        }
    }

    fun setReminderFrequency(frequency: ReminderFrequency) {
        prefs.edit { putString(KEY_REMINDER_FREQUENCY, frequency.name) }
    }
}
