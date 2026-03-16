package dev.pranav.reconnect.data.port

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StorageOperationType {
    READ,
    WRITE
}

data class StorageMetric(
    val operation: StorageOperationType,
    val name: String,
    val durationMs: Long,
    val itemCount: Int = 0,
    val succeeded: Boolean = true,
    val timestampMs: Long = System.currentTimeMillis()
)

interface StorageMetricsRecorder {
    val metrics: StateFlow<List<StorageMetric>>

    fun record(metric: StorageMetric)
}

class InMemoryStorageMetricsRecorder(
    private val maxEntries: Int = 250
) : StorageMetricsRecorder {
    private val _metrics = MutableStateFlow<List<StorageMetric>>(emptyList())
    override val metrics: StateFlow<List<StorageMetric>> = _metrics.asStateFlow()

    override fun record(metric: StorageMetric) {
        _metrics.value = (_metrics.value + metric).takeLast(maxEntries)
    }
}

inline fun <T> StorageMetricsRecorder.trackRead(
    name: String,
    itemCount: Int = 0,
    block: () -> T
): T {
    val start = SystemClock.elapsedRealtimeNanos()
    return try {
        block().also {
            val durationMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000
            record(
                StorageMetric(
                    operation = StorageOperationType.READ,
                    name = name,
                    durationMs = durationMs,
                    itemCount = itemCount,
                    succeeded = true
                )
            )
        }
    } catch (error: Throwable) {
        val durationMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000
        record(
            StorageMetric(
                operation = StorageOperationType.READ,
                name = name,
                durationMs = durationMs,
                itemCount = itemCount,
                succeeded = false
            )
        )
        throw error
    }
}

suspend inline fun <T> StorageMetricsRecorder.trackWrite(
    name: String,
    block: () -> T
): T {
    val start = SystemClock.elapsedRealtimeNanos()
    return try {
        block().also {
            val durationMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000
            record(
                StorageMetric(
                    operation = StorageOperationType.WRITE,
                    name = name,
                    durationMs = durationMs,
                    succeeded = true
                )
            )
        }
    } catch (error: Throwable) {
        val durationMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000
        record(
            StorageMetric(
                operation = StorageOperationType.WRITE,
                name = name,
                durationMs = durationMs,
                succeeded = false
            )
        )
        throw error
    }
}

