package com.example.sms2tg

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT 100")
    fun getLast100(): List<LogEntity>

    @Query("DELETE FROM logs")
    fun clearAll()

    @Insert
    fun insert(log: LogEntity)
}
