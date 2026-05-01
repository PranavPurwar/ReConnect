package dev.pranav.reconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.BuildConfig
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.ReminderFrequency
import dev.pranav.reconnect.core.storage.AuthState
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

    private val _notifyMemories = MutableStateFlow(sessionStore.isNotifyMemoriesEnabled())
    val notifyMemories: StateFlow<Boolean> = _notifyMemories.asStateFlow()

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

    private val _authState: StateFlow<AuthState> = AppContainer.authStore.authState
    val syncStatus: StateFlow<String> =
        combine(_isLoginEnabled, _authState) { loginEnabled, authState ->
            if (!loginEnabled) {
                "Local-only storage"
            } else {
                when (authState) {
                    AuthState.Loading -> "Checking connection..."
                    AuthState.Authenticated -> "Cloud sync enabled"
                    AuthState.NotAuthenticated -> "Not signed in"
                    AuthState.Unknown -> "Sync status unknown"
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (BuildConfig.ENABLE_LOGIN_GATE) "Checking connection..." else "Local-only storage"
        )

    init {
        loadUserProfile()
        refreshSyncStatus()
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

    fun toggleNotifyMemories(enabled: Boolean) {
        sessionStore.setNotifyMemories(enabled)
        _notifyMemories.value = enabled
    }

    fun updateReminderFrequency(frequency: ReminderFrequency) {
        sessionStore.setReminderFrequency(frequency)
        _reminderFrequency.value = frequency
    }

    fun refreshSyncStatus() {
        viewModelScope.launch {
            AppContainer.authStore.getCurrentSession()
            if (_isLoginEnabled.value) {
                loadUserProfile()
            }
        }
    }

    fun prepareExportJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val contacts = AppContainer.contactStore.contacts.first()
            val moments = AppContainer.momentStore.moments.first()
            onReady(Json.encodeToString(BackupPayload(contacts, moments)))
        }
    }

    fun restoreBackupJson(backupJson: String) {
        viewModelScope.launch {
            runCatching {
                val payload = Json.decodeFromString<BackupPayload>(backupJson)
                payload.contacts.forEach { contact ->
                    val existingContact = AppContainer.contactStore.findById(contact.id)
                    if (existingContact != null) {
                        AppContainer.contactStore.updateContact(contact)
                    } else {
                        AppContainer.contactStore.addContact(contact)
                    }
                }

                val existingMomentIds =
                    AppContainer.momentStore.moments.first().map { it.id }.toSet()
                payload.moments.forEach { moment ->
                    if (moment.id in existingMomentIds) {
                        AppContainer.momentStore.updateMoment(moment)
                    } else {
                        AppContainer.momentStore.addMoment(moment)
                    }
                }
            }
        }
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

@Serializable
private data class BackupPayload(
    val contacts: List<Contact>,
    val moments: List<PastMoment>
)
