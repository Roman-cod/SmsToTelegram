package com.example.sms2tg

import android.content.Context
import android.util.Log

/**
 * Менеджер очереди сообщений.
 *
 * Использование:
 *  val qm = MessageQueueManager(context)
 *  qm.addToQueue(sender, body)
 *
 *  // Из WorkManager:
 *  val sentCount = qm.trySendAllPending()
 *
 * Логика:
 *  - Сообщения сохраняются в Room (AppDatabase → PendingDao).
 *  - trySendAllPending() при каждой попытке считывает все pending, берет текущие bot token/chat id из SharedPreferences,
 *    пробует отправить каждое через TelegramClient; при успехе удаляет запись.
 *
 * Примечания:
 *  - Метод trySendAllPending() выполняется в том потоке, в котором вызывается (в Worker он уже background).
 *  - Возвращает количество успешно отправленных сообщений.
 */
class MessageQueueManager(private val context: Context) {
    private val db = AppDatabase.get(context)       // ✅ заменили QueueDatabase на AppDatabase
    private val dao = db.pendingDao()
    private val prefs = run {
        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()

        androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            "secret_settings",
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Добавить SMS в очередь.
     * Возвращает id добавленной записи (Long)
     */
    fun addToQueue(sender: String, body: String, timestamp: Long = System.currentTimeMillis()): Long {
        return try {
            val id = dao.insert(PendingMessage(sender = sender, body = body, timestamp = timestamp))
            Log.d("SmsToTelegram", "Queued SMS id=$id sender=$sender body=${body.take(80)}")
            id
        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Failed to insert to queue", e)
            -1
        }
    }

    /**
     * Попытаться отправить все накопленные сообщения.
     * Возвращает количество успешно отправленных записей.
     */
    fun trySendAllPending(): Int {
        val all = try {
            dao.getAll()
        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Failed to read pending messages", e)
            return 0
        }

        if (all.isEmpty()) {
            Log.d("SmsToTelegram", "No pending messages to send")
            return 0
        }

        val token = prefs.getString("bot_token", "") ?: ""
        val chatId = prefs.getString("chat_id", "") ?: ""
        if (token.isBlank() || chatId.isBlank()) {
            Log.w("SmsToTelegram", "No bot_token or chat_id configured — not sending pending messages")
            return 0
        }

        var successCount = 0
        val client = TelegramClient(context)

        for (msg in all) {
            try {
                val text = "📩 SMS from: ${msg.sender}\n\n${msg.body}"
                val result = client.sendMessage(token, chatId, text)
                if (result is TelegramClient.Result.Success) {
                    dao.deleteById(msg.id)
                    successCount++
                    Log.d("SmsToTelegram", "Sent queued msg id=${msg.id}")
                } else {
                    val errorMsg = (result as? TelegramClient.Result.Error)?.message ?: "Unknown error"
                    Log.w("SmsToTelegram", "Failed to send queued msg id=${msg.id}: $errorMsg — will retry later")
                }
            } catch (e: Exception) {
                Log.e("SmsToTelegram", "Exception sending queued msg id=${msg.id}", e)
            }
        }

        Log.i("SmsToTelegram", "trySendAllPending: sent $successCount of ${all.size}")
        return successCount
    }

    /** Полная очистка очереди (удалить всё) */
    fun clearAll() {
        try {
            dao.clearAll()
            Log.d("SmsToTelegram", "Cleared all pending messages")
        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Failed to clear pending", e)
        }
    }
}
