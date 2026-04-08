package dev.pranav.reconnect.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.MomentImage
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.AiInsightStore
import dev.pranav.reconnect.core.storage.AttachmentStore
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.core.storage.MomentStore
import dev.pranav.reconnect.data.port.AppContainer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
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

class ConnectionDetailViewModel(
    contactStore: ContactStore = AppContainer.contactStore,
    momentStore: MomentStore = AppContainer.momentStore,
    attachmentStore: AttachmentStore = AppContainer.attachmentStore,
    aiInsightStore: AiInsightStore = AppContainer.aiInsightStore
): ViewModel() {

    private val _contactStore = contactStore
    private val _momentStore = momentStore
    private val _attachmentStore = attachmentStore
    private val _aiInsightStore = aiInsightStore

    private val _contactId = MutableStateFlow<String?>(null)
    private val _selectedCategory = MutableStateFlow<MomentCategory?>(null)

    val uiState: StateFlow<PersonDetailUiState> = combine(
        _contactStore.contacts,
        _momentStore.moments,
        _contactId,
        _selectedCategory
    ) { contacts, moments, contactId, selectedCategory ->
        val contact = contactId?.let { id -> contacts.find { it.id == id } }
        val contactMoments =
            if (contactId != null) moments.filter { it.contactIds.contains(contactId) } else emptyList()
        val filteredMoments = if (selectedCategory == null) contactMoments
        else contactMoments.filter { it.category == selectedCategory }

        val daysSinceLastContact = contactMoments.maxByOrNull { it.dateEpochMs }?.let {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it.dateEpochMs).toInt()
                .coerceAtLeast(0)
        }
        val daysUntilBirthday = contact?.let { calculateDaysUntilBirthday(it) }
        val relationshipHealth = deriveHealth(daysSinceLastContact, contactMoments.size)
        val aiPrepBullets = generateAiPrepBullets(contact, contactMoments, _aiInsightStore)

        PersonDetailUiState(
            contact = contact,
            nextTalkDate = contact?.let { calculateNextTalkDate(it.reconnectInterval.days) } ?: "",
            toDiscuss = contactMoments.firstOrNull()?.title
                ?: "Tap 'Log Moment' to start tracking your conversations.",
            pastMoments = contactMoments,
            filteredMoments = filteredMoments,
            selectedCategory = selectedCategory,
            daysUntilBirthday = daysUntilBirthday,
            relationshipHealth = relationshipHealth,
            daysSinceLastContact = daysSinceLastContact,
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
        viewModelScope.launch {
            _contactStore.updateContact(contact.copy(isImportant = !contact.isImportant))
        }
    }

    fun deleteContact() {
        val contact = uiState.value.contact ?: return
        viewModelScope.launch {
            val moments = _momentStore.getMomentsFor(contact.id)
            moments.forEach { moment ->
                _attachmentStore.deleteMomentAttachments(moment.id)
            }
            _momentStore.deleteMomentsForContact(contact.id)
            _contactStore.deleteContact(contact.id)
        }
    }

    fun setFilter(category: MomentCategory?) {
        _selectedCategory.value = category
    }

    fun logMoment(
        contactId: String,
        title: String,
        description: String,
        category: MomentCategory,
        images: List<MomentImage> = emptyList(),
        isCoreMemory: Boolean = false,
        wasPresent: Boolean = true,
        groupName: String? = null,
        locationMood: String? = null,
        momentId: String = UUID.randomUUID().toString(),
        additionalContactIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            val combinedIds = buildSet {
                add(contactId)
                addAll(additionalContactIds)
            }.toList()

            _momentStore.addMoment(
                PastMoment(
                    id = momentId,
                    contactIds = combinedIds,
                    title = title,
                    description = description,
                    dateEpochMs = now,
                    category = category,
                    images = images,
                    isCoreMemory = isCoreMemory,
                    wasPresent = wasPresent,
                    groupName = groupName,
                    locationMood = locationMood,
                    createdAtEpochMs = now
                )
            )
        }
    }

    private fun generateAiPrepBullets(
        contact: Contact?,
        moments: List<PastMoment>,
        aiStore: AiInsightStore
    ): List<String> {
        val fallbackBullets = if (moments.isNotEmpty()) listOf(
            "Catch up on: ${moments.first().title}",
            "Ask how things have been since you last spoke",
            "Share something new happening in your life"
        ) else emptyList()

        return if (contact != null) {
            runCatching {
                aiStore.getPrepBullets(contact.id, fallbackBullets)
            }.getOrDefault(fallbackBullets)
        } else {
            fallbackBullets
        }
    }

    private fun calculateNextTalkDate(days: Int): String {
        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, days) }
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
        val date = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
        return "$dayName, $date"
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
        return TimeUnit.MILLISECONDS.toDays(bCal.timeInMillis - now.timeInMillis).toInt()
            .coerceAtLeast(0)
    }

    private fun deriveHealth(daysSince: Int?, momentCount: Int): RelationshipHealth = when {
        daysSince == null -> if (momentCount == 0) RelationshipHealth.FADING else RelationshipHealth.NEUTRAL
        daysSince <= 30 -> RelationshipHealth.STRONG
        daysSince <= 60 -> RelationshipHealth.NEUTRAL
        else -> RelationshipHealth.FADING
    }
}
