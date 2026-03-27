package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.data.local.db.MomentDao
import dev.pranav.reconnect.data.local.db.toEntity
import dev.pranav.reconnect.data.local.db.toModel
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.port.MomentRepository
import dev.pranav.reconnect.data.port.StorageMetricsRecorder
import dev.pranav.reconnect.data.port.trackRead
import dev.pranav.reconnect.data.port.trackWrite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalMomentRepository(
    private val dao: MomentDao,
    private val metricsRecorder: StorageMetricsRecorder
) : MomentRepository {

    override val moments: Flow<List<PastMoment>> = dao.observeMoments().map { entities ->
        metricsRecorder.trackRead(name = "moment.observe", itemCount = entities.size) {
            entities.map { it.toModel() }
        }
    }

    override suspend fun addMoment(moment: PastMoment) {
        metricsRecorder.trackWrite(name = "moment.insert") {
            dao.insertMoment(moment.toEntity())
        }
    }

    override suspend fun getMomentsFor(contactId: String): List<PastMoment> {
        return metricsRecorder.trackRead(name = "moment.byContact") {
            dao.getAllMoments().map { it.toModel() }
                .filter { it.contactIds.contains(contactId) }
        }
    }

    override suspend fun deleteMomentsForContact(contactId: String) {
        metricsRecorder.trackWrite(name = "moment.deleteByContact") {
            val allMoments = dao.getAllMoments().map { it.toModel() }
            val affectedMoments = allMoments.filter { it.contactIds.contains(contactId) }

            affectedMoments.forEach { moment ->
                val newContactIds = moment.contactIds - contactId
                if (newContactIds.isEmpty()) {
                    dao.deleteMoment(moment.toEntity())
                } else {
                    val updatedMoment = moment.copy(contactIds = newContactIds)
                    dao.updateMoment(updatedMoment.toEntity())
                }
            }
        }
    }
}

