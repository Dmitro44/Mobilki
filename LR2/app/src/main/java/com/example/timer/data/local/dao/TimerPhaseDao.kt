package com.example.timer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timer.data.local.entity.TimerPhase

@Dao
interface TimerPhaseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phases: List<TimerPhase>)
    
    @Query("DELETE FROM timer_phases WHERE sequence_id = :sequenceId")
    suspend fun deleteBySequenceId(sequenceId: Long)
    
    @Query("DELETE FROM timer_phases")
    suspend fun deleteAll()
}
