package dev.pranav.reconnect.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MomentCategory { DINING, ART, OUTDOORS, GENERAL }

@Serializable
data class MomentImage(
    val id: String,
    val uri: String,
    val caption: String? = null
)

@Serializable
data class PastMoment(
    val id: String,
    @SerialName("contact_ids") val contactIds: List<String> = emptyList(),
    val title: String,
    val description: String,
    @SerialName("date_epoch_ms") val dateEpochMs: Long,
    val category: MomentCategory,
    val images: List<MomentImage> = emptyList(),
    @SerialName("is_core_memory") val isCoreMemory: Boolean = false,
    @SerialName("was_present") val wasPresent: Boolean = true,
    @SerialName("group_name") val groupName: String? = null,
    @SerialName("location_mood") val locationMood: String? = null,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)
