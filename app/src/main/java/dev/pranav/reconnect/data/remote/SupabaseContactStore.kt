package dev.pranav.reconnect.data.remote

import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.port.ContactStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SupabaseContactStore(private val client: SupabaseClient): ContactStore {

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(SupabaseExperimental::class)
    override val contacts: Flow<List<Contact>> = client.from("contacts")
        .selectAsFlow(Contact::id)
        .map { it }
        .stateIn(
            scope = storeScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    override suspend fun addContacts(newContacts: List<Contact>) {
        client.postgrest["contacts"].insert(newContacts)
    }

    override suspend fun addContact(contact: Contact) {
        client.postgrest["contacts"].insert(contact)
    }

    override suspend fun updateContact(contact: Contact) {
        client.postgrest["contacts"].update(contact) {
            filter {
                eq("id", contact.id)
            }
        }
    }

    override suspend fun deleteContact(contactId: String) {
        client.postgrest["contacts"].delete {
            filter {
                eq("id", contactId)
            }
        }
    }

    override suspend fun findById(contactId: String): Contact? {
        return client.postgrest["contacts"].select {
            filter {
                eq("id", contactId)
            }
        }.decodeSingleOrNull()
    }
}

