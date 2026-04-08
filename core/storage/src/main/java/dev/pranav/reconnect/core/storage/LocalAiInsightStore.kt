package dev.pranav.reconnect.core.storage

class LocalAiInsightStore : AiInsightStore {
    override fun getPrepBullets(contactId: String, fallback: List<String>): List<String> = fallback
}


