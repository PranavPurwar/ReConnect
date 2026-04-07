package dev.pranav.reconnect.core.storage

import kotlinx.coroutines.flow.StateFlow

enum class AuthState {
    Loading,
    Authenticated,
    NotAuthenticated,
    Unknown
}

interface AuthStore {
    val authState: StateFlow<AuthState>
    val currentUserId: String?
    val currentUserEmail: String?
    val currentUserFullName: String?

    fun handleDeepLink(intent: android.content.Intent)
    fun getCurrentSession()

    suspend fun signIn(email: String, pass: String): Result<Unit>
    suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        avatar: ByteArray?
    ): Result<Unit>

    suspend fun updateProfile(fullName: String, email: String, avatar: ByteArray?): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}

