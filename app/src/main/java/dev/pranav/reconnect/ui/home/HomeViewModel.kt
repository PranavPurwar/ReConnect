package dev.pranav.reconnect.ui.home

import androidx.lifecycle.ViewModel
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.UpcomingEvent
import dev.pranav.reconnect.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val quickCatchUps: List<Pair<Contact, String>> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val repository = ContactRepository()

    private val _uiState = MutableStateFlow(
        HomeUiState(
            upcomingEvents = repository.getSampleUpcomingEvents(),
            quickCatchUps = repository.getSampleQuickCatchUps()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

