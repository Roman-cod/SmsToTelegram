package com.example.sms2tg

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BlockedSenderDao {
    @Query("SELECT * FROM blocked_senders ORDER BY id DESC")
    suspend fun getAll(): List<BlockedSender>

    @Insert
    suspend fun insert(item: BlockedSender): Long

    @Delete
    suspend fun delete(item: BlockedSender)

    @Query("DELETE FROM blocked_senders WHERE id = :id")
    suspend fun deleteById(id: Long)
}