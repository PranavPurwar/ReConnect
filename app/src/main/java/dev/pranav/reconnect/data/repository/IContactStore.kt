package dev.pranav.reconnect.data.repository

import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.PastMoment
import kotlinx.coroutines.flow.StateFlow

interface IContactStore {
    val contacts: StateFlow<List<Contact>>
    val moments: StateFlow<List<PastMoment>>

    fun addContacts(newContacts: List<Contact>)
    fun addContact(contact: Contact)
    fun updateContact(contact: Contact)
    fun addMoment(moment: PastMoment)
    fun getMomentsFor(contactId: String): List<PastMoment>
    fun deleteContact(contactId: String)
}

