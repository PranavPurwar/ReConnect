package com.pranav.video.compressor

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
class VideoCompressor(private val context: Context) {

    sealed interface State {
        data class Progress(val percentage: Int): State
        data class Success(val file: File): State
        data class Error(val exception: Throwable): State
    }

    private val analyzer = VideoAnalyzer(context)
    private val codecMatcher = HardwareCodecMatcher()
    private val factoryProvider = EncoderFactoryProvider(context)

    fun compress(
        inputUri: Uri,
        outputFile: File,
        options: CompressionOptions = CompressionOptions()
    ): Flow<State> = callbackFlow {
        var activeTransformer: Transformer? = null

        try {
            val metrics = withContext(Dispatchers.IO) {
                analyzer.analyze(inputUri)
            }

            if (!metrics.hasVideo) {
                trySend(State.Error(IllegalArgumentException("Input has no video track")))
                channel.close()
                return@callbackFlow
            }

            if (metrics.duration <= 0) {
                trySend(State.Error(IllegalArgumentException("Input video has zero or negative duration")))
                channel.close()
                return@callbackFlow
            }

            val resolvedFormat = resolveOutputFormat(metrics, outputFile, options)

            if (options.validateFormat) {
                val extension = outputFile.extension.lowercase()
                if (extension.isNotEmpty() && extension != resolvedFormat.extension) {
                    trySend(
                        State.Error(
                            IllegalArgumentException(
                                "Output file extension '.$extension' does not match resolved format '.${resolvedFormat.extension}'"
                            )
                        )
                    )
                    channel.close()
                    return@callbackFlow
                }

                val formatCapability = codecMatcher.determineSafeFormat(resolvedFormat)
                if (formatCapability is HardwareCodecMatcher.FormatCapability.Unsupported) {
                    trySend(State.Error(IllegalStateException("Unsupported format: ${formatCapability.reason}")))
                    channel.close()
                    return@callbackFlow
                }
            }

            // Calculate target dimensions
            val finalHeight = options.targetHeight.coerceAtMost(metrics.height)
            val finalWidth = (finalHeight * (metrics.width.toFloat() / metrics.height)).toInt()
                .coerceAtLeast(16)

            // Calculate bitrate
            val targetFps = metrics.frameRate.coerceAtMost(options.frameRateMax)
            val effectiveBpp = options.getEffectiveBpp(resolvedFormat)
            val calculatedBitrate = (finalWidth * finalHeight * targetFps * effectiveBpp).toInt()

            val inputBitrate = metrics.bitrate
            val presetFloor = options.presetTargetVideoBitrate?.let { floor ->
                minOf(inputBitrate, floor)
            } ?: 0

            val finalBitrate = options.videoBitrate
                ?.let { override -> minOf(inputBitrate, override) }
                ?: minOf(inputBitrate, maxOf(calculatedBitrate, presetFloor))

            val encoderFactory = factoryProvider.create(resolvedFormat, options, finalBitrate)

            val removeAudio = !options.includeAudio || !metrics.hasAudio
            val audioMimeType = resolveAudioMimeType(resolvedFormat, removeAudio)

            val videoEffects = listOf(Presentation.createForHeight(finalHeight))
            val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(inputUri))
                .setEffects(Effects(emptyList(), videoEffects))
                .setRemoveAudio(removeAudio)
                .build()

            withContext(Dispatchers.Main) {
                val transformerBuilder = Transformer.Builder(context)
                    .setVideoMimeType(resolvedFormat.codec.mimeType)

                if (audioMimeType != null) {
                    transformerBuilder.setAudioMimeType(audioMimeType)
                }

                val transformer = transformerBuilder
                    .setEncoderFactory(encoderFactory)
                    .addListener(object: Transformer.Listener {
                        override fun onCompleted(
                            composition: Composition,
                            exportResult: ExportResult
                        ) {
                            trySend(State.Success(outputFile))
                            channel.close()
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            ex: ExportException
                        ) {
                            trySend(State.Error(ex))
                            channel.close()
                        }
                    })
                    .build()

                activeTransformer = transformer
                transformer.start(editedMediaItem, outputFile.absolutePath)

                val progressHolder = ProgressHolder()
                // Polling must execute on the Main thread since getProgress verifies the application thread
                launch(Dispatchers.Main) {
                    while (isActive) {
                        if (transformer.getProgress(progressHolder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                            trySend(State.Progress(progressHolder.progress))
                        }
                        delay(250.milliseconds)
                    }
                }
            }
        } catch (e: Exception) {
            trySend(State.Error(e))
            channel.close()
        }

        awaitClose {
            activeTransformer?.let { transformer ->
                launch(Dispatchers.Main) {
                    transformer.cancel()
                }
            }
        }
    }

    suspend fun compressToStream(
        inputUri: Uri,
        options: CompressionOptions = CompressionOptions()
    ): InputStream = withContext(Dispatchers.IO) {
        val metrics = analyzer.analyze(inputUri)
        val resolvedFormat = resolveOutputFormat(metrics, outputFile = null, options = options)
        val resolvedOptions = options.copy(outputFormat = resolvedFormat)

        val tempFile = File.createTempFile(
            "compressed_",
            ".${resolvedFormat.extension}",
            context.cacheDir
        )
        tempFile.deleteOnExit()

        var result: State.Success? = null
        var error: Throwable? = null

        compress(inputUri, tempFile, resolvedOptions).collect { state ->
            when (state) {
                is State.Error -> error = state.exception
                is State.Success -> result = state
                is State.Progress -> Unit
            }
        }

        error?.let { throw it }
        result?.let { return@withContext FileInputStream(tempFile) }

        throw IllegalStateException("Compression did not complete")
    }

    private fun resolveOutputFormat(
        metrics: VideoMetrics,
        outputFile: File?,
        options: CompressionOptions
    ): OutputFormat {
        options.outputFormat?.let { return it }

        val outContainer = outputFile?.extension
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }
            ?.let { ext ->
                when (ext) {
                    "mp4", "m4v" -> OutputContainer.MP4
                    "mov" -> OutputContainer.MOV
                    "webm" -> OutputContainer.WEBM
                    else -> null
                }
            }

        val container = outContainer ?: metrics.container ?: OutputContainer.WEBM
        return selectBestFormatForContainer(container)
    }

    private fun selectBestFormatForContainer(container: OutputContainer): OutputFormat {
        return when (container) {
            OutputContainer.MP4 -> {
                if (codecMatcher.isVideoCodecSupported(VideoCodec.HEVC)) OutputFormat.MP4_HEVC else OutputFormat.MP4_AVC
            }

            OutputContainer.MOV -> {
                if (codecMatcher.isVideoCodecSupported(VideoCodec.HEVC)) OutputFormat.MOV_HEVC else OutputFormat.MOV_AVC
            }

            OutputContainer.WEBM -> OutputFormat.WEBM_VP9
        }
    }

    private fun resolveAudioMimeType(format: OutputFormat, removeAudio: Boolean): String? {
        if (removeAudio) return null

        return when (format.container) {
            OutputContainer.MP4, OutputContainer.MOV -> MimeTypes.AUDIO_AAC
            OutputContainer.WEBM -> {
                val opus = MimeTypes.AUDIO_OPUS
                if (!codecMatcher.isAudioMimeTypeSupported(opus)) {
                    throw IllegalStateException(
                        "Audio is enabled but this device cannot encode $opus for WebM output. " +
                                "Specify an MP4/MOV output format to keep audio."
                    )
                }
                opus
            }
        }
    }
}
