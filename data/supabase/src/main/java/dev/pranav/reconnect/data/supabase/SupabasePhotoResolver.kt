package dev.pranav.reconnect.data.supabase

import dev.pranav.reconnect.core.storage.PhotoResolver
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.coil.asSketchUri
import io.github.jan.supabase.storage.authenticatedStorageItem

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
}
