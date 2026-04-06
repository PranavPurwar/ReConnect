package dev.pranav.reconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.remote.SupabaseAuthManager
import dev.pranav.reconnect.data.remote.avatar
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class EditProfileViewModel: ViewModel() {

    private val _initialName = MutableStateFlow("")
    val initialName: StateFlow<String> = _initialName.asStateFlow()

    private val _initialEmail = MutableStateFlow("")
    val initialEmail: StateFlow<String> = _initialEmail.asStateFlow()

    private val _currentAvatarUrl = MutableStateFlow<String?>(null)
    val currentAvatarUrl: StateFlow<String?> = _currentAvatarUrl.asStateFlow()

    private val _updateResult = MutableStateFlow<Result<Unit>?>(null)
    val updateResult: StateFlow<Result<Unit>?> = _updateResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = SupabaseAuthManager.client.auth.currentUserOrNull()
            user?.let {
                _initialEmail.value = it.email ?: ""
                _initialName.value = it.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: ""
            }
            _currentAvatarUrl.value = SupabaseAuthManager.client.avatar
        }
    }

    fun updateProfile(fullName: String, email: String, avatarBytes: ByteArray?) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = null
            val result = SupabaseAuthManager.updateProfile(fullName, email, avatarBytes)
            _isLoading.value = false
            _updateResult.value = result
            if (result.isSuccess && avatarBytes != null) {
                // Refresh avatar reference if changed
                _currentAvatarUrl.value =
                    "${SupabaseAuthManager.client.avatar}?t=${System.currentTimeMillis()}"
            }
        }
    }
}
