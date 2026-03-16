package dev.pranav.reconnect.data.local

import dev.pranav.reconnect.data.port.AiInsightStore

class LocalAiInsightStore : AiInsightStore {
    override fun getPrepBullets(contactId: String, fallback: List<String>): List<String> = fallback
}


