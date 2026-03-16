package dev.pranav.reconnect.data.port

interface AttachmentStore {
    suspend fun persistMomentAttachments(
        contactId: String,
        momentId: String,
        sourceUris: List<String>
    ): List<String>

    suspend fun deleteMomentAttachments(momentId: String)
}

