package com.example.sms2tg

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface PendingDao {
    @Insert
    fun insert(item: PendingMessage): Long

    @Query("SELECT * FROM pending_messages ORDER BY timestamp ASC")
    fun getAll(): List<PendingMessage>

    @Query("DELETE FROM pending_messages WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM pending_messages")
    fun clearAll()
}
