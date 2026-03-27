package dev.pranav.reconnect.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MomentCategory { DINING, ART, OUTDOORS, GENERAL }

@Serializable
data class PastMoment(
    val id: String,
    @SerialName("contact_ids") val contactIds: List<String> = emptyList(),
    val title: String,
    val description: String,
    @SerialName("date_epoch_ms") val dateEpochMs: Long,
    val category: MomentCategory,
    @SerialName("image_uris") val imageUris: List<String> = emptyList(),
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)
