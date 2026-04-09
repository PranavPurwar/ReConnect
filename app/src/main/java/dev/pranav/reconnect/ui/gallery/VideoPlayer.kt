package dev.pranav.reconnect.ui.gallery

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.OverzoomEffect
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun VideoPlayer(
    uri: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = false
) {
    val context = LocalContext.current
    var resolvedUri by remember(uri) { mutableStateOf<String?>(null) }

    LaunchedEffect(uri) {
        resolvedUri = AppContainer.photoResolver.resolveVideo(uri)
    }

    val exoPlayer = remember(context, resolvedUri) {
        resolvedUri?.let { currentUri ->
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(currentUri)))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    LaunchedEffect(exoPlayer, playWhenReady) {
        exoPlayer?.playWhenReady = playWhenReady
    }

    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(
            maxZoomFactor = 20f,
            overzoomEffect = OverzoomEffect.NoLimits
        )
    )
    val presentationState = exoPlayer?.let { rememberPresentationState(it) }

    var isPlaying by remember { mutableStateOf(playWhenReady) }
    var showControls by remember { mutableStateOf(false) }

    // Listen to player state changes
    DisposableEffect(exoPlayer) {
        val listener = object: Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                isPlaying = isPlayingState
            }
        }
        exoPlayer?.addListener(listener)
        onDispose {
            exoPlayer?.removeListener(listener)
        }
    }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(2000)
            showControls = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (exoPlayer != null) {
            val videoSize = presentationState?.videoSizeDp
            val isReady = videoSize != null

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        state = zoomableState,
                        onClick = {
                            showControls = !showControls
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                PlayerSurface(
                    player = exoPlayer,
                    surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isReady) 1f else 0f)
                        .then(
                            if (isReady) Modifier.resizeWithContentScale(
                                ContentScale.Fit,
                                sourceSizeDp = videoSize
                            ) else Modifier
                        )
                )
            }

            AnimatedVisibility(
                visible = (isReady && showControls) || !isPlaying,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                                showControls = false
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
