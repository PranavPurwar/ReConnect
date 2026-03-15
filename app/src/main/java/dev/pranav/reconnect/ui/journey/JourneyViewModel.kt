package dev.pranav.reconnect.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.data.repository.IContactStore
import dev.pranav.reconnect.data.repository.SharedPrefsContactStore
import kotlinx.coroutines.flow.*

data class JourneyItem(
    val moment: PastMoment,
    val contactName: String,
    val contactId: String
)

data class JourneyUiState(
    val allItems: List<JourneyItem> = emptyList(),
    val filteredItems: List<JourneyItem> = emptyList(),
    val selectedCategory: MomentCategory? = null
)

class JourneyViewModel : ViewModel() {

    private val store: IContactStore = SharedPrefsContactStore
    private val _selectedCategory = MutableStateFlow<MomentCategory?>(null)

    val uiState: StateFlow<JourneyUiState> = combine(
        store.contacts,
        store.moments,
        _selectedCategory
    ) { contacts, moments, selectedCategory ->
        val contactMap = contacts.associateBy { it.id }
        val allItems = moments.mapNotNull { moment ->
            val contact = contactMap[moment.contactId] ?: return@mapNotNull null
            JourneyItem(moment, contact.name, contact.id)
        }
        val filtered = if (selectedCategory != null)
            allItems.filter { it.moment.category == selectedCategory }
        else allItems
        JourneyUiState(allItems = allItems, filteredItems = filtered, selectedCategory = selectedCategory)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JourneyUiState()
    )

    fun setFilter(category: MomentCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
}

