package com.pranav.video.compressor

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * High-level API for converting videos between different formats.
 *
 * Handles common format conversion scenarios without requiring detailed configuration.
 * Uses sensible defaults optimized for each output format.
 *
 * Example:
 * ```
 * val converter = VideoFormatConverter(context)
 * val flow = converter.convert(
 *     inputUri = videoUri,
 *     outputFile = File(cacheDir, "output.webm")
 * )
 * flow.collect { state ->
 *     when (state) {
 *         is VideoCompressor.State.Progress -> updateProgress(state.percentage)
 *         is VideoCompressor.State.Success -> showSuccess(state.file)
 *         is VideoCompressor.State.Error -> showError(state.exception)
 *     }
 * }
 * ```
 */
class VideoFormatConverter(private val context: Context) {

    private val compressor = VideoCompressor(context)

    /**
     * Convert video to target format with intelligent defaults.
     *
     * @param inputUri Source video URI (any supported format)
     * @param outputFile Target file path (extension must match desired format)
     * @param outputFormat Target output format. Defaults to WEBM_VP9 for modern efficient delivery.
     * @param preserveQuality If true, uses higher bitrate settings; if false, prioritizes smaller file size.
     * @return Flow of compression states (Progress, Success, Error)
     *
     * Supported conversions:
     * - Any format → MP4 (H.264 / H.265)
     * - Any format → MOV (H.264 / H.265)
     * - Any format → WebM (VP9, requires hardware support)
     */
    fun convert(
        inputUri: Uri,
        outputFile: File,
        outputFormat: OutputFormat = OutputFormat.WEBM_VP9,
        preserveQuality: Boolean = false
    ): Flow<VideoCompressor.State> {
        val baseOptions = selectPresetForFormat(outputFormat, preserveQuality)
        val options = baseOptions.copy(outputFormat = outputFormat)
        return compressor.compress(inputUri, outputFile, options)
    }

    /**
     * Batch convert multiple videos to a target format.
     *
     * @param conversions List of input-output file pairs
     * @param outputFormat Target format for all conversions. Defaults to WEBM_VP9.
     * @param preserveQuality Quality preference flag
     * @return Flow emitting batch progress and individual conversion states
     */
    fun batchConvert(
        conversions: List<Pair<Uri, File>>,
        outputFormat: OutputFormat = OutputFormat.WEBM_VP9,
        preserveQuality: Boolean = false
    ): Flow<BatchConversionState> = kotlinx.coroutines.flow.flow {
        val options = selectPresetForFormat(outputFormat, preserveQuality)
        var completed = 0

        for ((inputUri, outputFile) in conversions) {
            emit(BatchConversionState.Started(completed, conversions.size))

            try {
                compressor.compress(inputUri, outputFile, options)
                    .collect { state ->
                        when (state) {
                            is VideoCompressor.State.Progress -> {
                                val overallProgress =
                                    ((completed * 100 + state.percentage) / conversions.size)
                                emit(
                                    BatchConversionState.Progress(
                                        completed,
                                        overallProgress,
                                        conversions.size
                                    )
                                )
                            }

                            is VideoCompressor.State.Success -> {
                                completed++
                                emit(
                                    BatchConversionState.ItemCompleted(
                                        state.file,
                                        completed,
                                        conversions.size
                                    )
                                )
                            }

                            is VideoCompressor.State.Error -> {
                                emit(
                                    BatchConversionState.ItemFailed(
                                        inputUri,
                                        state.exception,
                                        completed,
                                        conversions.size
                                    )
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                emit(BatchConversionState.ItemFailed(inputUri, e, completed, conversions.size))
            }
        }

        emit(BatchConversionState.Completed(completed, conversions.size))
    }

    /**
     * Quick convert to WebM (VP9) - the modern, efficient default.
     * Optimized for streaming and web delivery.
     */
    fun toWebM(inputUri: Uri, outputFile: File): Flow<VideoCompressor.State> =
        convert(inputUri, outputFile, OutputFormat.WEBM_VP9, preserveQuality = false)

    /**
     * Quick convert to MP4 (H.265/HEVC) - balanced quality and compatibility.
     */
    fun toMP4(inputUri: Uri, outputFile: File): Flow<VideoCompressor.State> =
        convert(inputUri, outputFile, OutputFormat.MP4_HEVC, preserveQuality = false)

    /**
     * Quick convert to MP4 (H.264) - maximum device compatibility.
     */
    fun toMP4Legacy(inputUri: Uri, outputFile: File): Flow<VideoCompressor.State> =
        convert(inputUri, outputFile, OutputFormat.MP4_AVC, preserveQuality = false)

    /**
     * Quick convert to MOV (H.265/HEVC) - QuickTime compatibility.
     */
    fun toMOV(inputUri: Uri, outputFile: File): Flow<VideoCompressor.State> =
        convert(inputUri, outputFile, OutputFormat.MOV_HEVC, preserveQuality = false)

    /**
     * Infer output format from file extension.
     * Defaults to WEBM if extension is unrecognized.
     */
    fun inferFormatFromFile(outputFile: File): OutputFormat {
        val extension = outputFile.extension.lowercase()
        return when {
            extension == "webm" -> OutputFormat.WEBM_VP9
            extension == "mp4" -> OutputFormat.MP4_HEVC
            extension == "mov" -> OutputFormat.MOV_HEVC
            else -> OutputFormat.WEBM_VP9
        }
    }

    /**
     * Validate that output file has appropriate extension for the chosen format.
     */
    fun validateOutputFile(outputFile: File, format: OutputFormat): Boolean {
        val extension = outputFile.extension.lowercase()
        val expectedExtension = format.extension
        return extension == expectedExtension || extension.isEmpty()
    }

    private fun selectPresetForFormat(
        format: OutputFormat,
        preserveQuality: Boolean
    ): CompressionOptions {
        // Presets must not enforce format; caller-specified format is applied by the caller.
        return when {
            format == OutputFormat.WEBM_VP9 && preserveQuality -> CompressionBuilder()
                .height(1080)
                .fps(30)
                .quality(0.06)
                .withAudio(192_000)
                .build()

            format == OutputFormat.WEBM_VP9 -> CompressionPresets.modernWeb()

            format == OutputFormat.MP4_HEVC && preserveQuality -> CompressionPresets.highQuality()
            format == OutputFormat.MP4_HEVC -> CompressionPresets.balanced()

            format == OutputFormat.MP4_AVC && preserveQuality -> CompressionBuilder()
                .height(1080)
                .fps(30)
                .quality(0.08)
                .withAudio(192_000)
                .build()

            format == OutputFormat.MP4_AVC -> CompressionPresets.legacyDevice()

            format in listOf(OutputFormat.MOV_HEVC, OutputFormat.MOV_AVC) && preserveQuality ->
                CompressionPresets.highQuality()

            else -> CompressionOptions()
        }
    }
}

/**
 * State for batch video conversions.
 */
sealed interface BatchConversionState {
    data class Started(val itemIndex: Int, val totalItems: Int): BatchConversionState
    data class Progress(val itemIndex: Int, val overallProgress: Int, val totalItems: Int):
        BatchConversionState

    data class ItemCompleted(val outputFile: File, val completedCount: Int, val totalItems: Int):
        BatchConversionState

    data class ItemFailed(
        val inputUri: Uri,
        val exception: Throwable,
        val failedCount: Int,
        val totalItems: Int
    ): BatchConversionState

    data class Completed(val successCount: Int, val totalItems: Int): BatchConversionState
}
