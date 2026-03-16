package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.port.ContactRepository
import dev.pranav.reconnect.data.port.MomentRepository
import dev.pranav.reconnect.data.repository.ContactRepository as LegacyContactRepository
import kotlinx.coroutines.flow.first

object DatabaseSeeder {
    suspend fun seedIfNeeded(
        contactRepository: ContactRepository,
        momentRepository: MomentRepository
    ) {
        if (contactRepository.contacts.first().isNotEmpty()) return

        val legacy = LegacyContactRepository()
        val contacts = legacy.getSampleContacts()
        contactRepository.addContacts(contacts)

        val moments = legacy.getSamplePastMoments().mapIndexed { index, moment ->
            val contactId = contacts[index % contacts.size].id
            moment.copy(contactId = contactId, createdAtEpochMs = System.currentTimeMillis() - (index * 86_400_000L))
        }
        moments.forEach { moment: PastMoment ->
            momentRepository.addMoment(moment)
        }
    }
}


