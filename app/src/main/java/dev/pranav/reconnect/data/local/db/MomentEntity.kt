package dev.pranav.reconnect.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pranav.reconnect.data.model.MomentCategory

@Entity(tableName = "moments")
data class MomentEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val title: String,
    val description: String,
    val dateLabel: String,
    val category: MomentCategory,
    val imageUris: List<String>,
    val createdAtEpochMs: Long
)

