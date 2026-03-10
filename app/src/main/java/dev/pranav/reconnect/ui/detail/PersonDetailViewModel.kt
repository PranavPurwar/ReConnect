package dev.pranav.reconnect.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.repository.IContactStore
import dev.pranav.reconnect.data.repository.SharedPrefsContactStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*

data class PersonDetailUiState(
    val contact: Contact? = null,
    val toDiscuss: String = "",
    val nextTalkDate: String = "",
    val pastMoments: List<PastMoment> = emptyList()
)

class PersonDetailViewModel : ViewModel() {

    private val store: IContactStore = SharedPrefsContactStore
    private val _contactId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PersonDetailUiState> = combine(
        store.contacts,
        store.moments,
        _contactId
    ) { contacts, moments, contactId ->
        val contact = contactId?.let { id -> contacts.find { it.id == id } }
        val contactMoments = moments.filter { it.contactId == contactId }
        PersonDetailUiState(
            contact = contact,
            nextTalkDate = contact?.let { calculateNextTalkDate(it.reconnectInterval.days) } ?: "",
            toDiscuss = contactMoments.firstOrNull()?.title
                ?: "Tap 'Log Moment' to start tracking your conversations.",
            pastMoments = contactMoments
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PersonDetailUiState()
    )

    fun loadContact(contactId: String) {
        _contactId.value = contactId
    }

    fun logMoment(contactId: String, title: String, description: String, category: MomentCategory) {
        val dateLabel = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
        store.addMoment(
            PastMoment(
                id = System.currentTimeMillis().toString(),
                contactId = contactId,
                title = title.trim(),
                description = description.trim(),
                dateLabel = dateLabel,
                category = category
            )
        )
    }

    private fun calculateNextTalkDate(days: Int): String {
        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, days) }
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
        val date = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
        return "$dayName, $date"
    }
}
