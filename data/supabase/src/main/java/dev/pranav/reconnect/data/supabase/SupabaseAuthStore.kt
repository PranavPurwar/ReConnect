package dev.pranav.reconnect.data.supabase

import android.content.Intent
import dev.pranav.reconnect.core.storage.AuthState
import dev.pranav.reconnect.core.storage.AuthStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class SupabaseAuthStore(private val client: SupabaseClient): AuthStore {
    private val _authState = MutableStateFlow(AuthState.Unknown)
    override val authState: StateFlow<AuthState> = _authState

    override val currentUserId: String?
        get() = client.auth.currentUserOrNull()?.id

    override val currentUserEmail: String?
        get() = client.auth.currentUserOrNull()?.email

    override val currentUserFullName: String?
        get() = client.auth.currentUserOrNull()?.userMetadata?.get("full_name")?.jsonPrimitive?.content

    init {
        CoroutineScope(Dispatchers.IO).launch {
            client.auth.sessionStatus.collect { status ->
                _authState.value = when (status) {
                    is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> AuthState.Authenticated
                    is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated -> AuthState.NotAuthenticated
                    else -> AuthState.Loading
                }
            }
        }
    }

    override fun handleDeepLink(intent: Intent) {
        SupabaseAuthManager.handleDeepLink(intent)
    }

    override fun getCurrentSession() {
        SupabaseAuthManager.getCurrentSession()
    }

    override suspend fun signIn(email: String, pass: String): Result<Unit> {
        return SupabaseAuthManager.signIn(email, pass)
    }

    override suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        avatar: ByteArray?
    ): Result<Unit> {
        return SupabaseAuthManager.signUp(email, pass, fullName, avatar)
    }

    override suspend fun updateProfile(
        fullName: String,
        email: String,
        avatar: ByteArray?
    ): Result<Unit> {
        return SupabaseAuthManager.updateProfile(fullName, email, avatar)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return SupabaseAuthManager.resetPassword(email)
    }

    override suspend fun signOut(): Result<Unit> {
        return SupabaseAuthManager.signOut()
    }
}

