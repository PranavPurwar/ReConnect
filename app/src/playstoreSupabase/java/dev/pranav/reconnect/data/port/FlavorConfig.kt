package dev.pranav.reconnect.data.port

import android.content.Context
import dev.pranav.reconnect.core.storage.*
import dev.pranav.reconnect.data.local.LocalAiInsightStore
import dev.pranav.reconnect.data.local.LocalAttachmentStore
import dev.pranav.reconnect.data.supabase.*

object FlavorConfig {
    fun createContainer(): FlavorContainer = object: FlavorContainer {
        override lateinit var contactStore: ContactStore
        override lateinit var momentStore: MomentStore
        override lateinit var attachmentStore: AttachmentStore
        override lateinit var aiInsightStore: AiInsightStore
        override lateinit var photoResolver: PhotoResolver
        override lateinit var authStore: AuthStore

        override fun init(context: Context, metricsRecorder: StorageMetricsRecorder) {
            val client = SupabaseAuthManager.client
            contactStore = SupabaseContactStore(client, context)
            momentStore = SupabaseMomentStore(client)
            attachmentStore = LocalAttachmentStore(context, metricsRecorder)
            aiInsightStore = LocalAiInsightStore()
            photoResolver = SupabasePhotoResolver(client)
            authStore = SupabaseAuthStore(client)
        }
    }
}
