package dev.pranav.reconnect.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.EventProvider
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.model.UpcomingEvent
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.ReminderFrequency
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sessionStore = AppSessionStore(context)
        if (!sessionStore.isNotificationsEnabled()) {
            return Result.success()
        }

        NotificationHelper.createNotificationChannel(context)

        AppContainer.init(context)
        val contactStore = AppContainer.contactStore
        val contacts = contactStore.contacts.first()

        val frequency = sessionStore.getReminderFrequency()
        val includeDays = when (frequency) {
            ReminderFrequency.ON_DAY -> 0..0
            ReminderFrequency.DAY_BEFORE_AND_ON_DAY -> 0..1
            ReminderFrequency.WEEK_AND_DAY_BEFORE_AND_ON -> 0..7
        }

        val allEvents =
            EventProvider.deriveEvents(
                contacts,
                limit = Int.MAX_VALUE,
                includeFutureDays = includeDays.last
            )

        // Filter events for the configured reminder window.
        val relevantEvents = allEvents.filter { it.daysAway in includeDays }

        relevantEvents.forEach { timedEvent ->
            val days = timedEvent.daysAway
            val timeText = when (days) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> "In $days days"
            }

            when (val event = timedEvent.event) {
                is UpcomingEvent.Birthday -> {
                    if (sessionStore.isNotifyBirthdaysEnabled()) {
                        NotificationHelper.postNotification(
                            context = context,
                            notificationId = event.contactId.hashCode() + 1000 + days,
                            title = "Birthday Reminder: ${event.contactName}",
                            content = "${event.contactName}'s birthday is $timeText!",
                            contactId = event.contactId
                        )
                    }
                }

                is UpcomingEvent.CatchUp -> {
                    if (sessionStore.isNotifyCatchUpsEnabled()) {
                        NotificationHelper.postNotification(
                            context = context,
                            notificationId = event.contactId.hashCode() + 2000 + days,
                            title = "Catch Up: ${event.contactName}",
                            content = "It's time to reconnect with ${event.contactName} ($timeText).",
                            contactId = event.contactId
                        )
                    }
                }

                is UpcomingEvent.TimelineReminder -> {
                    // Ignored for daily push
                }
            }
        }

        if (sessionStore.isNotifyMemoriesEnabled()) {
            val moments = AppContainer.momentStore.moments.first()
            val memoryNotifications = deriveMemoryResurfacingNotifications(contacts, moments)
            memoryNotifications.forEach { notification ->
                NotificationHelper.postNotification(
                    context = context,
                    notificationId = notification.notificationId,
                    title = notification.title,
                    content = notification.content,
                    contactId = notification.contactId
                )
            }
        }

        return Result.success()
    }
}

private data class MemoryNotification(
    val notificationId: Int,
    val title: String,
    val content: String,
    val contactId: String? = null
)

private fun deriveMemoryResurfacingNotifications(
    contacts: List<Contact>,
    moments: List<PastMoment>
): List<MemoryNotification> {
    if (moments.isEmpty()) return emptyList()

    val contactMap = contacts.associateBy { it.id }
    val today = LocalDate.now(ZoneId.systemDefault())

    val anniversaryNotifications = moments.mapNotNull { moment ->
        val momentDate = Instant.ofEpochMilli(moment.dateEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        if (momentDate.month == today.month && momentDate.dayOfMonth == today.dayOfMonth && momentDate.year < today.year) {
            val yearsAgo = Period.between(momentDate, today).years
            val contactNames = moment.contactIds.mapNotNull { contactMap[it]?.name }
            val personText = when {
                contactNames.isEmpty() -> ""
                contactNames.size == 1 -> contactNames.first()
                else -> contactNames.take(2)
                    .joinToString(", ") + if (contactNames.size > 2) "…" else ""
            }

            val content = when {
                personText.isNotBlank() ->
                    if (yearsAgo == 1) "1 year ago today you were with $personText."
                    else "$yearsAgo years ago today you were with $personText."

                !moment.locationMood.isNullOrBlank() ->
                    if (yearsAgo == 1) "1 year ago today you visited ${moment.locationMood}."
                    else "$yearsAgo years ago today you visited ${moment.locationMood}."

                moment.title.isNotBlank() ->
                    if (yearsAgo == 1) "1 year ago today: ${moment.title}"
                    else "$yearsAgo years ago today: ${moment.title}"

                else ->
                    "A memory resurfaced from $yearsAgo years ago."
            }

            MemoryNotification(
                notificationId = moment.id.hashCode() + 3000 + yearsAgo,
                title = "Memory Resurfacing",
                content = content,
                contactId = moment.contactIds.firstOrNull()
            )
        } else {
            null
        }
    }

    if (anniversaryNotifications.isNotEmpty()) {
        return listOf(anniversaryNotifications.first())
    }

    val latestMoment = moments.maxByOrNull { it.dateEpochMs } ?: return emptyList()
    val momentDate = Instant.ofEpochMilli(latestMoment.dateEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val daysAgo = ChronoUnit.DAYS.between(momentDate, today)
    if (daysAgo < 30) return emptyList()

    val contactNames = latestMoment.contactIds.mapNotNull { contactMap[it]?.name }
    val personText = if (contactNames.isNotEmpty()) contactNames.first() else null
    val descriptor = latestMoment.locationMood?.takeIf { it.isNotBlank() }
        ?: latestMoment.title.takeIf { it.isNotBlank() }
        ?: "this place"

    val content = when {
        personText != null && daysAgo == 1L ->
            "Last time you connected with $personText was yesterday."

        personText != null ->
            "Last time you connected with $personText was $daysAgo days ago."

        daysAgo == 1L ->
            "Last time you visited $descriptor was yesterday."

        else ->
            "Last time you visited $descriptor was $daysAgo days ago."
    }

    return listOf(
        MemoryNotification(
            notificationId = latestMoment.id.hashCode() + 3000,
            title = "Memory Resurfacing",
            content = content,
            contactId = latestMoment.contactIds.firstOrNull()
        )
    )
}

