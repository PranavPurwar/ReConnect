package dev.pranav.reconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel: ViewModel() {

    private val _initialName = MutableStateFlow("")
    val initialName: StateFlow<String> = _initialName.asStateFlow()

    private val _initialEmail = MutableStateFlow("")
    val initialEmail: StateFlow<String> = _initialEmail.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _updateResult = MutableStateFlow<Result<Unit>?>(null)
    val updateResult: StateFlow<Result<Unit>?> = _updateResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _initialEmail.value = AppContainer.authStore.currentUserEmail ?: ""
            _initialName.value = AppContainer.authStore.currentUserFullName ?: ""
            _userId.value = AppContainer.authStore.currentUserId ?: ""
        }
    }

    fun updateProfile(fullName: String, email: String, avatarBytes: ByteArray?) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = null
            val result = AppContainer.authStore.updateProfile(fullName, email, avatarBytes)
            _isLoading.value = false
            _updateResult.value = result
        }
    }
}
