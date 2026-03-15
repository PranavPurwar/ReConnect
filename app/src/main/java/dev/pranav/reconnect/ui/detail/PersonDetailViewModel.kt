package dev.pranav.reconnect.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.repository.IContactStore
import dev.pranav.reconnect.data.repository.SharedPrefsContactStore
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class RelationshipHealth { STRONG, NEUTRAL, FADING }

data class PersonDetailUiState(
    val contact: Contact? = null,
    val toDiscuss: String = "",
    val nextTalkDate: String = "",
    val pastMoments: List<PastMoment> = emptyList(),
    val filteredMoments: List<PastMoment> = emptyList(),
    val selectedCategory: MomentCategory? = null,
    val daysSinceLastContact: Int? = null,
    val daysUntilBirthday: Int? = null,
    val relationshipHealth: RelationshipHealth = RelationshipHealth.NEUTRAL,
    val aiPrepBullets: List<String> = emptyList()
)

class PersonDetailViewModel : ViewModel() {

    private val store: IContactStore = SharedPrefsContactStore
    private val _contactId = MutableStateFlow<String?>(null)
    private val _selectedCategory = MutableStateFlow<MomentCategory?>(null)

    val uiState: StateFlow<PersonDetailUiState> = combine(
        store.contacts,
        store.moments,
        _contactId,
        _selectedCategory
    ) { contacts, moments, contactId, selectedCategory ->
        val contact = contactId?.let { id -> contacts.find { it.id == id } }
        val contactMoments = moments.filter { it.contactId == contactId }
        val filteredMoments = if (selectedCategory == null) contactMoments
        else contactMoments.filter { it.category == selectedCategory }

        val daysSinceLastContact = contactMoments.firstOrNull()?.let { parseDaysSince(it.dateLabel) }
        val daysUntilBirthday = contact?.let { calculateDaysUntilBirthday(it) }
        val relationshipHealth = deriveHealth(daysSinceLastContact, contactMoments.size)
        val aiPrepBullets = if (contactMoments.isNotEmpty()) listOf(
            "Catch up on: ${contactMoments.first().title}",
            "Ask how things have been since you last spoke",
            "Share something new happening in your life"
        ) else emptyList()

        PersonDetailUiState(
            contact = contact,
            nextTalkDate = contact?.let { calculateNextTalkDate(it.reconnectInterval.days) } ?: "",
            toDiscuss = contactMoments.firstOrNull()?.title
                ?: "Tap 'Log Moment' to start tracking your conversations.",
            pastMoments = contactMoments,
            filteredMoments = filteredMoments,
            selectedCategory = selectedCategory,
            daysSinceLastContact = daysSinceLastContact,
            daysUntilBirthday = daysUntilBirthday,
            relationshipHealth = relationshipHealth,
            aiPrepBullets = aiPrepBullets
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PersonDetailUiState()
    )

    fun loadContact(contactId: String) {
        _contactId.value = contactId
    }

    fun toggleImportant() {
        val contact = uiState.value.contact ?: return
        store.updateContact(contact.copy(isImportant = !contact.isImportant))
    }

    fun deleteContact() {
        val contact = uiState.value.contact ?: return
        store.deleteContact(contact.id)
    }

    fun setFilter(category: MomentCategory?) {
        _selectedCategory.value = category
    }

    fun logMoment(
        contactId: String,
        title: String,
        description: String,
        category: MomentCategory,
        imageUris: List<String> = emptyList()
    ) {
        val dateLabel = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        store.addMoment(
            PastMoment(
                id = System.currentTimeMillis().toString(),
                contactId = contactId,
                title = title.trim(),
                description = description.trim(),
                dateLabel = dateLabel,
                category = category,
                imageUris = imageUris
            )
        )
    }

    private fun calculateNextTalkDate(days: Int): String {
        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, days) }
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
        val date = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
        return "$dayName, $date"
    }

    private fun parseDaysSince(dateLabel: String): Int? {
        val formats = listOf("MMM d, yyyy", "MMM dd, yyyy", "MMMM d, yyyy")
        for (fmt in formats) {
            try {
                val parsed = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateLabel)
                if (parsed != null) {
                    val diff = Date().time - parsed.time
                    return TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
                }
            } catch (_: Exception) { }
        }
        return null
    }

    private fun calculateDaysUntilBirthday(contact: Contact): Int? {
        val month = contact.birthdayMonth ?: return null
        val day = contact.birthdayDay ?: return null
        val now = Calendar.getInstance()
        val bCal = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.YEAR, 1)
        }
        return TimeUnit.MILLISECONDS.toDays(bCal.timeInMillis - now.timeInMillis).toInt().coerceAtLeast(0)
    }

    private fun deriveHealth(daysSince: Int?, momentCount: Int): RelationshipHealth = when {
        daysSince == null -> if (momentCount == 0) RelationshipHealth.FADING else RelationshipHealth.NEUTRAL
        daysSince <= 30 -> RelationshipHealth.STRONG
        daysSince <= 60 -> RelationshipHealth.NEUTRAL
        else -> RelationshipHealth.FADING
    }
}
