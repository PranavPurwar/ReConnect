package dev.pranav.reconnect.ui.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.data.port.ContactRepository
import kotlinx.coroutines.flow.*

class SocialCircleViewModel : ViewModel() {

    private val contactRepository: ContactRepository = AppContainer.contactRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredContacts: StateFlow<List<Contact>> = combine(
        contactRepository.contacts,
        _searchQuery,
        _selectedCategory
    ) { contacts, query, category ->
        contacts.filter { contact ->
            val matchesSearch = query.isBlank() ||
                contact.name.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" ||
                contact.relationship.toCircleCategory() == category
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
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


