package dev.pranav.reconnect.data.supabase

import androidx.core.net.toUri
import dev.pranav.reconnect.core.storage.PhotoResolver
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.coil.asSketchUri
import io.github.jan.supabase.storage.authenticatedStorageItem
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.hours

@OptIn(SupabaseExperimental::class)
class SupabasePhotoResolver(private val client: SupabaseClient): PhotoResolver {
    override fun resolveContactPhoto(contactId: String): String {
        return authenticatedStorageItem(
            "contacts",
            "${client.id}/$contactId/photo.jpg"
        ).asSketchUri()
    }

    override fun resolveMomentPhoto(uri: String): String {
        return if (!uri.startsWith("http")) {
            authenticatedStorageItem("moments", uri).asSketchUri()
        } else {
            uri
        }
    }

    override fun resolveUserAvatar(id: String?): String {
        return authenticatedStorageItem("avatars", "$id/avatar.png").asSketchUri()
    }

    override suspend fun resolveVideo(uri: String): String {
        if (!uri.startsWith("supabase://")) return uri

        return try {
            val parsedUri = uri.toUri()
            val bucketId = parsedUri.host ?: return uri
            val path = parsedUri.path?.removePrefix("/") ?: return uri
            val authenticated = parsedUri.getQueryParameter("authenticated") == "true"

            val bucket = client.storage.from(bucketId)
            if (authenticated) {
                bucket.createSignedUrl(path, 1.hours)
            } else {
                bucket.publicUrl(path)
            }
        } catch (e: Exception) {
            uri
        }
    }
}
