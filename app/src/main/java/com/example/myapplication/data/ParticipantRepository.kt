package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class ParticipantRepository(private val participantDao: ParticipantDao) {
    suspend fun insertParticipant(participant: Participant) {
        participantDao.insertParticipant(participant)
    }
    
    suspend fun getParticipantById(userId: Int): Participant? {
        return participantDao.getParticipantById(userId)
    }
    
    fun getParticipantByIdFlow(userId: Int): Flow<Participant?> {
        return participantDao.getParticipantByIdFlow(userId)
    }
    
    suspend fun checkUserIdExists(userId: Int): Boolean {
        return participantDao.countByUserId(userId) > 0
    }
}
