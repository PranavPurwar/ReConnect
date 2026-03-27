package dev.pranav.reconnect.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pranav.reconnect.data.model.MomentCategory

@Entity(tableName = "moments")
data class MomentEntity(
    @PrimaryKey val id: String,
    val contactIds: List<String>,
    val title: String,
    val description: String,
    val dateEpochMs: Long,
    val category: MomentCategory,
    val imageUris: List<String>,
    val createdAtEpochMs: Long
)
