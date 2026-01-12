package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: Participant)
    
    @Query("SELECT * FROM participants WHERE userId = :userId")
    suspend fun getParticipantById(userId: Int): Participant?
    
    @Query("SELECT * FROM participants WHERE userId = :userId")
    fun getParticipantByIdFlow(userId: Int): Flow<Participant?>
    
    @Query("SELECT COUNT(*) FROM participants WHERE userId = :userId")
    suspend fun countByUserId(userId: Int): Int
}
