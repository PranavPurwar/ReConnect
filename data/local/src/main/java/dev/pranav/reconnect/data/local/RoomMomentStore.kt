package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.MomentStore
import dev.pranav.reconnect.data.local.db.MomentDao
import dev.pranav.reconnect.data.local.db.toEntity
import dev.pranav.reconnect.data.local.db.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMomentStore(private val momentDao: MomentDao): MomentStore {

    override val moments: Flow<List<PastMoment>> = momentDao.observeMoments()
        .map { it.map { entity -> entity.toModel() } }

    override suspend fun addMoment(moment: PastMoment) {
        momentDao.insertMoment(moment.toEntity())
    }

    override suspend fun getMomentsFor(contactId: String): List<PastMoment> {
        return momentDao.getAllMoments()
            .map { it.toModel() }
            .filter { it.contactIds.contains(contactId) }
    }

    override suspend fun deleteMomentsForContact(contactId: String) {
        val allMoments = momentDao.getAllMoments().map { it.toModel() }
        val affectedMoments = allMoments.filter { it.contactIds.contains(contactId) }

        affectedMoments.forEach { moment ->
            val newContactIds = moment.contactIds - contactId
            if (newContactIds.isEmpty()) {
                momentDao.deleteMoment(moment.toEntity())
            } else {
                val updatedMoment = moment.copy(contactIds = newContactIds)
                momentDao.updateMoment(updatedMoment.toEntity())
            }
        }
    }
}

