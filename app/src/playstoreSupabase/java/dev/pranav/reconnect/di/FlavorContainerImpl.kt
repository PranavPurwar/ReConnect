package dev.pranav.reconnect.di

import android.content.Context
import dev.pranav.reconnect.core.storage.*
import dev.pranav.reconnect.data.supabase.*

class SupabaseFlavorContainer: FlavorContainer {
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
        attachmentStore = FileAttachmentStore(context, metricsRecorder)
        aiInsightStore = MockAiInsightStore()
        photoResolver = SupabasePhotoResolver(client)
        authStore = SupabaseAuthStore(client)
    }
}

