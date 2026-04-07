package dev.pranav.reconnect.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ContactEntity::class, MomentEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class ReConnectDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun momentDao(): MomentDao

    companion object {
        @Volatile
        private var INSTANCE: ReConnectDatabase? = null

        fun getInstance(context: Context): ReConnectDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    ReConnectDatabase::class.java,
                    "reconnect.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}

