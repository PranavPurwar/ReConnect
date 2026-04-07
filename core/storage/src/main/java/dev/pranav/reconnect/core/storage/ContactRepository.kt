package dev.pranav.reconnect.core.storage

import dev.pranav.reconnect.core.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    val contacts: Flow<List<Contact>>

    suspend fun addContacts(newContacts: List<Contact>)
    suspend fun addContact(contact: Contact)
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(contactId: String)
    suspend fun findById(contactId: String): Contact?
}

