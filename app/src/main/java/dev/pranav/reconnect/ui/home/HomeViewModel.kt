package dev.pranav.reconnect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pranav.reconnect.core.model.*
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.core.storage.MomentStore
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

data class HomeUiState(
    val userName: String = "Friend",
    val topSlot: HomeTopSlot? = null,
    val reconnectSummary: String? = null,
    val reconnectChips: List<ReconnectChip> = emptyList(),
    val recentMoment: RecentMoment? = null,
    val mapMoments: List<PastMoment> = emptyList(),
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val quickCatchUps: List<Pair<Contact, String>> = emptyList(),
    val isLoading: Boolean = false
)

data class ReconnectChip(
    val contactId: String,
    val name: String,
    val label: String,
    val seedColorArgb: Int?
)

data class RecentMoment(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUri: String?,
    val contactId: String?
)

sealed interface HomeTopSlot {
    data class Birthday(val event: UpcomingEvent.Birthday): HomeTopSlot

    data class MemoryFlashback(
        val title: String,
        val subtitle: String,
        val imageUri: String?,
        val actionLabel: String,
        val contactId: String?
    ): HomeTopSlot

    data class SuggestedCatchUp(val event: UpcomingEvent): HomeTopSlot

    data class RelationshipSummary(
        val title: String,
        val subtitle: String
    ): HomeTopSlot
}

class HomeViewModel(
    private val contactStore: ContactStore = AppContainer.contactStore,
    private val momentStore: MomentStore = AppContainer.momentStore
): ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        contactStore.contacts,
        momentStore.moments
    ) { contacts, moments ->
        val userName = AppContainer.authStore.currentUserFullName
            ?.split(" ")
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Friend"

        val timedEvents = EventProvider.deriveEvents(contacts, limit = 8)
        val topSlot = deriveHomeTopSlot(timedEvents, contacts, moments)

        val reconnectThisWeekCount = timedEvents.count {
            it.event is UpcomingEvent.CatchUp && it.daysAway in 0..7
        }
        val reconnectSummary = if (reconnectThisWeekCount > 0) {
            "$reconnectThisWeekCount people to reconnect with this week"
        } else {
            "No reconnects due this week"
        }

        val reconnectChips = timedEvents
            .filter { it.event is UpcomingEvent.CatchUp }
            .take(6)
            .map { timedEvent ->
                val event = timedEvent.event as UpcomingEvent.CatchUp
                val label = when {
                    timedEvent.daysAway == 0 -> "Today"
                    timedEvent.daysAway == 1 -> "Tomorrow"
                    else -> "in ${timedEvent.daysAway}d"
                }
                ReconnectChip(
                    contactId = event.contactId,
                    name = event.contactName,
                    label = label,
                    seedColorArgb = event.seedColorArgb
                )
            }

        HomeUiState(
            userName = userName,
            topSlot = topSlot,
            reconnectSummary = reconnectSummary,
            reconnectChips = reconnectChips,
            recentMoment = deriveRecentMoment(moments, contacts),
            mapMoments = moments.filter { it.locationLatitude != null && it.locationLongitude != null },
            upcomingEvents = timedEvents.map { it.event },
            quickCatchUps = contacts.map { it to "Reconnect · ${it.reconnectInterval.label}" },
            isLoading = false
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(isLoading = true)
    )

    private fun deriveHomeTopSlot(
        timedEvents: List<EventProvider.TimedEvent>,
        contacts: List<Contact>,
        moments: List<PastMoment>
    ): HomeTopSlot? {
        val birthdayWithinAWeek = timedEvents.firstOrNull {
            it.event is UpcomingEvent.Birthday && it.daysAway in 0..7
        }?.event as? UpcomingEvent.Birthday

        if (birthdayWithinAWeek != null) {
            return HomeTopSlot.Birthday(birthdayWithinAWeek)
        }

        findOnThisDayMemory(moments, contacts)?.let { return it }

        val nextEvent = timedEvents.firstOrNull()?.event
        if (nextEvent != null) {
            return HomeTopSlot.SuggestedCatchUp(nextEvent)
        }

        if (contacts.isNotEmpty()) {
            val importantCount = contacts.count { it.isImportant }
            val totalCount = contacts.size
            val subtitle = if (importantCount > 0) {
                "You’ve caught up with $importantCount of $totalCount people in your Circle this month. Nice work!"
            } else {
                "Your Circle is ready when you are. Mark someone as important to get better reminders."
            }
            return HomeTopSlot.RelationshipSummary(
                title = "Relationship health",
                subtitle = subtitle
            )
        }

        return null
    }

    private fun deriveRecentMoment(
        moments: List<PastMoment>,
        contacts: List<Contact>
    ): RecentMoment? {
        if (moments.isEmpty()) return null

        val today = LocalDate.now(ZoneId.systemDefault())
        val contactMap = contacts.associateBy { it.id }
        val latestMoment = moments.maxByOrNull { it.dateEpochMs } ?: return null

        val momentDate = Instant.ofEpochMilli(latestMoment.dateEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val daysAgo = ChronoUnit.DAYS.between(momentDate, today).toInt()
        val dateLabel = when (daysAgo) {
            0 -> "Today"
            1 -> "Yesterday"
            else -> "$daysAgo days ago"
        }

        val contactNames = latestMoment.contactIds.mapNotNull { contactMap[it]?.name }
        val title = latestMoment.title.takeIf { it.isNotBlank() }
            ?: if (contactNames.isNotEmpty()) "Memory with ${contactNames.first()}" else "Recent memory"
        val subtitle = latestMoment.description.takeIf { it.isNotBlank() }
            ?: latestMoment.locationMood.takeIf { !it.isNullOrBlank() }
            ?: "A memory from last time"

        val imageUri = latestMoment.images.firstOrNull()?.uri
            ?.let { AppContainer.photoResolver.resolveMomentPhoto(it) }
            ?: latestMoment.contactIds.firstOrNull()
                ?.let { AppContainer.photoResolver.resolveContactPhoto(it) }

        return RecentMoment(
            id = latestMoment.id,
            title = title,
            subtitle = "$dateLabel · ${subtitle}",
            imageUri = imageUri,
            contactId = latestMoment.contactIds.firstOrNull()
        )
    }

    private fun findOnThisDayMemory(
        moments: List<PastMoment>,
        contacts: List<Contact>
    ): HomeTopSlot.MemoryFlashback? {
        if (moments.isEmpty()) return null

        val today = LocalDate.now(ZoneId.systemDefault())
        val contactMap = contacts.associateBy { it.id }

        val matchingMoment = moments
            .mapNotNull { moment ->
                val momentDate = Instant.ofEpochMilli(moment.dateEpochMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val yearsAgo = Period.between(momentDate, today).years
                if (momentDate.month == today.month && momentDate.dayOfMonth == today.dayOfMonth && yearsAgo in 1..2) {
                    moment to yearsAgo
                } else {
                    null
                }
            }
            .sortedWith(compareBy({ it.second }, { -it.first.dateEpochMs }))
            .firstOrNull()?.first
            ?: return null

        val contactNames = matchingMoment.contactIds.mapNotNull { contactMap[it]?.name }
        val title = if (contactNames.isNotEmpty()) {
            "On this day with ${contactNames.first()}"
        } else {
            "On this day"
        }

        val description = matchingMoment.description.takeIf { it.isNotBlank() }
            ?: matchingMoment.title.takeIf { it.isNotBlank() }
            ?: "A memory resurfaced from this day."

        val imageUri = matchingMoment.images.firstOrNull()?.uri
            ?.let { AppContainer.photoResolver.resolveMomentPhoto(it) }
            ?: matchingMoment.contactIds.firstOrNull()
                ?.let { AppContainer.photoResolver.resolveContactPhoto(it) }

        return HomeTopSlot.MemoryFlashback(
            title = title,
            subtitle = description,
            imageUri = imageUri,
            actionLabel = "Share this memory",
            contactId = matchingMoment.contactIds.firstOrNull()
        )
    }

    fun addContact(form: ContactFormData, photoUri: String?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val contact = buildContact(form)
            try {
                contactStore.addContact(contact, photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete()
        }
    }

    fun updateContact(contact: Contact, photoUri: String?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                contactStore.updateContact(contact, photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete()
        }
    }

    private fun buildContact(form: ContactFormData): Contact {
        return Contact(
            id = UUID.randomUUID().toString(),
            name = form.name.trim(),
            phoneNumber = form.phone.trim(),
            title = form.title.trim(),
            relationship = form.relationship.trim(),
            notes = form.notes.trim(),
            reconnectInterval = form.interval,
            isImportant = true,
            birthdayYear = form.birthdayYear,
            birthdayMonth = form.birthdayMonth,
            birthdayDay = form.birthdayDay,
            seedColorArgb = form.seedColorArgb
        )
    }
}
