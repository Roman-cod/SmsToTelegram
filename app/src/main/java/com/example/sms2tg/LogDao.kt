package com.example.sms2tg

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    // Стримим последние N записей (используется в MainActivity.observeLast(100))
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeLast(limit: Int): Flow<List<LogEntity>>

    // Получить последние N записей единовременно (если понадобится)
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLast(limit: Int): List<LogEntity>

    // Весь лог (не обязательно, но оставим — может пригодиться)
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<LogEntity>

    @Insert
    suspend fun insert(log: LogEntity)

    @Query("DELETE FROM logs")
    suspend fun clearAll()
}
