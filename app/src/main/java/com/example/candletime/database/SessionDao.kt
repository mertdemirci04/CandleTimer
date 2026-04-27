package com.example.candletime.database

import androidx.room.*
import com.example.candletime.models.CandleSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: CandleSession)

    @Delete
    suspend fun delete(session: CandleSession)

    @Query("SELECT * FROM candle_sessions ORDER BY startedAt DESC")
    fun getAll(): Flow<List<CandleSession>>


    @Query("SELECT SUM(actualSeconds) FROM candle_sessions")
    fun getTotalBurned(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM candle_sessions WHERE completed = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM candle_sessions")
    fun getTotalCount(): Flow<Int>

    @Query("DELETE FROM candle_sessions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}