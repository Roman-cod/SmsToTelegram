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

        // ⚠️ ВАЖНО: Используем goAsync(), чтобы система не убила Receiver, пока работает корутина
        val pendingResult = goAsync()

        // Всё, что блокирующее: в корутине на IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)

                // 1️⃣ Проверка по блок-листу
                val blocked = db.blockedSenderDao().getAll()
                
                val isBlocked = blocked.any { rule ->
                    // 1. Прямое текстовое совпадение (для Tele2, Info и т.д., а также точных совпадений)
                    if (sender.contains(rule.pattern, ignoreCase = true)) return@any true
                    
                    // 2. Нормализация (только цифры) для телефонов (например, +7 (999)... vs 8999...)
                    val cleanSender = sender.filter { it.isDigit() }
                    val cleanRule = rule.pattern.filter { it.isDigit() }
                    
                    // Если правило содержит цифры (это телефон), проверяем совпадение по цифрам
                    if (cleanRule.isNotEmpty() && cleanSender.isNotEmpty()) {
                        cleanSender.contains(cleanRule)
                    } else {
                        false
                    }
                }

                if (isBlocked) {
                    // 🚫 Запись в лог только при включённом Debug Mode
                    Logger.i(context, sender.ifEmpty { "unknown" }, "[BLOCKED] $fullText")
                    Log.d("SmsToTelegram", "🚫 Blocked by rule; sender=$sender")
                    
                    // 🛑 Останавливаем распространение SMS (чтобы не попало во входящие)
                    pendingResult.abortBroadcast()
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
            } finally {
                // ✅ Обязательно завершаем PendingResult, чтобы отпустить Receiver
                pendingResult.finish()
            }
        }
    }
}
