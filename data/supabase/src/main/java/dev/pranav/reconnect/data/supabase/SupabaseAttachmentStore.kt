package dev.pranav.reconnect.data.supabase

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.joelromanpr.tinycompressor.CompressFormat
import com.joelromanpr.tinycompressor.ImageCompressor
import com.joelromanpr.tinycompressor.Options
import com.joelromanpr.tinycompressor.Source
import dev.pranav.reconnect.core.model.MomentImage
import dev.pranav.reconnect.core.storage.AttachmentStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseAttachmentStore(
    private val client: SupabaseClient,
    private val context: Context
) : AttachmentStore {

    override suspend fun persistMomentAttachments(
        contactId: String,
        momentId: String,
        sourceUris: List<MomentImage>
    ): List<MomentImage> = withContext(Dispatchers.IO) {
        val user = client.auth.currentUserOrNull() ?: return@withContext emptyList()
        val result = mutableListOf<MomentImage>()

        for (item in sourceUris) {
            try {
                val uri = item.uri.toUri()
                val extension = getFileExtension(context, uri) ?: "bin"
                val path = "${user.id}/$momentId/${item.id}.$extension"

                val bytes = compressFromUriToBytes(context, uri, extension)
                client.storage.from("moments").upload(path, bytes) {
                    upsert = true
                }
                result.add(item.copy(uri = path))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        result
    }

    override suspend fun deleteMomentAttachments(momentId: String) {
        withContext(Dispatchers.IO) {
            try {
                val user = client.auth.currentUserOrNull() ?: return@withContext
                val bucket = client.storage.from("moments")
                val folderPath = "${user.id}/$momentId"
                val files = bucket.list(folderPath)
                if (files.isNotEmpty()) {
                    val filePaths = files.map { "$folderPath/${it.name}" }
                    bucket.delete(filePaths)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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


    suspend fun compressFromUriToBytes(context: Context, uri: Uri, extension: String): ByteArray {
        if (extension !in listOf("png", "jpg", "jpeg", "webp")) {
            return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: ByteArray(0)
        }

        return ImageCompressor.compressToByteArray(
            context = context,
            source = Source.Uri(uri),
            options = Options(
                maxWidth = 2560,
                maxHeight = 2560,
                format = CompressFormat.WEBP,
                quality = 90
            )
        )
    }
}
