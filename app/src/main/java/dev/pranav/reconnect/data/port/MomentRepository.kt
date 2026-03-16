package dev.pranav.reconnect.data.port

import dev.pranav.reconnect.data.model.PastMoment
import kotlinx.coroutines.flow.Flow

interface MomentRepository {
    val moments: Flow<List<PastMoment>>

    suspend fun addMoment(moment: PastMoment)
    suspend fun getMomentsFor(contactId: String): List<PastMoment>
    suspend fun deleteMomentsForContact(contactId: String)
}

