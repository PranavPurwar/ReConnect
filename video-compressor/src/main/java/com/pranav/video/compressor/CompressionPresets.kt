package com.pranav.video.compressor

/**
 * High-level presets for common compression scenarios.
 *
 * Presets define *quality knobs only* (resolution/fps/BPP/etc). They do NOT enforce output format.
 * Output format is resolved from explicit options, output file extension, or input container.
 */
object CompressionPresets {

    fun ultraCompressed(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 360,
        bitsPerPixel = 0.055,
        frameRateMax = 24,
        iFrameInterval = 3.0f,
        includeAudio = true,
        audioBitrate = 64_000,
        presetTargetVideoBitrate = 1_200_000
    )

    fun balanced(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 720,
        bitsPerPixel = 0.07,
        frameRateMax = 30,
        iFrameInterval = 2.0f,
        includeAudio = true,
        audioBitrate = 128_000,
        presetTargetVideoBitrate = 2_500_000
    )

    fun highQuality(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 1080,
        bitsPerPixel = 0.09,
        frameRateMax = 30,
        iFrameInterval = 2.0f,
        includeAudio = true,
        audioBitrate = 192_000,
        presetTargetVideoBitrate = 5_000_000
    )

    fun premium(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 2160,
        bitsPerPixel = 0.11,
        frameRateMax = 60,
        iFrameInterval = 1.0f,
        includeAudio = true,
        audioBitrate = 256_000,
        presetTargetVideoBitrate = 12_000_000
    )

    fun webOptimized(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 480,
        bitsPerPixel = 0.06,
        frameRateMax = 30,
        iFrameInterval = 2.0f,
        includeAudio = true,
        audioBitrate = 96_000,
        presetTargetVideoBitrate = 1_800_000
    )

    fun legacyDevice(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 720,
        bitsPerPixel = 0.085,
        frameRateMax = 30,
        iFrameInterval = 2.0f,
        includeAudio = true,
        audioBitrate = 128_000,
        presetTargetVideoBitrate = 2_000_000
    )

    fun modernWeb(): CompressionOptions = CompressionOptions(
        outputFormat = null,
        targetHeight = 720,
        bitsPerPixel = 0.075,
        frameRateMax = 30,
        iFrameInterval = 2.0f,
        includeAudio = true,
        audioBitrate = 128_000,
        presetTargetVideoBitrate = 2_500_000
    )
}

class CompressionBuilder {
    private var outputFormat: OutputFormat? = null
    private var targetHeight: Int = 720
    private var bitsPerPixel: Double? = null
    private var frameRateMax: Int = 30
    private var iFrameInterval: Float = 2.0f
    private var includeAudio: Boolean = true
    private var videoBitrate: Int? = null
    private var audioBitrate: Int = 128_000
    private var validateFormat: Boolean = true

    fun format(format: OutputFormat?) = apply { this.outputFormat = format }

    fun height(height: Int) = apply { this.targetHeight = height }

    fun quality(bitsPerPixel: Double) = apply { this.bitsPerPixel = bitsPerPixel }

    fun fps(frameRate: Int) = apply { this.frameRateMax = frameRate }

    fun keyframeInterval(seconds: Float) = apply { this.iFrameInterval = seconds }

    fun videoBitrate(bitrate: Int?) = apply { this.videoBitrate = bitrate }

    fun withAudio(audioBitrate: Int = 128_000) = apply {
        this.includeAudio = true
        this.audioBitrate = audioBitrate
    }

    fun withoutAudio() = apply { this.includeAudio = false }

    fun validateFormat(validate: Boolean) = apply { this.validateFormat = validate }

    fun build(): CompressionOptions = CompressionOptions(
        outputFormat = outputFormat,
        targetHeight = targetHeight,
        bitsPerPixel = bitsPerPixel,
        frameRateMax = frameRateMax,
        iFrameInterval = iFrameInterval,
        includeAudio = includeAudio,
        videoBitrate = videoBitrate,
        audioBitrate = audioBitrate,
        validateFormat = validateFormat
    )
}
