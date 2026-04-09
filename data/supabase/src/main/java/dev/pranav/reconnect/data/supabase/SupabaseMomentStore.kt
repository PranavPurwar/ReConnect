package dev.pranav.reconnect.data.supabase

import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.MomentImage
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.MomentStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(SupabaseExperimental::class)
class SupabaseMomentStore(private val client: SupabaseClient): MomentStore {

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1).apply { tryEmit(Unit) }

    init {
        storeScope.launch {
            try {
                client.from("moments").selectAsFlow(MomentSupabase::id).collect {
                    refreshTrigger.tryEmit(Unit)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Serializable
    private data class MomentContactSupabase(
        @SerialName("contact_id") val contactId: String
    )

    @Serializable
    private data class MomentSupabase(
        val id: String,
        val title: String,
        val description: String,
        @SerialName("date_epoch_ms") val dateEpochMs: Long,
        val category: MomentCategory,
        @SerialName("images") val images: List<MomentImage> = emptyList(),
        @SerialName("is_core_memory") val isCoreMemory: Boolean = false,
        @SerialName("was_present") val wasPresent: Boolean = true,
        @SerialName("group_name") val groupName: String? = null,
        @SerialName("location_mood") val locationMood: String? = null,
        @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long,
        @SerialName("moment_contacts") val momentContacts: List<MomentContactSupabase> = emptyList()
    )

    @Serializable
    private data class MomentInsert(
        val id: String,
        val title: String,
        val description: String,
        @SerialName("date_epoch_ms") val dateEpochMs: Long,
        val category: MomentCategory,
        @SerialName("images") val images: List<MomentImage>,
        @SerialName("is_core_memory") val isCoreMemory: Boolean,
        @SerialName("was_present") val wasPresent: Boolean,
        @SerialName("group_name") val groupName: String?,
        @SerialName("location_mood") val locationMood: String?,
        @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long
    )

    @Serializable
    private data class MomentContactInsert(
        @SerialName("moment_id") val momentId: String,
        @SerialName("contact_id") val contactId: String
    )

    private fun MomentSupabase.toDomain(): PastMoment {
        return PastMoment(
            id = id,
            contactIds = momentContacts.map { it.contactId },
            title = title,
            description = description,
            dateEpochMs = dateEpochMs,
            category = category,
            images = images,
            isCoreMemory = isCoreMemory,
            wasPresent = wasPresent,
            groupName = groupName,
            locationMood = locationMood,
            createdAtEpochMs = createdAtEpochMs
        )
    }

    @OptIn(SupabaseExperimental::class)
    override val moments: Flow<List<PastMoment>> = client.from("moments")
        .selectAsFlow(MomentSupabase::id)
        .map { _ ->
            try {
                client.postgrest["moments"]
                    .select(columns = Columns.list("*, moment_contacts(contact_id)"))
                    .decodeList<MomentSupabase>()
                    .map { it.toDomain() }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
        .stateIn(
            scope = storeScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    override suspend fun addMoment(moment: PastMoment) {
        val momentInsert = MomentInsert(
            id = moment.id,
            title = moment.title,
            description = moment.description,
            dateEpochMs = moment.dateEpochMs,
            category = moment.category,
            images = moment.images,
            isCoreMemory = moment.isCoreMemory,
            wasPresent = moment.wasPresent,
            groupName = moment.groupName,
            locationMood = moment.locationMood,
            createdAtEpochMs = moment.createdAtEpochMs
        )
        client.postgrest["moments"].insert(momentInsert)

        if (moment.contactIds.isNotEmpty()) {
            val contactInserts = moment.contactIds.map { contactId ->
                MomentContactInsert(momentId = moment.id, contactId = contactId)
            }
            client.postgrest["moment_contacts"].insert(contactInserts)
        }

        refreshTrigger.tryEmit(Unit)
    }

    override suspend fun updateMoment(moment: PastMoment) {
        val momentUpdate = MomentInsert(
            id = moment.id,
            title = moment.title,
            description = moment.description,
            dateEpochMs = moment.dateEpochMs,
            category = moment.category,
            images = moment.images,
            isCoreMemory = moment.isCoreMemory,
            wasPresent = moment.wasPresent,
            groupName = moment.groupName,
            locationMood = moment.locationMood,
            createdAtEpochMs = moment.createdAtEpochMs
        )
        client.postgrest["moments"].update(momentUpdate) {
            filter { eq("id", moment.id) }
        }

        client.postgrest["moment_contacts"].delete {
            filter { eq("moment_id", moment.id) }
        }

        if (moment.contactIds.isNotEmpty()) {
            val contactInserts = moment.contactIds.map { contactId ->
                MomentContactInsert(momentId = moment.id, contactId = contactId)
            }
            client.postgrest["moment_contacts"].insert(contactInserts)
        }

        refreshTrigger.tryEmit(Unit)
    }

    override suspend fun deleteMoment(momentId: String) {
        client.postgrest["moments"].delete {
            filter { eq("id", momentId) }
        }
        refreshTrigger.tryEmit(Unit)
    }

    override suspend fun getMomentsFor(contactId: String): List<PastMoment> {
        return moments.first().filter { it.contactIds.contains(contactId) }
    }

    override suspend fun deleteMomentsForContact(contactId: String) {
        client.postgrest["moment_contacts"].delete {
            filter {
                eq("contact_id", contactId)
            }
        }
    }
}
