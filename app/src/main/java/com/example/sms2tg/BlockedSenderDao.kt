package com.example.sms2tg

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedSenderDao {

    // Вставка без исключения при дубликате (если уже есть такая строка — вернёт -1)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BlockedSender): Long

    @Delete
    suspend fun delete(entity: BlockedSender)

    // Проверка существования перед вставкой (доп. UX, чтобы показать тост)
    @Query("SELECT COUNT(*) FROM blocked_senders WHERE pattern = :pattern")
    suspend fun exists(pattern: String): Int

    // === используется в SmsReceiver.kt ===
    // Синхронная выборка всего списка (в корутине/IO) для проверки правил
    @Query("SELECT * FROM blocked_senders ORDER BY id DESC")
    suspend fun getAll(): List<BlockedSender>

    // На будущее: если нужно наблюдать изменения в UI через Flow
    @Query("SELECT * FROM blocked_senders ORDER BY id DESC")
    fun observeAll(): Flow<List<BlockedSender>>
}
