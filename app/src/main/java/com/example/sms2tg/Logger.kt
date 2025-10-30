package com.example.sms2tg

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Лёгкий централизованный логгер:
 * - Пишет лог только если включён Debug Mode (Prefs.isDebug(context) == true)
 * - Не меняет схему БД: использует LogEntity(sender, body, timestamp)
 * - Поток: IO, отдельный SupervisorJob
 */
object Logger {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Информационное сообщение */
    fun i(context: Context, sender: String, body: String) = log(context, sender, body)

    /** Ошибка (семантически как error, но в БД поле status отсутствует, поэтому просто пишем текст) */
    fun e(context: Context, sender: String, body: String) = log(context, sender, body)

    private fun log(context: Context, sender: String, body: String) {
        if (!Prefs.isDebug(context)) return

        val entity = LogEntity(
            sender = sender,
            body = body,
            timestamp = System.currentTimeMillis()
        )

        scope.launch {
            // Важно: использовать тот же фабричный метод БД, что и в проекте (у тебя он AppDatabase.get(this))
            AppDatabase.get(context).logDao().insert(entity)
        }
    }
}
