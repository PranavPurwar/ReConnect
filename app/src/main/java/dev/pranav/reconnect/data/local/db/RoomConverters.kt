package dev.pranav.reconnect.data.local.db

import androidx.room.TypeConverter
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.ReconnectInterval
import org.json.JSONArray

class RoomConverters {
    @TypeConverter
    fun reconnectIntervalToString(value: ReconnectInterval): String = value.name

    @TypeConverter
    fun reconnectIntervalFromString(value: String): ReconnectInterval =
        runCatching { ReconnectInterval.valueOf(value) }.getOrDefault(ReconnectInterval.MONTHLY)

    @TypeConverter
    fun momentCategoryToString(value: MomentCategory): String = value.name

    @TypeConverter
    fun momentCategoryFromString(value: String): MomentCategory =
        runCatching { MomentCategory.valueOf(value) }.getOrDefault(MomentCategory.GENERAL)

    @TypeConverter
    fun imageUrisToJson(value: List<String>): String {
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun imageUrisFromJson(value: String): List<String> {
        val array = runCatching { JSONArray(value) }.getOrNull() ?: return emptyList()
        return (0 until array.length()).map { index -> array.optString(index) }
    }
}

