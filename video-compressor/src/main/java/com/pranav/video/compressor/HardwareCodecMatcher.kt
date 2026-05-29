package com.pranav.video.compressor

import android.media.MediaCodecList

internal class HardwareCodecMatcher {

    sealed class FormatCapability {
        data class Supported(val format: OutputFormat): FormatCapability()
        data class Unsupported(val format: OutputFormat, val reason: String): FormatCapability()
    }

    fun isVideoCodecSupported(codec: VideoCodec): Boolean {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        return codecList.codecInfos.any { codecInfo ->
            codecInfo.isEncoder && codecInfo.supportedTypes.any {
                it.equals(
                    codec.mimeType,
                    ignoreCase = true
                )
            }
        }
    }

    fun isAudioMimeTypeSupported(mimeType: String): Boolean {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        return codecList.codecInfos.any { codecInfo ->
            codecInfo.isEncoder && codecInfo.supportedTypes.any {
                it.equals(
                    mimeType,
                    ignoreCase = true
                )
            }
        }
    }

    fun determineSafeFormat(requestedFormat: OutputFormat): FormatCapability {
        return if (isVideoCodecSupported(requestedFormat.codec)) {
            FormatCapability.Supported(requestedFormat)
        } else {
            FormatCapability.Unsupported(
                requestedFormat,
                "Device does not support ${requestedFormat.codec.mimeType} encoding"
            )
        }
    }
}
