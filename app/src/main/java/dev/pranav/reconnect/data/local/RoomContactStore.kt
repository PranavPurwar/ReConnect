package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.data.local.db.ContactDao
import dev.pranav.reconnect.data.local.db.toEntity
import dev.pranav.reconnect.data.local.db.toModel
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.port.ContactStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomContactStore(private val contactsDao: ContactDao): ContactStore {

    override val contacts: Flow<List<Contact>> = contactsDao.observeContacts()
        .map { entities -> entities.map { it.toModel() } }

    override suspend fun addContacts(newContacts: List<Contact>) {
        contactsDao.insertContacts(newContacts.map { it.toEntity() })
    }

    override suspend fun addContact(contact: Contact, avatarUri: String?) {
        contactsDao.insertContact(contact.toEntity())
    }

    override suspend fun updateContact(contact: Contact, avatarUri: String?) {
        contactsDao.updateContact(contact.toEntity())
    }

    override suspend fun deleteContact(contactId: String) {
        contactsDao.deleteContact(contactId)
    }

    override suspend fun findById(contactId: String): Contact? {
        return contactsDao.findById(contactId)?.toModel()
    }
}
