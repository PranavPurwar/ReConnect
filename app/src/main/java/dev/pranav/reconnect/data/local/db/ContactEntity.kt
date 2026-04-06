package dev.pranav.reconnect.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pranav.reconnect.data.model.ReconnectInterval

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val relationship: String,
    val notes: String,
    val seedColorArgb: Int?,
    val phoneNumber: String,
    val isActive: Boolean,
    val isImportant: Boolean,
    val reconnectInterval: ReconnectInterval,
    val birthdayYear: Int?,
    val birthdayMonth: Int?,
    val birthdayDay: Int?
)

