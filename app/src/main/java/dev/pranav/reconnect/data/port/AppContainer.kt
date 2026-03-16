package dev.pranav.reconnect.data.port

import android.content.Context
import dev.pranav.reconnect.data.local.LocalAiInsightStore
import dev.pranav.reconnect.data.local.LocalAttachmentStore
import dev.pranav.reconnect.data.local.LocalContactRepository
import dev.pranav.reconnect.data.local.LocalMomentRepository
import dev.pranav.reconnect.data.local.db.ReConnectDatabase
import kotlinx.coroutines.runBlocking

object AppContainer {
    private var initialized = false

    lateinit var contactRepository: ContactRepository
        private set
    lateinit var momentRepository: MomentRepository
        private set
    lateinit var attachmentStore: AttachmentStore
        private set
    lateinit var aiInsightStore: AiInsightStore
        private set
    lateinit var storageMetricsRecorder: StorageMetricsRecorder
        private set

    fun init(context: Context) {
        if (initialized) return

        val appContext = context.applicationContext
        val database = ReConnectDatabase.getInstance(appContext)
        val metricsRecorder = InMemoryStorageMetricsRecorder()
        val contactRepo = LocalContactRepository(database.contactDao(), metricsRecorder)
        val momentRepo = LocalMomentRepository(database.momentDao(), metricsRecorder)

        contactRepository = contactRepo
        momentRepository = momentRepo
        attachmentStore = LocalAttachmentStore(appContext, metricsRecorder)
        aiInsightStore = LocalAiInsightStore()
        storageMetricsRecorder = metricsRecorder

        runBlocking {
            dev.pranav.reconnect.data.local.DatabaseSeeder.seedIfNeeded(
                contactRepository = contactRepo,
                momentRepository = momentRepo
            )
        }

        initialized = true
    }
}

