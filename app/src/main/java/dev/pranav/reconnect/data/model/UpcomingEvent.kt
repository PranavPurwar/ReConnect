package dev.pranav.reconnect.data.model

sealed class UpcomingEvent {
    abstract val contactName: String
    abstract val contactId: String

    data class Birthday(
        override val contactName: String,
        override val contactId: String,
        val age: Int,
        val day: Int,
        val month: String,
        val note: String
    ) : UpcomingEvent()

    data class CatchUp(
        override val contactName: String,
        override val contactId: String,
        val day: Int,
        val dayOfWeek: String,
        val seedColorArgb: Int?
    ) : UpcomingEvent()

    data class TimelineReminder(
        override val contactName: String,
        override val contactId: String,
        val duration: String,
        val actionLabel: String
    ) : UpcomingEvent()
}

