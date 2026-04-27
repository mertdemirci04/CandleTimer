package com.example.candletime.database


import com.example.candletime.models.CandleSession
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val dao: SessionDao
) {

    val allSessions: Flow<List<CandleSession>> = dao.getAll()

    val totalBurned: Flow<Long?> = dao.getTotalBurned()

    val completedCount: Flow<Int> = dao.getCompletedCount()

    val totalCount: Flow<Int> = dao.getTotalCount()

    suspend fun save(session: CandleSession) {
        dao.insert(session)
    }

    suspend fun delete(session: CandleSession) {
        dao.delete(session)
    }

    suspend fun deleteMultiple(ids: List<Long>) {
        dao.deleteByIds(ids)
    }
}