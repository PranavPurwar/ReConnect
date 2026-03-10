package dev.pranav.reconnect.data.model

enum class MomentCategory { DINING, ART, OUTDOORS, GENERAL }

data class PastMoment(
    val id: String,
    val title: String,
    val description: String,
    val dateLabel: String,
    val category: MomentCategory,
    val imageUris: List<String> = emptyList()
)

