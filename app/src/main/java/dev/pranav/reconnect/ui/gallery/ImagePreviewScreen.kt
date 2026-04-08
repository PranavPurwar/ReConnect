package dev.pranav.reconnect.ui.gallery

import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.rememberSketchZoomState
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.GoldPrimary

@Composable
fun ImagePreviewScreen(
    imageUris: List<String>,
    initialIndex: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val safeIndex = initialIndex.coerceIn(0, (imageUris.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = safeIndex) { imageUris.size }

    fun isVideoUri(uriString: String): Boolean {
        val uri = android.net.Uri.parse(uriString)
        val mimeType = if (uri.scheme == android.content.ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uriString)
            if (extension.isNotEmpty()) {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            } else null
        }
        return mimeType?.startsWith("video/") == true ||
            uriString.endsWith(".mp4", true) ||
            uriString.endsWith(".mov", true) ||
            uriString.endsWith(".webm", true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // We only want the pager to scroll when we aren't zoomed in
            userScrollEnabled = true
        ) { page ->
            val uri = imageUris[page]
            val isVideo = remember(uri) { isVideoUri(uri) }

            if (isVideo) {
                // Determine if this is the currently active page to auto-play or not
                val isCurrentPage = pagerState.currentPage == page
                VideoPlayer(
                    uri = uri,
                    modifier = Modifier.fillMaxSize(),
                    playWhenReady = isCurrentPage && !pagerState.isScrollInProgress
                )
            } else {
                val state = rememberAsyncImageState(ComposableImageOptions {
                    crossfade()
                })

                // Each page needs its own zoom state
                val zoomState = rememberSketchZoomState()

                // Reset zoom when the user scrolls away from this page
                LaunchedEffect(pagerState.isScrollInProgress) {
                    if (pagerState.isScrollInProgress) {
                        zoomState.zoomable.reset()
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SketchZoomAsyncImage(
                        uri = uri,
                        contentDescription = "Image preview",
                        zoomState = zoomState,
                        state = state,
                        modifier = if (state.painterState is PainterState.Success) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.wrapContentSize()
                        },
                        contentScale = ContentScale.Fit,
                    )
                    if (state.painterState !is PainterState.Success) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = null,
                            tint = GoldPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        }

        // Top bar overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }

            if (imageUris.size > 1) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        "${pagerState.currentPage + 1} / ${imageUris.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
