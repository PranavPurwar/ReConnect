package dev.pranav.reconnect.data.port

interface AiInsightStore {
    fun getPrepBullets(contactId: String, fallback: List<String>): List<String>
}


