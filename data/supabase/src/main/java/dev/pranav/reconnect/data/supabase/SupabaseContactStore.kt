package dev.pranav.reconnect.data.supabase

import android.content.Context
import androidx.core.net.toUri
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.storage.ContactStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SupabaseContactStore(
    private val client: SupabaseClient,
    private val context: Context
): ContactStore {

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

    override suspend fun addContact(contact: Contact, avatarUri: String?) {
        uploadPhotoIfNeeded(contact.id, avatarUri)
        client.postgrest["contacts"].insert(contact)
    }

    override suspend fun updateContact(contact: Contact, avatarUri: String?) {
        uploadPhotoIfNeeded(contact.id, avatarUri)
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

    private suspend fun uploadPhotoIfNeeded(contactId: String, uriStr: String?) {
        if (uriStr == null || uriStr.startsWith("http")) return

        val user = client.auth.currentUserOrNull() ?: return
        val path = "${user.id}/$contactId/photo.jpg"

        try {
            val bytes =
                context.contentResolver.openInputStream(uriStr.toUri())?.use { it.readBytes() }
                ?: return

            client.storage.from("contacts").upload(path, bytes) {
                upsert = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
