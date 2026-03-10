package dev.pranav.reconnect.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import dev.pranav.reconnect.data.model.*

class ContactRepository {

    fun getDeviceContacts(contentResolver: ContentResolver): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val seenIds = mutableSetOf<String>()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIdx) ?: continue
                if (id in seenIds) continue
                seenIds.add(id)

                contacts.add(
                    Contact(
                        id = id,
                        name = it.getString(nameIdx) ?: "Unknown",
                        phoneNumber = it.getString(numberIdx) ?: "",
                        photoUri = it.getString(photoIdx)
                    )
                )
            }
        }
        return contacts
    }

    fun getSampleContacts(): List<Contact> = listOf(
        Contact(
            id = "1",
            name = "Eleanor Vance",
            title = "Creative Director",
            relationship = "Close Friend",
            isActive = true,
            isImportant = true,
            reconnectInterval = ReconnectInterval.WEEKLY
        ),
        Contact(
            id = "2",
            name = "Sarah Jenkins",
            isImportant = true,
            reconnectInterval = ReconnectInterval.MONTHLY
        ),
        Contact(
            id = "3",
            name = "Mark Thompson",
            isImportant = true,
            reconnectInterval = ReconnectInterval.BIWEEKLY
        ),
        Contact(
            id = "4",
            name = "David Chen",
            isImportant = true,
            reconnectInterval = ReconnectInterval.MONTHLY
        ),
        Contact(
            id = "5",
            name = "Elena Rodriguez",
            isImportant = true,
            reconnectInterval = ReconnectInterval.MONTHLY
        ),
        Contact(
            id = "6",
            name = "Mom",
            isImportant = true,
            reconnectInterval = ReconnectInterval.WEEKLY
        )
    )

    fun getSampleUpcomingEvents(): List<UpcomingEvent> = listOf(
        UpcomingEvent.Birthday(
            contactName = "Sarah\nJenkins",
            contactId = "2",
            age = 28,
            day = 12,
            month = "MARCH",
            note = "Turning 28. Don't forget the lilies!"
        ),
        UpcomingEvent.CatchUp(
            contactName = "Mark",
            contactId = "3",
            day = 15,
            dayOfWeek = "WEDNESDAY"
        ),
        UpcomingEvent.TimelineReminder(
            contactName = "Mom",
            contactId = "6",
            duration = "3 Months Since Mom",
            actionLabel = "CALL NOW"
        )
    )

    fun getSampleQuickCatchUps(): List<Pair<Contact, String>> = listOf(
        Contact(id = "4", name = "David Chen") to "Last spoke: 2 weeks ago",
        Contact(id = "5", name = "Elena Rodriguez") to "Suggested: Afternoon tea"
    )

    fun getSamplePastMoments(): List<PastMoment> = listOf(
        PastMoment(
            id = "1",
            title = "Dinner at Rosso's",
            description = "Celebrated her promotion. Discussed the new project timeline and shared some laughs over dessert.",
            dateLabel = "LAST WEEK",
            category = MomentCategory.DINING
        ),
        PastMoment(
            id = "2",
            title = "Gallery Opening",
            description = "Checked out the local art scene. Eleanor was really inspired by the textile works.",
            dateLabel = "OCT 12, 2023",
            category = MomentCategory.ART,
            imageUris = listOf("gallery1", "gallery2")
        ),
        PastMoment(
            id = "3",
            title = "Park Morning Walk",
            description = "Quick catch-up during her morning jog. Planned for the upcoming holiday trip.",
            dateLabel = "SEPT 24, 2023",
            category = MomentCategory.OUTDOORS
        )
    )
}

