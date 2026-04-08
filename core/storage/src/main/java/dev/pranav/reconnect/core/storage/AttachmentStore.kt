package dev.pranav.reconnect.core.storage

import dev.pranav.reconnect.core.model.MomentImage

interface AttachmentStore {
    suspend fun persistMomentAttachments(
        contactId: String,
        momentId: String,
        sourceUris: List<MomentImage>
    ): List<MomentImage>

    suspend fun deleteMomentAttachments(momentId: String)
}
