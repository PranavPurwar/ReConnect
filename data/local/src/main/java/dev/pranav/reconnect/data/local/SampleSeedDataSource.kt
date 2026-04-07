package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.model.ReconnectInterval

class SampleSeedDataSource {
    fun getSampleContacts(): List<Contact> = listOf(
        Contact(
            id = "1",
            name = "Eleanor Vance",
            title = "Creative Director",
            relationship = "Close Friend",
            isActive = true,
            isImportant = true,
            reconnectInterval = ReconnectInterval.WEEKLY
        ),
        Contact(
            id = "2",
            name = "Sarah Jenkins",
            isImportant = true,
            reconnectInterval = ReconnectInterval.MONTHLY
        ),
        Contact(
            id = "3",
            name = "Mark Thompson",
            isImportant = true,
            reconnectInterval = ReconnectInterval.BIWEEKLY
        ),
        Contact(
            id = "4",
            name = "David Chen",
            isImportant = true,
            reconnectInterval = ReconnectInterval.MONTHLY
        ),
        Contact(
            id = "5",
            name = "Elena Rodriguez",
            isImportant = true,
            reconnectInterval = ReconnectInterval.MONTHLY
        ),
        Contact(
            id = "6",
            name = "Mom",
            isImportant = true,
            reconnectInterval = ReconnectInterval.WEEKLY
        )
    )

    fun getSamplePastMoments(): List<PastMoment> = listOf(
        PastMoment(
            id = "1",
            title = "Dinner at Rosso's",
            description = "Celebrated her promotion. Discussed the new project timeline and shared some laughs over dessert.",
            dateEpochMs = System.currentTimeMillis() - 7 * 86_400_000L,
            category = MomentCategory.DINING
        ),
        PastMoment(
            id = "2",
            title = "Gallery Opening",
            description = "Checked out the local art scene. Eleanor was really inspired by the textile works.",
            dateEpochMs = 1697068800000L, // OCT 12, 2023
            category = MomentCategory.ART,
            imageUris = listOf("gallery1", "gallery2")
        ),
        PastMoment(
            id = "3",
            title = "Park Morning Walk",
            description = "Quick catch-up during her morning jog. Planned for the upcoming holiday trip.",
            dateEpochMs = 1695513600000L, // SEPT 24, 2023
            category = MomentCategory.OUTDOORS
        )
    )
}
