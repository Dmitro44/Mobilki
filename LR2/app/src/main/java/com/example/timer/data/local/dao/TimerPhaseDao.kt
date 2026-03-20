package com.example.timer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.timer.data.local.entity.TimerPhase
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPhaseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phase: TimerPhase): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phases: List<TimerPhase>)
    
    @Update
    suspend fun update(phase: TimerPhase)
    
    @Delete
    suspend fun delete(phase: TimerPhase)
    
    @Query("SELECT * FROM timer_phases WHERE id = :id")
    suspend fun getById(id: Long): TimerPhase?
    
    @Query("SELECT * FROM timer_phases WHERE sequence_id = :sequenceId ORDER BY order_index ASC")
    suspend fun getBySequenceId(sequenceId: Long): List<TimerPhase>
    
    @Query("SELECT * FROM timer_phases WHERE sequence_id = :sequenceId ORDER BY order_index ASC")
    fun getBySequenceIdFlow(sequenceId: Long): Flow<List<TimerPhase>>
    
    @Query("DELETE FROM timer_phases WHERE sequence_id = :sequenceId")
    suspend fun deleteBySequenceId(sequenceId: Long)
    
    @Query("DELETE FROM timer_phases")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM timer_phases WHERE sequence_id = :sequenceId")
    suspend fun getCountBySequenceId(sequenceId: Long): Int
}
