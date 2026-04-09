package dev.pranav.reconnect.core.storage

import dev.pranav.reconnect.core.model.PastMoment
import kotlinx.coroutines.flow.Flow

interface MomentStore {
    val moments: Flow<List<PastMoment>>

    suspend fun addMoment(moment: PastMoment)
    suspend fun updateMoment(moment: PastMoment)
    suspend fun deleteMoment(momentId: String)
    suspend fun getMomentsFor(contactId: String): List<PastMoment>
    suspend fun deleteMomentsForContact(contactId: String)
}
