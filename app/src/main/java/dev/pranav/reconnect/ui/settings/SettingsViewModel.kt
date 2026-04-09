package dev.pranav.reconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.BuildConfig
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.ReminderFrequency
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sessionStore: AppSessionStore
): ViewModel() {

    private val _isLoginEnabled = MutableStateFlow(BuildConfig.ENABLE_LOGIN_GATE)
    val isLoginEnabled: StateFlow<Boolean> = _isLoginEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sessionStore.isNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notifyBirthdays = MutableStateFlow(sessionStore.isNotifyBirthdaysEnabled())
    val notifyBirthdays: StateFlow<Boolean> = _notifyBirthdays.asStateFlow()

    private val _notifyCatchUps = MutableStateFlow(sessionStore.isNotifyCatchUpsEnabled())
    val notifyCatchUps: StateFlow<Boolean> = _notifyCatchUps.asStateFlow()

    private val _reminderFrequency = MutableStateFlow(sessionStore.getReminderFrequency())
    val reminderFrequency: StateFlow<ReminderFrequency> = _reminderFrequency.asStateFlow()

    private val _signOutResult = MutableStateFlow<Result<Unit>?>(null)
    val signOutResult: StateFlow<Result<Unit>?> = _signOutResult.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            if (_isLoginEnabled.value) {
                _userEmail.value = AppContainer.authStore.currentUserEmail ?: ""
                _userName.value = AppContainer.authStore.currentUserFullName ?: "ReConnect User"
                _userId.value = AppContainer.authStore.currentUserId ?: ""
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        sessionStore.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }

    fun toggleNotifyBirthdays(enabled: Boolean) {
        sessionStore.setNotifyBirthdays(enabled)
        _notifyBirthdays.value = enabled
    }

    fun toggleNotifyCatchUps(enabled: Boolean) {
        sessionStore.setNotifyCatchUps(enabled)
        _notifyCatchUps.value = enabled
    }

    fun updateReminderFrequency(frequency: ReminderFrequency) {
        sessionStore.setReminderFrequency(frequency)
        _reminderFrequency.value = frequency
    }

    fun signOut() {
        viewModelScope.launch {
            if (_isLoginEnabled.value) {
                val result = AppContainer.authStore.signOut()
                if (result.isSuccess) {
                    sessionStore.setLoginDone(false)
                }
                _signOutResult.value = result
            } else {
                _signOutResult.value = Result.success(Unit)
            }
        }
    }
}
