package dev.pranav.reconnect.di

import android.content.Context
import dev.pranav.reconnect.core.storage.*

interface FlavorContainer {
    val contactStore: ContactStore
    val momentStore: MomentStore
    val attachmentStore: AttachmentStore
    val aiInsightStore: AiInsightStore
    val photoResolver: PhotoResolver
    val authStore: AuthStore

    fun init(
        context: Context,
        metricsRecorder: StorageMetricsRecorder
    )
}
