package com.pranav.video.compressor

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream

/**
 * Extension functions for video compression.
 * Provides fluent, context-aware methods on Uri and File for easy video manipulation.
 */

/**
 * Compress this video to the specified format.
 *
 * Example: `videoUri.compress(context, OutputFormat.WEBM_VP9)`
 * Returns: InputStream of compressed video
 */
suspend fun Uri.compress(
    context: Context,
    outputFormat: OutputFormat? = OutputFormat.WEBM_VP9,
    preset: CompressionOptions = CompressionPresets.premium()
): InputStream =
    CompressService(context).compress(this, outputFormat, preset)

/**
 * Compress this video with detailed flow of progress/completion states.
 *
 * Example: `videoUri.compressWithProgress(context, outputFile, options).collect { state -> ... }`
 */
fun Uri.compressWithProgress(
    context: Context,
    outputFile: File,
    options: CompressionOptions = CompressionOptions()
): Flow<VideoCompressor.State> =
    VideoCompressor(context).compress(this, outputFile, options)

/**
 * Quickly verify if file extension matches the output format.
 *
 * Example: `outputFile.isValidForFormat(OutputFormat.WEBM_VP9)`
 */
fun File.isValidForFormat(format: OutputFormat): Boolean =
    extension.lowercase() == format.extension

/**
 * Get the suggested output format based on file extension.
 *
 * Example: `outputFile.suggestedFormat()  // returns OutputFormat.WEBM_VP9 for "*.webm"`
 */
fun File.suggestedFormat(): OutputFormat {
    val extension = this.extension.lowercase()
    return when (extension) {
        "webm" -> OutputFormat.WEBM_VP9
        "mp4" -> OutputFormat.MP4_HEVC
        "mov" -> OutputFormat.MOV_HEVC
        else -> OutputFormat.WEBM_VP9
    }
}

/**
 * Ensure file path has the correct extension for the format.
 * Returns a new File with corrected extension if needed.
 *
 * Example: `file.ensureFormatExtension(OutputFormat.WEBM_VP9)  // returns File with .webm ext`
 */
fun File.ensureFormatExtension(format: OutputFormat): File {
    val currentExtension = this.extension.lowercase()
    return if (currentExtension == format.extension) {
        this
    } else {
        val basePath = this.absolutePath.substringBeforeLast(".")
        File("$basePath.${format.extension}")
    }
}
