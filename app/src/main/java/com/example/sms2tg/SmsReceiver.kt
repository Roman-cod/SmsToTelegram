package com.example.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return
        Log.d("SmsToTelegram", "📩 SMS_RECEIVED triggered")

        val bundle: Bundle = intent.extras ?: run {
            Log.w("SmsToTelegram", "No extras in SMS intent")
            return
        }

        val pdus = bundle.get("pdus") as? Array<*> ?: run {
            Log.w("SmsToTelegram", "No PDUs in bundle")
            return
        }
        val format = bundle.getString("format")

        val messages = pdus.mapNotNull { pdu ->
            try {
                SmsMessage.createFromPdu(pdu as ByteArray, format)
            } catch (e: Exception) {
                Log.e("SmsToTelegram", "createFromPdu failed", e)
                null
            }
        }
        if (messages.isEmpty()) {
            Log.w("SmsToTelegram", "No valid SMS messages after parsing PDUs")
            return
        }

        // Единый sender и собранный текст
        val sender: String = messages.firstOrNull()?.originatingAddress.orEmpty()
        val fullText: String = messages
            .sortedBy { it.timestampMillis }
            .joinToString(separator = "") { it.messageBody.orEmpty() }

        Log.i("SmsToTelegram", "📨 SMS from $sender: ${fullText.take(200)}${if (fullText.length > 200) "..." else ""}")

        // Всё, что блокирующее: в корутине на IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)

                // 1️⃣ Проверка по блок-листу
                val blocked = db.blockedSenderDao().getAll()
                val isBlocked = blocked.any { rule ->
                    sender.contains(rule.pattern, ignoreCase = true)
                }

                if (isBlocked) {
                    // 🚫 Запись в лог только при включённом Debug Mode
                    Logger.i(context, sender.ifEmpty { "unknown" }, "[BLOCKED] $fullText")
                    Log.d("SmsToTelegram", "🚫 Blocked by rule; sender=$sender")
                    return@launch
                }

                // 2️⃣ Обычная логика: лог + постановка в очередь
                Logger.i(context, sender.ifEmpty { "unknown" }, fullText)

                MessageQueueManager(context).addToQueue(
                    sender = sender,
                    body = fullText,
                    timestamp = System.currentTimeMillis()
                )

                // Планируем отправку (WorkManager выполнит при появлении сети)
                SendPendingWorker.schedule(context)

            } catch (e: Exception) {
                Log.e("SmsToTelegram", "Error in SmsReceiver coroutine", e)
                // Лог ошибки (если Debug Mode включён)
                Logger.e(context, "SmsReceiver", "Error: ${e.message}")
            }
        }
    }
}
