package dev.pranav.reconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.BuildConfig
import dev.pranav.reconnect.data.remote.SupabaseAuthManager
import dev.pranav.reconnect.data.remote.avatar
import dev.pranav.reconnect.data.session.AppSessionStore
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

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

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            if (_isLoginEnabled.value) {
                val user = SupabaseAuthManager.client.auth.currentUserOrNull()
                user?.let {
                    _userEmail.value = it.email ?: ""
                    _userName.value = it.userMetadata?.get("full_name")?.jsonPrimitive?.content
                        ?: "ReConnect User"
                }
                _userAvatar.value = SupabaseAuthManager.client.avatar
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            if (_isLoginEnabled.value) {
                val result = SupabaseAuthManager.signOut()
                if (result.isSuccess) {
                    sessionStore.setLoginDone(false)
                }
                _signOutResult.value = result
            }
        }
    }
}
