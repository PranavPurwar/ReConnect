package dev.pranav.reconnect.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.MomentStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MomentsViewModel(
    private val momentStore: MomentStore
): ViewModel() {

    private val _moments = MutableStateFlow<List<PastMoment>>(emptyList())
    val moments: StateFlow<List<PastMoment>> = _moments

    init {
        fetchMoments()
    }

    private fun fetchMoments() {
        viewModelScope.launch {
            momentStore.moments.collectLatest { fetchedMoments ->
                _moments.value = fetchedMoments
            }
        }
    }
}
