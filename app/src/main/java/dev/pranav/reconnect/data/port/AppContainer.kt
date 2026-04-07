package dev.pranav.reconnect.data.port

import android.content.Context
import dev.pranav.reconnect.core.storage.*

object AppContainer {
    private var initialized = false
    private lateinit var flavorContainer: FlavorContainer

    val contactStore: ContactStore
        get() = flavorContainer.contactStore
    val momentStore: MomentStore
        get() = flavorContainer.momentStore
    val attachmentStore: AttachmentStore
        get() = flavorContainer.attachmentStore
    val aiInsightStore: AiInsightStore
        get() = flavorContainer.aiInsightStore
    val photoResolver: PhotoResolver
        get() = flavorContainer.photoResolver
    val authStore: AuthStore
        get() = flavorContainer.authStore

    lateinit var storageMetricsRecorder: StorageMetricsRecorder
        private set

    fun init(context: Context) {
        if (initialized) return

        val appContext = context.applicationContext
        val metricsRecorder = InMemoryStorageMetricsRecorder()
        storageMetricsRecorder = metricsRecorder

        flavorContainer = FlavorConfig.createContainer()
        flavorContainer.init(appContext, metricsRecorder)

        initialized = true
    }
}
