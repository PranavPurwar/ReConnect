package dev.pranav.reconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.ContactFormData
import dev.pranav.reconnect.core.model.EventProvider
import dev.pranav.reconnect.core.model.UpcomingEvent
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class HomeUiState(
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val quickCatchUps: List<Pair<Contact, String>> = emptyList()
)
class HomeViewModel(
    private val contactStore: ContactStore = AppContainer.contactStore
): ViewModel() {
    val uiState: StateFlow<HomeUiState> = contactStore.contacts.map { contacts ->
        HomeUiState(
            upcomingEvents = EventProvider.deriveEvents(contacts, limit = 5).map { it.event },
            quickCatchUps = contacts.map { it to "Reconnect · ${it.reconnectInterval.label}" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
    fun addContact(form: ContactFormData, photoUri: String?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val contact = buildContact(form)
            try {
                contactStore.addContact(contact, photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete()
        }
    }
    fun updateContact(contact: Contact, photoUri: String?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                contactStore.updateContact(contact, photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete()
        }
    }
    private fun buildContact(form: ContactFormData): Contact {
        return Contact(
            id = UUID.randomUUID().toString(),
            name = form.name.trim(),
            phoneNumber = form.phone.trim(),
            title = form.title.trim(),
            relationship = form.relationship.trim(),
            notes = form.notes.trim(),
            reconnectInterval = form.interval,
            isImportant = true,
            birthdayYear = form.birthdayYear,
            birthdayMonth = form.birthdayMonth,
            birthdayDay = form.birthdayDay,
            seedColorArgb = form.seedColorArgb
        )
    }
}
