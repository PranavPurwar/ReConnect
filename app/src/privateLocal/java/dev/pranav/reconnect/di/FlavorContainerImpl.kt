package dev.pranav.reconnect.di

import android.content.Context
import dev.pranav.reconnect.core.storage.*
import dev.pranav.reconnect.data.local.*
import dev.pranav.reconnect.data.local.db.ReConnectDatabase
import kotlinx.coroutines.runBlocking

class LocalFlavorContainer: FlavorContainer {
    override lateinit var contactStore: ContactStore
    override lateinit var momentStore: MomentStore
    override lateinit var attachmentStore: AttachmentStore
    override lateinit var aiInsightStore: AiInsightStore
    override lateinit var photoResolver: PhotoResolver
    override lateinit var authStore: AuthStore

    override fun init(context: Context, metricsRecorder: StorageMetricsRecorder) {
        val database = ReConnectDatabase.getInstance(context)
        contactStore = RoomContactStore(database.contactDao())
        momentStore = RoomMomentStore(database.momentDao())
        attachmentStore = FileAttachmentStore(context, metricsRecorder)
        aiInsightStore = MockAiInsightStore()
        photoResolver = dev.pranav.reconnect.data.local.LocalPhotoResolver(context)
        authStore = LocalAuthStore()

    }
}
