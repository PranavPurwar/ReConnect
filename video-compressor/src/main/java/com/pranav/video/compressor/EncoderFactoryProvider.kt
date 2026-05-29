package com.pranav.video.compressor

import android.content.Context
import android.media.MediaCodecInfo
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.VideoEncoderSettings

internal class EncoderFactoryProvider(private val context: Context) {

    @OptIn(UnstableApi::class)
    fun create(
        format: OutputFormat,
        options: CompressionOptions,
        computedBitrate: Int
    ): DefaultEncoderFactory {
        val codecProfile = when (format.codec) {
            VideoCodec.HEVC -> MediaCodecInfo.CodecProfileLevel.HEVCProfileMain
            VideoCodec.AVC -> MediaCodecInfo.CodecProfileLevel.AVCProfileMain
            VideoCodec.VP9 -> MediaCodecInfo.CodecProfileLevel.VP9Profile0
        }

        val encoderSettings = VideoEncoderSettings.Builder()
            .setBitrateMode(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            .setBitrate(computedBitrate)
            .setEncodingProfileLevel(codecProfile, VideoEncoderSettings.NO_VALUE)
            .setiFrameIntervalSeconds(options.iFrameInterval)
            .build()

        return DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(encoderSettings)
            .setEnableFallback(true)
            .build()
    }
}
