package dev.pranav.reconnect.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.model.ReconnectInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object SharedPrefsContactStore : IContactStore {

    private const val PREFS_NAME = "reconnect_contact_store"
    private const val KEY_CONTACTS = "contacts"
    private const val KEY_MOMENTS = "moments"
    private const val KEY_SEEDED = "seeded"

    private lateinit var prefs: SharedPreferences

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    override val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _moments = MutableStateFlow<List<PastMoment>>(emptyList())
    override val moments: StateFlow<List<PastMoment>> = _moments.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            seedSampleData()
        } else {
            _contacts.value = loadContacts()
            _moments.value = loadMoments()
        }
    }

    private fun seedSampleData() {
        val repository = ContactRepository()
        _contacts.value = repository.getSampleContacts()
        _moments.value = repository.getSamplePastMoments().map { it.copy(contactId = "1") }
        persistContacts()
        persistMoments()
        prefs.edit { putBoolean(KEY_SEEDED, true) }
    }

    override fun addContacts(newContacts: List<Contact>) {
        val existingIds = _contacts.value.map { it.id }.toSet()
        val toAdd = newContacts.filter { it.id !in existingIds }
        if (toAdd.isEmpty()) return
        _contacts.value = _contacts.value + toAdd
        persistContacts()
    }

    override fun addContact(contact: Contact) {
        if (_contacts.value.any { it.id == contact.id }) return
        _contacts.value = _contacts.value + contact
        persistContacts()
    }

    override fun updateContact(contact: Contact) {
        _contacts.value = _contacts.value.map { if (it.id == contact.id) contact else it }
        persistContacts()
    }

    override fun addMoment(moment: PastMoment) {
        _moments.value = listOf(moment) + _moments.value
        persistMoments()
    }

    override fun getMomentsFor(contactId: String): List<PastMoment> =
        _moments.value.filter { it.contactId == contactId }

    override fun deleteContact(contactId: String) {
        _contacts.value = _contacts.value.map {
           if (it.id == contactId) null else it
        }.filterNotNull()
        persistContacts()
    }

    private fun persistContacts() {
        val arr = JSONArray()
        _contacts.value.forEach { arr.put(it.toJson()) }
        prefs.edit { putString(KEY_CONTACTS, arr.toString()) }
    }

    private fun persistMoments() {
        val arr = JSONArray()
        _moments.value.forEach { arr.put(it.toJson()) }
        prefs.edit { putString(KEY_MOMENTS, arr.toString()) }
    }

    private fun loadContacts(): List<Contact> {
        val json = prefs.getString(KEY_CONTACTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getJSONObject(it).toContact() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadMoments(): List<PastMoment> {
        val json = prefs.getString(KEY_MOMENTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getJSONObject(it).toPastMoment() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun Contact.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("title", title)
        put("relationship", relationship)
        put("notes", notes)
        photoUri?.let { put("photoUri", it) }
        seedColorArgb?.let { put("seedColorArgb", it) }
        put("phoneNumber", phoneNumber)
        put("isActive", isActive)
        put("isImportant", isImportant)
        put("reconnectInterval", reconnectInterval.name)
        birthdayMonth?.let { put("birthdayMonth", it) }
        birthdayDay?.let { put("birthdayDay", it) }
    }

    private fun JSONObject.toContact(): Contact = Contact(
        id = getString("id"),
        name = getString("name"),
        title = optString("title", ""),
        relationship = optString("relationship", ""),
        notes = optString("notes", ""),
        photoUri = optString("photoUri").takeIf { it.isNotBlank() },
        seedColorArgb = if (has("seedColorArgb")) getInt("seedColorArgb") else null,
        phoneNumber = optString("phoneNumber", ""),
        isActive = optBoolean("isActive", false),
        isImportant = optBoolean("isImportant", false),
        reconnectInterval = try {
            ReconnectInterval.valueOf(optString("reconnectInterval", ReconnectInterval.MONTHLY.name))
        } catch (e: Exception) {
            ReconnectInterval.MONTHLY
        },
        birthdayMonth = if (has("birthdayMonth")) getInt("birthdayMonth") else null,
        birthdayDay = if (has("birthdayDay")) getInt("birthdayDay") else null
    )

    private fun PastMoment.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("contactId", contactId)
        put("title", title)
        put("description", description)
        put("dateLabel", dateLabel)
        put("category", category.name)
        val imgArr = JSONArray()
        imageUris.forEach { imgArr.put(it) }
        put("imageUris", imgArr)
    }

    private fun JSONObject.toPastMoment(): PastMoment = PastMoment(
        id = getString("id"),
        contactId = optString("contactId", ""),
        title = getString("title"),
        description = optString("description", ""),
        dateLabel = optString("dateLabel", ""),
        category = try {
            MomentCategory.valueOf(optString("category", MomentCategory.GENERAL.name))
        } catch (e: Exception) {
            MomentCategory.GENERAL
        },
        imageUris = run {
            val arr = optJSONArray("imageUris") ?: return@run emptyList()
            (0 until arr.length()).map { arr.getString(it) }
        }
    )
}

