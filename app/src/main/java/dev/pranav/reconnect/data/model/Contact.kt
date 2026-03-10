package dev.pranav.reconnect.data.model

data class Contact(
    val id: String,
    val name: String,
    val title: String = "",
    val relationship: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val phoneNumber: String = "",
    val isActive: Boolean = false,
    val isImportant: Boolean = false,
    val reconnectInterval: ReconnectInterval = ReconnectInterval.MONTHLY
)

