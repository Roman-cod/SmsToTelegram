package com.example.sms2tg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Лёгкая обёртка: перенаправляет на BlockedListActivity и завершает себя.
 * Это устраняет ошибки activity_add_blocked_sender / add_blocked_sender / etNumber,
 * т.к. этот экран не использует разметку и ресурсы.
 */
class AddBlockedSenderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, BlockedListActivity::class.java))
        finish()
    }
}
