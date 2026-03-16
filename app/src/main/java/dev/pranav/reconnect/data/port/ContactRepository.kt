package dev.pranav.reconnect.data.port

import dev.pranav.reconnect.data.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    val contacts: Flow<List<Contact>>

    suspend fun addContacts(newContacts: List<Contact>)
    suspend fun addContact(contact: Contact)
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(contactId: String)
    suspend fun findById(contactId: String): Contact?
}

