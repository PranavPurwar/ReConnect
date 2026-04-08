package dev.pranav.reconnect.data.local.db

import androidx.room.TypeConverter
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.MomentImage
import dev.pranav.reconnect.core.model.ReconnectInterval
import kotlinx.serialization.json.Json

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
    fun stringListToJson(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun stringListFromJson(value: String): List<String> {
        return runCatching { Json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun imagesToJson(value: List<MomentImage>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun imagesFromJson(value: String): List<MomentImage> {
        return runCatching { Json.decodeFromString<List<MomentImage>>(value) }.getOrDefault(emptyList())
    }
}
