package com.pranav.video.compressor

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

internal data class VideoMetrics(
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val frameRate: Int,
    val isLandscape: Boolean,
    val duration: Long,
    val hasAudio: Boolean,
    val hasVideo: Boolean,
    val mimeType: String?,
    val container: OutputContainer?
)

internal class VideoAnalyzer(private val context: Context) {

    fun analyze(uri: Uri): VideoMetrics {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
                ?.takeIf { it > 0 } ?: 1280
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
                ?.takeIf { it > 0 } ?: 720
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()
                ?.takeIf { it > 0 } ?: 2_000_000
            val frameRate =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                    ?.toIntOrNull()
                    ?.takeIf { it > 0 } ?: 30
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            val mimeType = context.contentResolver.getType(uri)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            val container = inferContainer(uri, mimeType)

            val numTracks = try {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
                    ?.toIntOrNull() ?: 0
            } catch (e: Exception) {
                0
            }

            val hasVideo = numTracks > 0
            val hasAudio = numTracks > 1

            val isLandscape = rotation == 0 || rotation == 180
            VideoMetrics(
                width = width,
                height = height,
                bitrate = bitrate,
                frameRate = frameRate,
                isLandscape = isLandscape,
                duration = duration,
                hasAudio = hasAudio,
                hasVideo = hasVideo,
                mimeType = mimeType,
                container = container
            )
        } catch (e: Exception) {
            VideoMetrics(1280, 720, 3_000_000, 30, true, 0L, true, true, null, null)
        } finally {
            retriever.release()
        }
    }

    private fun inferContainer(uri: Uri, mimeType: String?): OutputContainer? {
        val mt = mimeType?.lowercase()
        when (mt) {
            "video/mp4" -> return OutputContainer.MP4
            "video/quicktime" -> return OutputContainer.MOV
            "video/webm" -> return OutputContainer.WEBM
        }

        val ext = uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }

        return when (ext) {
            "mp4", "m4v" -> OutputContainer.MP4
            "mov" -> OutputContainer.MOV
            "webm" -> OutputContainer.WEBM
            else -> null
        }
    }
}
