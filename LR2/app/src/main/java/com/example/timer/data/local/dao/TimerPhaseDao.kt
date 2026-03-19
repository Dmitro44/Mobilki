package com.example.timer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.timer.data.local.entity.TimerPhase
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TimerPhase operations
 */
@Dao
interface TimerPhaseDao {
    
    /**
     * Insert a new timer phase
     * @return ID of the inserted phase
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phase: TimerPhase): Long
    
    /**
     * Insert multiple timer phases
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phases: List<TimerPhase>)
    
    /**
     * Update an existing timer phase
     */
    @Update
    suspend fun update(phase: TimerPhase)
    
    /**
     * Delete a timer phase
     */
    @Delete
    suspend fun delete(phase: TimerPhase)
    
    /**
     * Get a timer phase by ID
     */
    @Query("SELECT * FROM timer_phases WHERE id = :id")
    suspend fun getById(id: Long): TimerPhase?
    
    /**
     * Get all phases for a specific sequence
     */
    @Query("SELECT * FROM timer_phases WHERE sequence_id = :sequenceId ORDER BY order_index ASC")
    suspend fun getBySequenceId(sequenceId: Long): List<TimerPhase>
    
    /**
     * Get all phases for a specific sequence as Flow
     */
    @Query("SELECT * FROM timer_phases WHERE sequence_id = :sequenceId ORDER BY order_index ASC")
    fun getBySequenceIdFlow(sequenceId: Long): Flow<List<TimerPhase>>
    
    /**
     * Delete all phases for a specific sequence
     */
    @Query("DELETE FROM timer_phases WHERE sequence_id = :sequenceId")
    suspend fun deleteBySequenceId(sequenceId: Long)
    
    /**
     * Delete all timer phases
     */
    @Query("DELETE FROM timer_phases")
    suspend fun deleteAll()
    
    /**
     * Get count of phases for a specific sequence
     */
    @Query("SELECT COUNT(*) FROM timer_phases WHERE sequence_id = :sequenceId")
    suspend fun getCountBySequenceId(sequenceId: Long): Int
}
