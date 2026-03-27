package dev.pranav.reconnect.data.remote

import android.content.Intent
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.coil.SketchIntegration
import io.github.jan.supabase.coil.supportSupabaseStorage
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseAuthManager {
    private const val SUPABASE_URL = "https://kxjpijwezjnrqyfktbhr.supabase.co"
    private const val SUPABASE_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt4anBpandlempucnF5Zmt0YmhyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM1NTg0NjIsImV4cCI6MjA4OTEzNDQ2Mn0.8CNi2Loz5K7QAibmbDjtdlN63RbPXwEsew25OoWT3eA"

    @OptIn(SupabaseExperimental::class)
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth) {
                scheme = "reconnect"
                host = "confirm"
                autoLoadFromStorage = true
                autoSaveToStorage = true
            }
            install(Postgrest)
            install(Realtime)
            install(SketchIntegration)
            install(Storage)
            install(ComposeAuth)
        }
    }

    init {
        runBlocking { client.auth.loadFromStorage() }
        SingletonSketch.setSafe {
            Sketch.Builder(it).apply {
                components {
                    supportSupabaseStorage(client)
                }
            }.build()
        }
    }

    fun handleDeepLink(intent: Intent) {
        client.handleDeeplinks(intent)
    }

    fun getCurrentSession() = client.auth.currentSessionOrNull()

    suspend fun signIn(email: String, password: String): Result<Unit> {
        val configError = validateConfig()
        if (configError != null) return Result.failure(configError)

        return runCatching {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        avatar: ByteArray?
    ): Result<Unit> {
        val configError = validateConfig()
        if (configError != null) return Result.failure(configError)

        return runCatching {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }?.let {
                if (avatar != null)
                    client.storage.from("avatars").upload("${it.id}/avatar.png", avatar)
            }
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        val configError = validateConfig()
        if (configError != null) return Result.failure(configError)

        return runCatching {
            client.auth.resetPasswordForEmail(email)
        }
    }

    private fun validateConfig(): IllegalStateException? {
        if (SUPABASE_URL.isBlank() || SUPABASE_KEY.isBlank()) {
            return IllegalStateException(
                "Supabase config is missing. Set URL and KEY in SupabaseAuthManager."
            )
        }
        return null
    }
}

val SupabaseClient.id: String
    get() = auth.currentUserOrNull()!!.id

val SupabaseClient.avatar: String
    get() = "$id/avatar.png"
