package com.example.sms2tg

import androidx.room.Entity
import androidx.room.PrimaryKey

// Таблица логов с корректным именем 'logs'.
@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long
)
