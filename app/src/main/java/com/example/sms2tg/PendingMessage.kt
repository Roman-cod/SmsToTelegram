package com.example.sms2tg

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность для хранения отложенных SMS, которые нужно отправить в Telegram.
 * Поля:
 *  - id: автоинкрементный PK
 *  - sender: номер отправителя
 *  - body: текст SMS
 *  - timestamp: время получения (ms)
 */
@Entity(tableName = "pending_messages")
data class PendingMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long
)
