package dev.pranav.reconnect.data.remote

import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.port.MomentStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseMomentStore(private val client: SupabaseClient): MomentStore {

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        @SerialName("image_uris") val imageUris: List<String>,
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
        @SerialName("image_uris") val imageUris: List<String>,
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
            imageUris = imageUris,
            createdAtEpochMs = createdAtEpochMs
        )
    }

    @OptIn(SupabaseExperimental::class)
    override val moments: Flow<List<PastMoment>> = client.from("moments")
        .selectAsFlow(PastMoment::id)
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
            imageUris = moment.imageUris,
            createdAtEpochMs = moment.createdAtEpochMs
        )
        client.postgrest["moments"].insert(momentInsert)

        if (moment.contactIds.isNotEmpty()) {
            val contactInserts = moment.contactIds.map { contactId ->
                MomentContactInsert(momentId = moment.id, contactId = contactId)
            }
            client.postgrest["moment_contacts"].insert(contactInserts)
        }
    }

    override suspend fun getMomentsFor(contactId: String): List<PastMoment> {
        return client.postgrest["moments"]
            .select(columns = Columns.raw("*, moment_contacts!inner(contact_id)")) {
                filter {
                    eq("moment_contacts.contact_id", contactId)
                }
            }
            .decodeList<MomentSupabase>()
            .map { it.toDomain() }
    }

    override suspend fun deleteMomentsForContact(contactId: String) {
        client.postgrest["moment_contacts"].delete {
            filter {
                eq("contact_id", contactId)
            }
        }
    }
}

