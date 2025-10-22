package com.example.sms2tg

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LogEntity)

    @Query("DELETE FROM logs")
    suspend fun clearAll()

    // Потоковое наблюдение (Flow) — обновления в реальном времени
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeLast(limit: Int = 100): Flow<List<LogEntity>>

    // Разовый запрос без Flow
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLast(limit: Int = 100): List<LogEntity>
}
