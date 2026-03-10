package dev.pranav.reconnect.data.model

enum class ReconnectInterval(val label: String, val days: Int) {
    WEEKLY("Weekly", 7),
    BIWEEKLY("Biweekly", 14),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}

