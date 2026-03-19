package com.example.timer.data.repository

import com.example.timer.data.local.dao.TimerPhaseDao
import com.example.timer.data.local.dao.TimerSequenceDao
import com.example.timer.data.mapper.toEntity
import com.example.timer.data.mapper.toModel
import com.example.timer.data.mapper.toModelList
import com.example.timer.data.mapper.toPhaseEntityList
import com.example.timer.domain.model.TimerSequenceModel
import com.example.timer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of TimerRepository
 * Handles data operations for timer sequences
 */
class TimerRepositoryImpl(
    private val sequenceDao: TimerSequenceDao,
    private val phaseDao: TimerPhaseDao
) : TimerRepository {
    
    override fun getAllSequences(): Flow<List<TimerSequenceModel>> {
        return sequenceDao.getAllSequencesWithPhasesFlow().map { sequencesWithPhases ->
            sequencesWithPhases.toModelList()
        }
    }
    
    override suspend fun getSequenceById(id: Long): TimerSequenceModel? {
        return sequenceDao.getSequenceWithPhases(id)?.toModel()
    }
    
    override suspend fun insertSequence(sequence: TimerSequenceModel): Long {
        // Insert the sequence first
        val sequenceId = sequenceDao.insert(sequence.toEntity())
        
        // Then insert all phases with the correct sequenceId
        if (sequence.phases.isNotEmpty()) {
            val phasesWithSequenceId = sequence.phases.map { phase ->
                phase.copy(sequenceId = sequenceId).toEntity()
            }
            phaseDao.insertAll(phasesWithSequenceId)
        }
        
        return sequenceId
    }
    
    override suspend fun updateSequence(sequence: TimerSequenceModel) {
        // Update the sequence
        val updatedSequence = sequence.toEntity().copy(
            updatedAt = System.currentTimeMillis()
        )
        sequenceDao.update(updatedSequence)
        
        // Delete existing phases and insert new ones
        phaseDao.deleteBySequenceId(sequence.id)
        if (sequence.phases.isNotEmpty()) {
            val phases = sequence.phases.map { it.toEntity() }
            phaseDao.insertAll(phases)
        }
    }
    
    override suspend fun deleteSequence(sequence: TimerSequenceModel) {
        sequenceDao.delete(sequence.toEntity())
        // Phases are cascade deleted via foreign key constraint
    }
    
    override suspend fun deleteSequenceById(id: Long) {
        val sequence = sequenceDao.getById(id)
        if (sequence != null) {
            sequenceDao.delete(sequence)
        }
    }
    
    override suspend fun deleteAllSequences() {
        sequenceDao.deleteAll()
        // Phases are cascade deleted via foreign key constraint
    }
    
    override suspend fun getSequenceCount(): Int {
        return sequenceDao.getCount()
    }
}
