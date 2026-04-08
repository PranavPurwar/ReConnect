package dev.pranav.reconnect.core.storage

import android.content.ContentResolver
import android.provider.ContactsContract
import dev.pranav.reconnect.core.model.Contact

class DeviceContactsDataSource {
    fun getSystemContacts(contentResolver: ContentResolver): List<Contact> {
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
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIdx) ?: continue
                if (id in seenIds) continue
                seenIds.add(id)

                contacts.add(
                    Contact(
                        id = "device_$id",
                        name = it.getString(nameIdx) ?: "Unknown",
                        phoneNumber = it.getString(numberIdx) ?: "",
                        photoUri = it.getString(photoIdx)
                    )
                )
            }
        }
        return contacts
    }
}


