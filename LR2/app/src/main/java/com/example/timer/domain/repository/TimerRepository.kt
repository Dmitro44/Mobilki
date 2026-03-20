package com.example.timer.domain.repository

import com.example.timer.domain.model.TimerSequenceModel
import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    
    fun getAllSequences(): Flow<List<TimerSequenceModel>>
    
    suspend fun getSequenceById(id: Long): TimerSequenceModel?
    
    suspend fun insertSequence(sequence: TimerSequenceModel): Long
    
    suspend fun updateSequence(sequence: TimerSequenceModel)
    
    suspend fun deleteSequence(sequence: TimerSequenceModel)
    
    suspend fun deleteSequenceById(id: Long)
    
    suspend fun deleteAllSequences()
    
    suspend fun getSequenceCount(): Int
}
