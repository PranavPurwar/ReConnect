package dev.pranav.reconnect.ui.picker

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.ReconnectInterval
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.core.storage.DeviceContactsDataSource
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class ContactPickerUiState(
    val contacts: List<Contact> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val intervals: Map<String, ReconnectInterval> = emptyMap(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val needsContactsPermission: Boolean = false
) {
    val selectedCount: Int get() = selectedIds.size

    companion object {
        fun filterContacts(contacts: List<Contact>, query: String): List<Contact> {
            return if (query.isBlank()) contacts
            else contacts.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}

class ContactPickerViewModel(
    contactStore: ContactStore = AppContainer.contactStore,
    private val repository: DeviceContactsDataSource = DeviceContactsDataSource()
): ViewModel() {

    private val _contactStore = contactStore
    private val _uiState = MutableStateFlow(ContactPickerUiState())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val filteredContacts: StateFlow<List<Contact>> = combine(
        _uiState.map { it.contacts },
        _searchQuery.debounce(150L.milliseconds)
    ) { contacts, query ->
        ContactPickerUiState.filterContacts(contacts, query)
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<ContactPickerUiState> = _uiState.asStateFlow()

    fun loadContacts(contentResolver: ContentResolver) {
        _uiState.update { it.copy(isLoading = true, needsContactsPermission = false) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemContacts = repository.getSystemContacts(contentResolver)
                _uiState.update {
                    it.copy(
                        contacts = systemContacts,
                        isLoading = false,
                        needsContactsPermission = false
                    )
                }
            } catch (_: SecurityException) {
                _uiState.update {
                    it.copy(
                        contacts = emptyList(),
                        isLoading = false,
                        needsContactsPermission = true
                    )
                }
            }
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
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun confirmSelection() {
        val state = _uiState.value
        val selected = state.contacts
            .filter { it.id in state.selectedIds }
            .map { contact ->
                contact.copy(
                    reconnectInterval = state.intervals[contact.id] ?: ReconnectInterval.MONTHLY,
                    isImportant = true
                )
            }
        viewModelScope.launch(Dispatchers.IO) {
            _contactStore.addContacts(selected)
        }
    }

    fun importSelected() {
        val selected = _uiState.value.contacts.filter { it.id in _uiState.value.selectedIds }
        viewModelScope.launch(Dispatchers.IO) {
            _contactStore.addContacts(
                selected.map {
                    it.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        isActive = true,
                        isImportant = true,
                        reconnectInterval = ReconnectInterval.MONTHLY
                    )
                }
            )
        }
    }
}
