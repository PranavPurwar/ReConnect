package dev.pranav.reconnect.data.port

import dev.pranav.reconnect.data.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactStore {
    val contacts: Flow<List<Contact>>

    suspend fun addContacts(newContacts: List<Contact>)
    suspend fun addContact(contact: Contact, avatarUri: String? = null)
    suspend fun updateContact(contact: Contact, avatarUri: String? = null)
    suspend fun deleteContact(contactId: String)
    suspend fun findById(contactId: String): Contact?
}
