package com.example.sms2tg

import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Менеджер очереди сообщений.
 */
class MessageQueueManager(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val dao = db.pendingDao()
    private val prefs by lazy {
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
     * Добавить сообщение в очередь.
     */
    fun addToQueue(sender: String, body: String, timestamp: Long = System.currentTimeMillis()): Long {
        return try {
            val id = dao.insert(PendingMessage(sender = sender, body = body, timestamp = timestamp))
            Log.d("SmsToTelegram", "Queued message id=$id sender=$sender body=${body.take(80)}")
            id
        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Failed to insert to queue", e)
            -1
        }
    }

    /**
     * Попытаться отправить все накопленные сообщения.
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
        val deviceName = prefs.getString("device_name", Build.MODEL) ?: Build.MODEL
        
        if (token.isBlank() || chatId.isBlank()) {
            Log.w("SmsToTelegram", "No bot_token or chat_id configured")
            return 0
        }

        var successCount = 0
        val client = TelegramClient(context)

        for (msg in all) {
            try {
                // Префикс с именем устройства для каждого сообщения
                val devicePrefix = "[$deviceName]"
                
                // Разделяем логику формирования текста: для системы/батареи или для SMS
                val text = if (msg.sender == "Battery" || msg.sender == "System") {
                    "$devicePrefix ${msg.body}"
                } else {
                    "$devicePrefix 📩 SMS from: ${msg.sender}\n\n${msg.body}"
                }

                val result = client.sendMessage(token, chatId, text)
                if (result is TelegramClient.Result.Success) {
                    dao.deleteById(msg.id)
                    successCount++
                    Log.d("SmsToTelegram", "Sent queued msg id=${msg.id}")
                } else {
                    val errorMsg = (result as? TelegramClient.Result.Error)?.message ?: "Unknown error"
                    Log.w("SmsToTelegram", "Failed to send msg id=${msg.id}: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("SmsToTelegram", "Exception sending msg id=${msg.id}", e)
            }
        }

        Log.i("SmsToTelegram", "trySendAllPending: sent $successCount of ${all.size}")
        return successCount
    }

    fun clearAll() {
        try {
            dao.clearAll()
        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Failed to clear pending", e)
        }
    }
}
