package dev.pranav.reconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.ContactFormData
import dev.pranav.reconnect.data.model.UpcomingEvent
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.data.port.ContactRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class HomeUiState(
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val quickCatchUps: List<Pair<Contact, String>> = emptyList()
)

class HomeViewModel : ViewModel() {

    private data class TimedEvent(
        val event: UpcomingEvent,
        val timeInMillis: Long
    )

    private val contactRepository: ContactRepository = AppContainer.contactRepository

    val uiState: StateFlow<HomeUiState> = contactRepository.contacts.map { contacts ->
        HomeUiState(
            upcomingEvents = deriveEvents(contacts),
            quickCatchUps = contacts.map { it to "Reconnect · ${it.reconnectInterval.label}" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun addContact(form: ContactFormData) {
        viewModelScope.launch {
            contactRepository.addContact(
                Contact(
                    id = UUID.randomUUID().toString(),
                    name = form.name.trim(),
                    phoneNumber = form.phone.trim(),
                    title = form.title.trim(),
                    relationship = form.relationship.trim(),
                    notes = form.notes.trim(),
                    reconnectInterval = form.interval,
                    isImportant = true,
                    birthdayMonth = form.birthdayMonth,
                    birthdayDay = form.birthdayDay,
                    photoUri = form.photoUri,
                    seedColorArgb = form.seedColorArgb
                )
            )
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.updateContact(contact)
        }
    }

    private fun deriveEvents(contacts: List<Contact>): List<UpcomingEvent> {
        if (contacts.isEmpty()) return emptyList()
        val now = Calendar.getInstance()
        val dowFmt = SimpleDateFormat("EEEE", Locale.getDefault())
        val monthFmt = SimpleDateFormat("MMMM", Locale.getDefault())
        val timedEvents = mutableListOf<TimedEvent>()

        contacts
            .filter { it.birthdayMonth != null && it.birthdayDay != null }
            .forEach { contact ->
                val bCal = Calendar.getInstance().apply {
                    set(Calendar.MONTH, contact.birthdayMonth!! - 1)
                    set(Calendar.DAY_OF_MONTH, contact.birthdayDay!!)
                    if (before(now)) add(Calendar.YEAR, 1)
                }
                val monthName = monthFmt.format(bCal.time).uppercase()
                timedEvents.add(
                    TimedEvent(
                        event = UpcomingEvent.Birthday(
                            contactName = contact.name,
                            contactId = contact.id,
                            age = 0,
                            day = contact.birthdayDay!!,
                            month = monthName,
                            note = ""
                        ),
                        timeInMillis = bCal.timeInMillis
                    )
                )
            }

        contacts.filter { it.isImportant }.forEach { contact ->
            val cal = now.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, contact.reconnectInterval.days)
            timedEvents.add(
                TimedEvent(
                    event = UpcomingEvent.CatchUp(
                        contactName = contact.name.split(" ").first(),
                        contactId = contact.id,
                        day = cal.get(Calendar.DAY_OF_MONTH),
                        dayOfWeek = dowFmt.format(cal.time).uppercase(),
                        seedColorArgb = contact.seedColorArgb
                    ),
                    timeInMillis = cal.timeInMillis
                )
            )
        }

        return timedEvents
            .sortedBy { it.timeInMillis }
            .map { it.event }
            .take(5)
    }
}
