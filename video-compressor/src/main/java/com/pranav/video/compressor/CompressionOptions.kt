package com.pranav.video.compressor

import androidx.media3.common.MimeTypes

enum class VideoCodec(val mimeType: String, val defaultBpp: Double) {
    AVC(MimeTypes.VIDEO_H264, 0.075),
    HEVC(MimeTypes.VIDEO_H265, 0.045),
    VP9(MimeTypes.VIDEO_VP9, 0.05)
}

enum class OutputContainer(val extension: String) {
    MP4("mp4"),
    MOV("mov"),
    WEBM("webm")
}

enum class OutputFormat(
    val container: OutputContainer,
    val codec: VideoCodec,
    val extension: String = container.extension
) {
    MP4_AVC(OutputContainer.MP4, VideoCodec.AVC),
    MP4_HEVC(OutputContainer.MP4, VideoCodec.HEVC),
    MOV_AVC(OutputContainer.MOV, VideoCodec.AVC),
    MOV_HEVC(OutputContainer.MOV, VideoCodec.HEVC),
    WEBM_VP9(OutputContainer.WEBM, VideoCodec.VP9)
}

/**
 * Configuration options for the video compression engine.
 *
 * `outputFormat` is optional:
 * - If provided: use it.
 * - If null: resolve from output file extension (if any), otherwise default to the input container.
 */
data class CompressionOptions(
    val outputFormat: OutputFormat? = null,
    val targetHeight: Int = 720,
    val bitsPerPixel: Double? = null,
    val frameRateMax: Int = 30,
    val iFrameInterval: Float = 2.0f,
    val includeAudio: Boolean = true,
    /**
     * Explicit video bitrate override (bps). Will still be clamped to the input bitrate.
     */
    val videoBitrate: Int? = null,
    /**
     * Preset-level target floor (bps) used for input-aware smoothing.
     * Effective floor is min(inputBitrate, presetTargetVideoBitrate).
     */
    val presetTargetVideoBitrate: Int? = null,
    val audioBitrate: Int = 128_000,
    val validateFormat: Boolean = true
) {
    init {
        require(targetHeight > 0) { "targetHeight must be positive" }
        require(frameRateMax > 0) { "frameRateMax must be positive" }
        require(iFrameInterval > 0f) { "iFrameInterval must be positive" }
        require(videoBitrate == null || videoBitrate > 0) { "videoBitrate must be null or positive" }
        require(audioBitrate > 0) { "audioBitrate must be positive" }
    }

    fun getEffectiveBpp(resolvedFormat: OutputFormat): Double =
        bitsPerPixel ?: resolvedFormat.codec.defaultBpp
}
