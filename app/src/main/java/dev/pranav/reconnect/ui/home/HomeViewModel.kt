package dev.pranav.reconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.ReconnectInterval
import dev.pranav.reconnect.data.model.UpcomingEvent
import dev.pranav.reconnect.data.repository.IContactStore
import dev.pranav.reconnect.data.repository.SharedPrefsContactStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*

data class HomeUiState(
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val quickCatchUps: List<Pair<Contact, String>> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val store: IContactStore = SharedPrefsContactStore

    val uiState: StateFlow<HomeUiState> = store.contacts.map { contacts ->
        HomeUiState(
            upcomingEvents = deriveEvents(contacts),
            quickCatchUps = contacts.map { it to "Reconnect · ${it.reconnectInterval.label}" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun addContact(
        name: String,
        phone: String,
        title: String,
        relationship: String,
        interval: ReconnectInterval,
        notes: String = ""
    ) {
        store.addContact(
            Contact(
                id = System.currentTimeMillis().toString(),
                name = name.trim(),
                phoneNumber = phone.trim(),
                title = title.trim(),
                relationship = relationship.trim(),
                notes = notes.trim(),
                reconnectInterval = interval,
                isImportant = true
            )
        )
    }

    private fun deriveEvents(contacts: List<Contact>): List<UpcomingEvent> {
        if (contacts.isEmpty()) return emptyList()
        val now = Calendar.getInstance()
        val monthFmt = SimpleDateFormat("MMMM", Locale.getDefault())
        val dowFmt = SimpleDateFormat("EEEE", Locale.getDefault())
        return contacts.filter { it.isImportant }.take(3).mapIndexed { index, contact ->
            val cal = now.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, contact.reconnectInterval.days)
            val firstName = contact.name.split(" ").first()
            if (index == 0) {
                UpcomingEvent.Birthday(
                    contactName = contact.name,
                    contactId = contact.id,
                    age = 0,
                    day = cal.get(Calendar.DAY_OF_MONTH),
                    month = monthFmt.format(cal.time).uppercase(),
                    note = "Next reconnect · ${contact.reconnectInterval.label}"
                )
            } else {
                UpcomingEvent.CatchUp(
                    contactName = firstName,
                    contactId = contact.id,
                    day = cal.get(Calendar.DAY_OF_MONTH),
                    dayOfWeek = dowFmt.format(cal.time).uppercase()
                )
            }
        }
    }
}
