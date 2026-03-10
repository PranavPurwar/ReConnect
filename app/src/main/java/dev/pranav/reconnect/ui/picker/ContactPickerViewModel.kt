package dev.pranav.reconnect.ui.picker

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.ReconnectInterval
import dev.pranav.reconnect.data.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactPickerUiState(
    val contacts: List<Contact> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val intervals: Map<String, ReconnectInterval> = emptyMap(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
) {
    val filteredContacts: List<Contact>
        get() = if (searchQuery.isBlank()) contacts
        else contacts.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val selectedCount: Int get() = selectedIds.size
}

class ContactPickerViewModel : ViewModel() {

    private val repository = ContactRepository()
    private val _uiState = MutableStateFlow(ContactPickerUiState())
    val uiState: StateFlow<ContactPickerUiState> = _uiState.asStateFlow()

    fun loadContacts(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = repository.getDeviceContacts(contentResolver)
            _uiState.update { it.copy(contacts = contacts, isLoading = false) }
        }
    }

    fun toggleContact(id: String) {
        _uiState.update { state ->
            val newSelected = state.selectedIds.toMutableSet()
            val newIntervals = state.intervals.toMutableMap()
            if (id in newSelected) {
                newSelected.remove(id)
                newIntervals.remove(id)
            } else {
                newSelected.add(id)
                newIntervals[id] = ReconnectInterval.MONTHLY
            }
            state.copy(selectedIds = newSelected, intervals = newIntervals)
        }
    }

    fun setInterval(id: String, interval: ReconnectInterval) {
        _uiState.update { state ->
            state.copy(intervals = state.intervals + (id to interval))
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

