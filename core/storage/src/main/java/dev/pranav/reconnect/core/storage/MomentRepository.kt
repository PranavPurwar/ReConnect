package dev.pranav.reconnect.core.storage

import dev.pranav.reconnect.core.model.PastMoment
import kotlinx.coroutines.flow.Flow

interface MomentRepository {
    val moments: Flow<List<PastMoment>>

    suspend fun addMoment(moment: PastMoment)
    suspend fun getMomentsFor(contactId: String): List<PastMoment>
    suspend fun deleteMomentsForContact(contactId: String)
}

