package dev.pranav.reconnect.data.local

import android.content.Context
import dev.pranav.reconnect.core.storage.PhotoResolver
import java.io.File

class LocalPhotoResolver(private val context: Context): PhotoResolver {
    override fun resolveContactPhoto(contactId: String): String? {
        val file = File(context.filesDir, "contacts/$contactId/photo.jpg")
        return if (file.exists()) file.absolutePath else null
    }

    override fun resolveUserAvatar(id: String?): String? {
        val file = File(context.filesDir, "avatars/avatar.png")
        return if (file.exists()) file.absolutePath else null
    }

    override fun resolveMomentPhoto(uri: String): String {
        return uri
    }
}
