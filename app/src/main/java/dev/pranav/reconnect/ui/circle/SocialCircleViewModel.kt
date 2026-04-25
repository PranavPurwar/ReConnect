package dev.pranav.reconnect.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.debounce

@kotlinx.coroutines.FlowPreview
class SocialCircleViewModel(
    contactStore: ContactStore = AppContainer.contactStore
): ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredContacts: StateFlow<List<Contact>> = combine(
        contactStore.contacts,
        _searchQuery.debounce(150L),
        _selectedCategory
    ) { contacts, query, category ->
        applyFilters(contacts, query, category)
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun applyFilters(
        contacts: List<Contact>,
        query: String,
        category: String
    ): List<Contact> {
        return contacts.filter { contact ->
            val matchesSearch = query.isBlank() ||
                    contact.name.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" ||
                    contact.relationship.toCircleCategory() == category
            matchesSearch && matchesCategory
        }
    }
}

fun String.toCircleCategory(): String = when {
    contains("family", ignoreCase = true) ||
    contains("mom", ignoreCase = true) ||
    contains("dad", ignoreCase = true) ||
    contains("parent", ignoreCase = true) ||
    contains("sibling", ignoreCase = true) ||
    contains("brother", ignoreCase = true) ||
    contains("sister", ignoreCase = true) -> "Family"

    contains("friend", ignoreCase = true) ||
    contains("buddy", ignoreCase = true) ||
    contains("pal", ignoreCase = true) -> "Friends"

    contains("colleague", ignoreCase = true) ||
    contains("coworker", ignoreCase = true) ||
    contains("co-worker", ignoreCase = true) ||
    contains("work", ignoreCase = true) ||
    contains("manager", ignoreCase = true) ||
    contains("boss", ignoreCase = true) -> "Work"

    else -> "Other"
}

fun String.toTagLabel(): String = toCircleCategory().uppercase()
