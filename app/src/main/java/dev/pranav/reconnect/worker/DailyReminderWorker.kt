package dev.pranav.reconnect.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.pranav.reconnect.core.model.EventProvider
import dev.pranav.reconnect.core.model.UpcomingEvent
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.di.AppContainer
import kotlinx.coroutines.flow.first

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

        val contactStore = AppContainer.contactStore
        val contacts = contactStore.contacts.first()
        val allEvents =
            EventProvider.deriveEvents(contacts, limit = Int.MAX_VALUE, includeFutureDays = 3)

        // Filter events for today and up to 3 days out
        val relevantEvents = allEvents.filter { it.daysAway in 0..3 }

        relevantEvents.forEachIndexed { index, timedEvent ->
            val days = timedEvent.daysAway
            val timeText = when (days) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> "In $days days"
            }

            when (val event = timedEvent.event) {
                is UpcomingEvent.Birthday -> {
                    NotificationHelper.postNotification(
                        context = context,
                        notificationId = event.contactId.hashCode() + 1000 + days,
                        title = "Birthday Reminder: ${event.contactName}",
                        content = "${event.contactName}'s birthday is $timeText!",
                        contactId = event.contactId
                    )
                }

                is UpcomingEvent.CatchUp -> {
                    NotificationHelper.postNotification(
                        context = context,
                        notificationId = event.contactId.hashCode() + 2000 + days,
                        title = "Catch Up: ${event.contactName}",
                        content = "It's time to reconnect with ${event.contactName} ($timeText).",
                        contactId = event.contactId
                    )
                }

                is UpcomingEvent.TimelineReminder -> {
                    // Ignored for daily push
                }
            }
        }

        return Result.success()
    }
}

