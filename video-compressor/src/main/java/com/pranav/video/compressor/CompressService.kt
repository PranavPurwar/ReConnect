package com.pranav.video.compressor

import android.content.Context
import android.net.Uri
import java.io.InputStream

/**
 * High-level compression service.
 *
 * If `outputFormat` is null, output defaults to the input container (mp4→mp4, mov→mov, webm→webm).
 */
class CompressService(private val context: Context) {

    private val compressor = VideoCompressor(context)

    suspend fun compress(
        input: Uri,
        outputFormat: OutputFormat? = null,
        preset: String = "balanced"
    ): InputStream {
        val baseConfig = when (preset) {
            "ultra_compressed" -> CompressionPresets.ultraCompressed()
            "balanced" -> CompressionPresets.balanced()
            "high_quality" -> CompressionPresets.highQuality()
            "premium" -> CompressionPresets.premium()
            "web_optimized" -> CompressionPresets.webOptimized()
            "legacy_device" -> CompressionPresets.legacyDevice()
            "modern_web" -> CompressionPresets.modernWeb()
            else -> CompressionPresets.balanced()
        }

        val config = baseConfig.copy(outputFormat = outputFormat)
        return compressor.compressToStream(input, config)
    }

    suspend fun compress(
        input: Uri,
        outputFormat: OutputFormat? = null,
        preset: CompressionOptions = CompressionPresets.balanced()
    ): InputStream {
        val config = preset.copy(outputFormat = outputFormat)
        return compressor.compressToStream(input, config)
    }

    suspend fun compressBatch(
        inputs: List<Uri>,
        outputFormat: OutputFormat? = null,
        preset: String = "balanced"
    ): List<InputStream> = inputs.map { input ->
        compress(input, outputFormat, preset)
    }
}

fun createCompressor(context: Context): CompressService = CompressService(context)
