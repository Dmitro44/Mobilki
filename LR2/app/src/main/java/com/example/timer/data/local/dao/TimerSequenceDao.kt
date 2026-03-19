package com.example.timer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.timer.data.local.entity.TimerSequence
import com.example.timer.data.local.relation.TimerSequenceWithPhases
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TimerSequence operations
 */
@Dao
interface TimerSequenceDao {
    
    /**
     * Insert a new timer sequence
     * @return ID of the inserted sequence
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sequence: TimerSequence): Long
    
    /**
     * Update an existing timer sequence
     */
    @Update
    suspend fun update(sequence: TimerSequence)
    
    /**
     * Delete a timer sequence (cascade deletes associated phases)
     */
    @Delete
    suspend fun delete(sequence: TimerSequence)
    
    /**
     * Get a timer sequence by ID
     */
    @Query("SELECT * FROM timer_sequences WHERE id = :id")
    suspend fun getById(id: Long): TimerSequence?
    
    /**
     * Get all timer sequences, ordered by update time (most recent first)
     */
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    fun getAllFlow(): Flow<List<TimerSequence>>
    
    /**
     * Get all timer sequences as a list
     */
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    suspend fun getAll(): List<TimerSequence>
    
    /**
     * Get a timer sequence with all its phases
     */
    @Transaction
    @Query("SELECT * FROM timer_sequences WHERE id = :id")
    suspend fun getSequenceWithPhases(id: Long): TimerSequenceWithPhases?
    
    /**
     * Get all timer sequences with their phases
     */
    @Transaction
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    fun getAllSequencesWithPhasesFlow(): Flow<List<TimerSequenceWithPhases>>
    
    /**
     * Get all timer sequences with their phases as a list
     */
    @Transaction
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    suspend fun getAllSequencesWithPhases(): List<TimerSequenceWithPhases>
    
    /**
     * Delete all timer sequences
     */
    @Query("DELETE FROM timer_sequences")
    suspend fun deleteAll()
    
    /**
     * Get count of timer sequences
     */
    @Query("SELECT COUNT(*) FROM timer_sequences")
    suspend fun getCount(): Int
}
