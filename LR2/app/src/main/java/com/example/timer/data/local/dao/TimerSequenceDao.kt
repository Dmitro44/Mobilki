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

@Dao
interface TimerSequenceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sequence: TimerSequence): Long
    
    @Update
    suspend fun update(sequence: TimerSequence)
    
    @Delete
    suspend fun delete(sequence: TimerSequence)
    
    @Query("SELECT * FROM timer_sequences WHERE id = :id")
    suspend fun getById(id: Long): TimerSequence?
    
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    fun getAllFlow(): Flow<List<TimerSequence>>
    
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    suspend fun getAll(): List<TimerSequence>
    
    @Transaction
    @Query("SELECT * FROM timer_sequences WHERE id = :id")
    suspend fun getSequenceWithPhases(id: Long): TimerSequenceWithPhases?
    
    @Transaction
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    fun getAllSequencesWithPhasesFlow(): Flow<List<TimerSequenceWithPhases>>
    
    @Transaction
    @Query("SELECT * FROM timer_sequences ORDER BY updated_at DESC")
    suspend fun getAllSequencesWithPhases(): List<TimerSequenceWithPhases>
    
    @Query("DELETE FROM timer_sequences")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM timer_sequences")
    suspend fun getCount(): Int
}
