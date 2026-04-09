package dev.pranav.reconnect.core.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object EventProvider {
    data class TimedEvent(
        val event: UpcomingEvent,
        val timeInMillis: Long,
        val daysAway: Int
    )

    fun deriveEvents(
        contacts: List<Contact>,
        limit: Int = 5,
        includeFutureDays: Int = 3
    ): List<TimedEvent> {
        if (contacts.isEmpty()) return emptyList()
        val now = Calendar.getInstance()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dowFmt = SimpleDateFormat("EEEE", Locale.getDefault())
        val monthFmt = SimpleDateFormat("MMMM", Locale.getDefault())
        val timedEvents = mutableListOf<TimedEvent>()

        contacts
            .filter { it.birthdayMonth != null && it.birthdayDay != null }
            .forEach { contact ->
                val bCal = Calendar.getInstance().apply {
                    set(Calendar.MONTH, contact.birthdayMonth!! - 1)
                    set(Calendar.DAY_OF_MONTH, contact.birthdayDay!!)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    // If birthday has passed this year, look at next year
                    if (before(todayStart)) add(Calendar.YEAR, 1)
                }

                val diffMillis = bCal.timeInMillis - todayStart.timeInMillis
                val daysAway = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                val monthName = monthFmt.format(bCal.time).uppercase()
                timedEvents.add(
                    TimedEvent(
                        event = UpcomingEvent.Birthday(
                            contactName = contact.name,
                            contactId = contact.id,
                            age = 0,
                            day = contact.birthdayDay!!,
                            month = monthName,
                            note = ""
                        ),
                        timeInMillis = bCal.timeInMillis,
                        daysAway = daysAway
                    )
                )
            }

        contacts.filter { it.isImportant }.forEach { contact ->
            val cal = now.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, contact.reconnectInterval.days)

            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val diffMillis = cal.timeInMillis - todayStart.timeInMillis
            val daysAway = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            timedEvents.add(
                TimedEvent(
                    event = UpcomingEvent.CatchUp(
                        contactName = contact.name.split(" ").first(),
                        contactId = contact.id,
                        day = cal.get(Calendar.DAY_OF_MONTH),
                        dayOfWeek = dowFmt.format(cal.time).uppercase(),
                        seedColorArgb = contact.seedColorArgb
                    ),
                    timeInMillis = cal.timeInMillis,
                    daysAway = daysAway
                )
            )
        }

        return timedEvents
            .sortedBy { it.timeInMillis }
            .take(limit)
    }
}

