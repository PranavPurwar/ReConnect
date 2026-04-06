package dev.pranav.reconnect.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

fun Context.takePersistableReadPermissionIfPossible(uri: Uri) {
    if (uri.scheme != "content") return
    runCatching {
        contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        if (scheme == "http" || scheme == "https") {
            java.net.URL(toString()).openStream().use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } else {
            context.contentResolver.openInputStream(this)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
