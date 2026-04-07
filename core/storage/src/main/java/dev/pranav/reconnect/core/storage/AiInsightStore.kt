package dev.pranav.reconnect.core.storage

interface AiInsightStore {
    fun getPrepBullets(contactId: String, fallback: List<String>): List<String>
}


