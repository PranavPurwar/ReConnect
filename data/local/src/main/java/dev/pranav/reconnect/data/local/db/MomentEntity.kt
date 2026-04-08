package dev.pranav.reconnect.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.MomentImage

@Entity(tableName = "moments")
data class MomentEntity(
    @PrimaryKey val id: String,
    val contactIds: List<String>,
    val title: String,
    val description: String,
    val dateEpochMs: Long,
    val category: MomentCategory,
    val images: List<MomentImage>,
    val isCoreMemory: Boolean,
    val wasPresent: Boolean,
    val groupName: String?,
    val locationMood: String?,
    val createdAtEpochMs: Long
)
