package dev.pranav.reconnect.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MomentDao {
    @Query("SELECT * FROM moments ORDER BY createdAtEpochMs DESC")
    fun observeMoments(): Flow<List<MomentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: MomentEntity)

    @Query("SELECT * FROM moments WHERE contactId = :contactId ORDER BY createdAtEpochMs DESC")
    suspend fun getMomentsFor(contactId: String): List<MomentEntity>

    @Query("DELETE FROM moments WHERE contactId = :contactId")
    suspend fun deleteMomentsForContact(contactId: String)
}

