package com.example.timer.domain.repository

import com.example.timer.domain.model.TimerSequenceModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for timer sequence operations
 * Follows clean architecture principles by defining contracts in domain layer
 */
interface TimerRepository {
    
    /**
     * Get all timer sequences as a Flow
     * Emits whenever data changes in the database
     */
    fun getAllSequences(): Flow<List<TimerSequenceModel>>
    
    /**
     * Get a specific timer sequence by ID
     * Returns null if not found
     */
    suspend fun getSequenceById(id: Long): TimerSequenceModel?
    
    /**
     * Insert a new timer sequence
     * @return ID of the newly created sequence
     */
    suspend fun insertSequence(sequence: TimerSequenceModel): Long
    
    /**
     * Update an existing timer sequence
     * This will update both the sequence and its phases
     */
    suspend fun updateSequence(sequence: TimerSequenceModel)
    
    /**
     * Delete a timer sequence
     * Cascade deletes all associated phases
     */
    suspend fun deleteSequence(sequence: TimerSequenceModel)
    
    /**
     * Delete a timer sequence by ID
     */
    suspend fun deleteSequenceById(id: Long)
    
    /**
     * Delete all timer sequences
     */
    suspend fun deleteAllSequences()
    
    /**
     * Get count of timer sequences
     */
    suspend fun getSequenceCount(): Int
}
