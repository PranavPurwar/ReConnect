package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.data.port.ContactStore
import dev.pranav.reconnect.data.port.MomentStore
import kotlinx.coroutines.flow.first

object DatabaseSeeder {
    suspend fun seedIfNeeded(
        contactStore: ContactStore,
        momentStore: MomentStore
    ) {
        if (contactStore.contacts.first().isNotEmpty()) return

        val seedDataSource = SampleSeedDataSource()
        val contacts = seedDataSource.getSampleContacts()
        contactStore.addContacts(contacts)

        val moments = seedDataSource.getSamplePastMoments().mapIndexed { index, moment ->
            val contact = contacts[index % contacts.size]
            moment.copy(
                contactIds = listOf(contact.id),
                createdAtEpochMs = System.currentTimeMillis() - (index * 86_400_000L)
            )
        }
        moments.forEach { moment ->
            momentStore.addMoment(moment)
        }
    }
}
