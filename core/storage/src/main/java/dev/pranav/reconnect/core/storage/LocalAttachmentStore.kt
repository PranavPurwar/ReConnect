package dev.pranav.reconnect.core.storage

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dev.pranav.reconnect.core.model.MomentImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalAttachmentStore(
    private val context: Context,
    private val metricsRecorder: StorageMetricsRecorder
) : AttachmentStore {

    private val momentsDir: File
        get() = File(context.filesDir, "moments")

    override suspend fun persistMomentAttachments(
        contactId: String,
        momentId: String,
        sourceUris: List<MomentImage>
    ): List<MomentImage> = withContext(Dispatchers.IO) {
        val result = mutableListOf<MomentImage>()

        for (item in sourceUris) {
            try {
                val uri = Uri.parse(item.uri)
                val extension = getFileExtension(context, uri) ?: "bin"
                val destFileName = "mom_$momentId" + "_${item.id}.$extension"
                val destFile = File(momentsDir, destFileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                result.add(item.copy(uri = destFile.absolutePath))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        result
    }

    override suspend fun deleteMomentAttachments(momentId: String) {
        metricsRecorder.trackWrite(name = "attachment.delete") {
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val contentResolver = context.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
        }
        return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(java.io.File(uri.path ?: "")).toString())
    }
}
