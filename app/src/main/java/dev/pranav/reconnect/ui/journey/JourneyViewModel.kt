package dev.pranav.reconnect.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.core.storage.MomentStore
import dev.pranav.reconnect.data.port.AppContainer
import kotlinx.coroutines.flow.*

data class JourneyItem(
    val moment: PastMoment,
    val contactNames: String
)

data class JourneyUiState(
    val filteredItems: List<JourneyItem> = emptyList(),
    val selectedCategory: MomentCategory? = null
)

class JourneyViewModel(
    contactStore: ContactStore = AppContainer.contactStore,
    momentStore: MomentStore = AppContainer.momentStore
): ViewModel() {

    private val _selectedCategory = MutableStateFlow<MomentCategory?>(null)

    val uiState: StateFlow<JourneyUiState> = combine(
        contactStore.contacts,
        momentStore.moments,
        _selectedCategory
    ) { contacts, moments, selectedCategory ->
        val allItems = buildJourneyItems(contacts = contacts, moments = moments)
        JourneyUiState(
            filteredItems = applyCategoryFilter(
                items = allItems,
                selectedCategory = selectedCategory
            ),
            selectedCategory = selectedCategory
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JourneyUiState()
    )

    fun setFilter(category: MomentCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private fun buildJourneyItems(
        contacts: List<dev.pranav.reconnect.core.model.Contact>,
        moments: List<PastMoment>
    ): List<JourneyItem> {
        val contactMap = contacts.associateBy { it.id }
        return moments.mapNotNull { moment ->
            val involvedContacts = moment.contactIds.mapNotNull { contactMap[it] }
            if (involvedContacts.isEmpty()) return@mapNotNull null
            JourneyItem(
                moment = moment,
                contactNames = involvedContacts.joinToString(", ") { it.name }
            )
        }
    }

    private fun applyCategoryFilter(
        items: List<JourneyItem>,
        selectedCategory: MomentCategory?
    ): List<JourneyItem> {
        if (selectedCategory == null) return items
        return items.filter { it.moment.category == selectedCategory }
    }
}
