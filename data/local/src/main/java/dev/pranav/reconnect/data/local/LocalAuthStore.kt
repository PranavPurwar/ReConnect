package dev.pranav.reconnect.data.local

import android.content.Intent
import dev.pranav.reconnect.core.storage.AuthState
import dev.pranav.reconnect.core.storage.AuthStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocalAuthStore: AuthStore {
    private val _authState = MutableStateFlow(AuthState.Authenticated)
    override val authState: StateFlow<AuthState> = _authState

    override val currentUserId: String = "local_user"
    override val currentUserEmail: String = "local@example.com"
    override val currentUserFullName: String = "Local User"

    override fun handleDeepLink(intent: Intent) {}

    override fun getCurrentSession() {}

    override suspend fun signIn(email: String, pass: String): Result<Unit> {
        _authState.value = AuthState.Authenticated
        return Result.success(Unit)
    }

    override suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        avatar: ByteArray?
    ): Result<Unit> {
        _authState.value = AuthState.Authenticated
        return Result.success(Unit)
    }

    override suspend fun updateProfile(
        fullName: String,
        email: String,
        avatar: ByteArray?
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        _authState.value = AuthState.NotAuthenticated
        return Result.success(Unit)
    }
}

