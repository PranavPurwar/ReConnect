package dev.pranav.reconnect.core.model

data class ContactFormData(
    val name: String,
    val phone: String = "",
    val title: String = "",
    val relationship: String = "",
    val notes: String = "",
    val interval: ReconnectInterval = ReconnectInterval.MONTHLY,
    val birthdayMonth: Int? = null,
    val birthdayDay: Int? = null,
    val birthdayYear: Int? = null,
    val seedColorArgb: Int? = null
)
