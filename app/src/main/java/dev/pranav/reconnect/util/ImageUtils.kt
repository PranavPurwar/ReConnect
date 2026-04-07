package dev.pranav.reconnect.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.asDrawable
import com.github.panpf.sketch.request.ImageRequest
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.theme.DefaultSeedColor

fun decodePhotoBitmap(context: Context, photoUri: String?): android.graphics.Bitmap? {
    if (photoUri.isNullOrBlank()) return null
    return runCatching { photoUri.toUri().toBitmap(context) }.getOrNull()
}

fun provisionalSeedColorFromPhotoUri(photoUri: String?): Color {
    if (photoUri.isNullOrBlank()) return DefaultSeedColor
    val hue = ((photoUri.hashCode().toLong() and 0x7FFFFFFF) % 360L).toFloat()
    return Color.hsv(hue = hue, saturation = 0.42f, value = 0.82f)
}

suspend fun loadRemoteBitmap(
    context: Context,
    contactId: String
): android.graphics.Bitmap? {
    val resolvedUri = AppContainer.photoResolver.resolveContactPhoto(contactId) ?: return null
    val request = ImageRequest.Builder(context, uri = resolvedUri)
        .build()
    val result = SingletonSketch.get(context).execute(request)
    return (result.image?.asDrawable() as? android.graphics.drawable.BitmapDrawable)?.bitmap
}

