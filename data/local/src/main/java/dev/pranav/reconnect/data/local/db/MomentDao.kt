package dev.pranav.reconnect.data.local.db

import androidx.room.* // using wildcard for Update, Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface MomentDao {
    @Query("SELECT * FROM moments ORDER BY createdAtEpochMs DESC")
    fun observeMoments(): Flow<List<MomentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: MomentEntity)

    @Update
    suspend fun updateMoment(moment: MomentEntity)

    @Delete
    suspend fun deleteMoment(moment: MomentEntity)

    @Query("SELECT * FROM moments ORDER BY createdAtEpochMs DESC")
    suspend fun getAllMoments(): List<MomentEntity>
}
