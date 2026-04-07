package dev.pranav.reconnect.core.storage

import android.content.Intent

interface AuthManager {
    suspend fun getCurrentSession(): Any?
    fun handleDeepLink(intent: Intent)
    suspend fun getUserEmail(): String?
    suspend fun getUserAvatarUrl(): String?
    suspend fun signOut()
    suspend fun updateProfile(name: String, unhashedPass: String?, imageBytes: ByteArray?)
    suspend fun signIn(email: String, unhashedPass: String)
    suspend fun signUp(email: String, unhashedPass: String)
    suspend fun sendPasswordReset(email: String)
}
