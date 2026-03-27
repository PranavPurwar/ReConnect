package dev.pranav.reconnect.data.port

import android.content.Context
import dev.pranav.reconnect.BuildConfig
import dev.pranav.reconnect.data.local.LocalAiInsightStore
import dev.pranav.reconnect.data.local.LocalAttachmentStore
import dev.pranav.reconnect.data.local.RoomContactStore
import dev.pranav.reconnect.data.local.RoomMomentStore
import dev.pranav.reconnect.data.local.db.ReConnectDatabase
import dev.pranav.reconnect.data.remote.SupabaseAuthManager
import dev.pranav.reconnect.data.remote.SupabaseContactStore
import dev.pranav.reconnect.data.remote.SupabaseMomentStore
import kotlinx.coroutines.runBlocking

object AppContainer {
    private var initialized = false

    lateinit var contactStore: ContactStore
        private set
    lateinit var momentStore: MomentStore
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
        val metricsRecorder = InMemoryStorageMetricsRecorder()
        storageMetricsRecorder = metricsRecorder

        if (BuildConfig.FLAVOR == "playstoreSupabase") {
            contactStore = SupabaseContactStore(SupabaseAuthManager.client)
            momentStore = SupabaseMomentStore(SupabaseAuthManager.client)
        } else {
            val database = ReConnectDatabase.getInstance(appContext)
            contactStore = RoomContactStore(database.contactDao())
            momentStore = RoomMomentStore(database.momentDao())

            runBlocking {
                dev.pranav.reconnect.data.local.DatabaseSeeder.seedIfNeeded(
                    contactStore = contactStore,
                    momentStore = momentStore
                )
            }
        }

        attachmentStore = LocalAttachmentStore(appContext, metricsRecorder)
        aiInsightStore = LocalAiInsightStore()

        initialized = true
    }
}
