package dev.pranav.reconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.BuildConfig
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.data.port.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sessionStore: AppSessionStore
): ViewModel() {

    private val _isLoginEnabled = MutableStateFlow(BuildConfig.ENABLE_LOGIN_GATE)
    val isLoginEnabled: StateFlow<Boolean> = _isLoginEnabled.asStateFlow()

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
