package dev.pranav.reconnect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: String,
    val name: String,
    val title: String = "",
    val relationship: String = "",
    val notes: String = "",
    @SerialName("seed_color_argb") val seedColorArgb: Int? = null,
    @SerialName("phone_number") val phoneNumber: String = "",
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("is_important") val isImportant: Boolean = false,
    @SerialName("reconnect_interval") val reconnectInterval: ReconnectInterval = ReconnectInterval.MONTHLY,
    @SerialName("birthday_month") val birthdayMonth: Int? = null,
    @SerialName("birthday_day") val birthdayDay: Int? = null,
    @SerialName("birthday_year") val birthdayYear: Int? = null,

    @kotlinx.serialization.Transient
    val photoUri: String? = null
)
