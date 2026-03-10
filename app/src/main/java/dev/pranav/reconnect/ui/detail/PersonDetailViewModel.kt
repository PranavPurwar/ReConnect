package dev.pranav.reconnect.ui.detail

import androidx.lifecycle.ViewModel
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PersonDetailUiState(
    val contact: Contact? = null,
    val toDiscuss: String = "",
    val nextTalkDate: String = "",
    val pastMoments: List<PastMoment> = emptyList()
)

class PersonDetailViewModel : ViewModel() {

    private val repository = ContactRepository()
    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    fun loadContact(contactId: String) {
        val contact = repository.getSampleContacts().find { it.id == contactId }
        _uiState.value = PersonDetailUiState(
            contact = contact,
            toDiscuss = "Coffee at The Daily Grind. Recent travel plans and that new book recommendation!",
            nextTalkDate = "Friday, 10:00 AM",
            pastMoments = repository.getSamplePastMoments()
        )
    }
}

