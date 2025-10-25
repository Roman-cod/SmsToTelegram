package com.example.sms2tg

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_senders")
data class BlockedSender(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String
)