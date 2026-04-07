package dev.pranav.reconnect.core.storage

interface PhotoResolver {
    fun resolveContactPhoto(contactId: String): String?
    fun resolveMomentPhoto(uri: String): String
    fun resolveUserAvatar(id: String?): String?
}
