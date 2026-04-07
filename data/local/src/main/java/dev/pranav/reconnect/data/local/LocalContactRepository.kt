package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.storage.ContactRepository
import dev.pranav.reconnect.core.storage.StorageMetricsRecorder
import dev.pranav.reconnect.core.storage.trackRead
import dev.pranav.reconnect.core.storage.trackWrite
import dev.pranav.reconnect.data.local.db.ContactDao
import dev.pranav.reconnect.data.local.db.toEntity
import dev.pranav.reconnect.data.local.db.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalContactRepository(
    private val dao: ContactDao,
    private val metricsRecorder: StorageMetricsRecorder
) : ContactRepository {

    override val contacts: Flow<List<Contact>> = dao.observeContacts().map { entities ->
        metricsRecorder.trackRead(name = "contact.observe", itemCount = entities.size) {
            entities.map { it.toModel() }
        }
    }

    override suspend fun addContacts(newContacts: List<Contact>) {
        metricsRecorder.trackWrite(name = "contact.insert.bulk") {
            dao.insertContacts(newContacts.map { it.toEntity() })
        }
    }

    override suspend fun addContact(contact: Contact) {
        metricsRecorder.trackWrite(name = "contact.insert.single") {
            dao.insertContact(contact.toEntity())
        }
    }

    override suspend fun updateContact(contact: Contact) {
        metricsRecorder.trackWrite(name = "contact.update") {
            dao.updateContact(contact.toEntity())
        }
    }

    override suspend fun deleteContact(contactId: String) {
        metricsRecorder.trackWrite(name = "contact.delete") {
            dao.deleteContact(contactId)
        }
    }

    override suspend fun findById(contactId: String): Contact? {
        return metricsRecorder.trackRead(name = "contact.findById") {
            dao.findById(contactId)?.toModel()
        }
    }

    suspend fun count(): Int = dao.count()
}

