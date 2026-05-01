package dev.pranav.reconnect.core.session

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "reconnect_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"
private const val KEY_LOGIN_DONE = "login_done"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
private const val KEY_NOTIFY_BIRTHDAYS = "notify_birthdays"
private const val KEY_NOTIFY_CATCHUPS = "notify_catchups"
private const val KEY_NOTIFY_MEMORIES = "notify_memories"
private const val KEY_REMINDER_FREQUENCY = "reminder_frequency"
private const val KEY_MAP_STYLE = "map_style"

enum class StartDestination {
    LOGIN, MAIN, ONBOARDING
}

enum class ReminderFrequency(val label: String) {
    ON_DAY("On the day"),
    DAY_BEFORE_AND_ON_DAY("1 day before & on day"),
    WEEK_AND_DAY_BEFORE_AND_ON("1 week, 1 day before & on day")
}

enum class MapStyle(val label: String, val styleUri: String) {
    AWS_HYBRID(
        "AWS hybrid",
        "https://maps.geo.eu-west-1.amazonaws.com/v2/styles/Hybrid/descriptor?key=v1.public.eyJqdGkiOiJiOTNkYjBlZi04OWUzLTQxMGUtODFhMC0zYjZjZjVmZWZmMDgifYtukap0NBaJpcrS6Vit9j03GJgK9Bn-RSu5UCe3jkdSql2kKp3IEgLPtyLssbmKUdVO11sXddjK3ZOZy8V6QG0olv0K_1tOxyMIe4DAO3IV6H4VzHWiaXlbSakGiEgFLuHBdcfLDeMotye7N6rSRxuZb0CN9ytH9VjLly6-NEBRZezO_qPQyvdTFdeZsARIpL0f9YVpxPxPVvUcAWYCk5LpaPseRCDPrY5SlCdA1ZKqUA4F9RzxSTxB73Fel_SoNDkCNaux1VposBu791-uUpDzUpr7leKckrPXrpZ2hwnFbafVxFV9vq4fLTpB5KoBksuLfGNIwAx1RLLxWuMhE4c.ZGQzZDY2OGQtMWQxMy00ZTEwLWIyZGUtOGVjYzUzMjU3OGE4&color-scheme=Light"
    ),
    BRIGHT(
        "Bright",
        "https://tiles.openfreemap.org/styles/bright"
    ),
    LIBERTY(
        "Liberty",
        "https://tiles.openfreemap.org/styles/liberty"
    )
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

    fun isNotifyMemoriesEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFY_MEMORIES, true)

    fun setNotifyMemories(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFY_MEMORIES, enabled) }
    }

    fun getReminderFrequency(): ReminderFrequency {
        val name =
            prefs.getString(KEY_REMINDER_FREQUENCY, ReminderFrequency.DAY_BEFORE_AND_ON_DAY.name)
        return try {
            ReminderFrequency.valueOf(name ?: ReminderFrequency.DAY_BEFORE_AND_ON_DAY.name)
        } catch (_: Exception) {
            ReminderFrequency.DAY_BEFORE_AND_ON_DAY
        }
    }

    fun setReminderFrequency(frequency: ReminderFrequency) {
        prefs.edit { putString(KEY_REMINDER_FREQUENCY, frequency.name) }
    }

    fun getMapStyle(): MapStyle {
        val name = prefs.getString(KEY_MAP_STYLE, MapStyle.AWS_HYBRID.name)
        return try {
            MapStyle.valueOf(name ?: MapStyle.AWS_HYBRID.name)
        } catch (_: Exception) {
            MapStyle.AWS_HYBRID
        }
    }

    fun setMapStyle(style: MapStyle) {
        prefs.edit { putString(KEY_MAP_STYLE, style.name) }
    }
}
