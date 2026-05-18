package dev.pranav.reconnect.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.core.storage.MomentStore
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MomentDetailUiState(
    val moment: PastMoment? = null,
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = true
)

class MomentDetailViewModel(
    private val momentId: String,
    private val contactStore: ContactStore = AppContainer.contactStore,
    private val momentStore: MomentStore = AppContainer.momentStore
): ViewModel() {

    private val _uiState = MutableStateFlow(MomentDetailUiState())
    val uiState: StateFlow<MomentDetailUiState> = _uiState.asStateFlow()

    init {
        loadMoment()
    }

    private fun loadMoment() {
        viewModelScope.launch {
            val allMoments = momentStore.moments.first()
            val moment = allMoments.find { it.id == momentId }
            if (moment != null) {
                val allContacts = contactStore.contacts.first()
                val involvedContacts = allContacts.filter { contact ->
                    moment.contactIds.contains(contact.id)
                }
                _uiState.value = MomentDetailUiState(
                    moment = moment,
                    contacts = involvedContacts,
                    isLoading = false
                )
            } else {
                _uiState.value = MomentDetailUiState(isLoading = false)
            }
        }
    }
}
