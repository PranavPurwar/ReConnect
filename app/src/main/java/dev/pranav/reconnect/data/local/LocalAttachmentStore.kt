package dev.pranav.reconnect.data.local

import android.content.Context
import dev.pranav.reconnect.data.port.AttachmentStore
import dev.pranav.reconnect.data.port.StorageMetricsRecorder
import dev.pranav.reconnect.data.port.trackWrite

class LocalAttachmentStore(
    private val context: Context,
    private val metricsRecorder: StorageMetricsRecorder
) : AttachmentStore {

    override suspend fun persistMomentAttachments(
        contactId: String,
        momentId: String,
        sourceUris: List<String>
    ): List<String> {
        return metricsRecorder.trackWrite(name = "attachment.persist") {
            // Local flavor currently stores existing URIs and keeps file operations backend-specific.
            sourceUris
        }
    }

    override suspend fun deleteMomentAttachments(momentId: String) {
        metricsRecorder.trackWrite(name = "attachment.delete") {
            Unit
        }
    }
}

