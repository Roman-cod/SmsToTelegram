package com.example.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import java.util.concurrent.Executors

class SmsReceiver : BroadcastReceiver() {

    // Буфер частей по отправителю (не обязательный, но оставляем простую реализацию:
    // если несколько PDUs в одном broadcast — они объединяются по sender и order)
    private val executor = Executors.newSingleThreadExecutor()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle: Bundle? = intent.extras
        if (bundle == null) {
            Log.w("SmsToTelegram", "No extras in SMS intent")
            return
        }

        try {
            val pdus = bundle["pdus"] as? Array<*>
            if (pdus == null || pdus.isEmpty()) {
                Log.w("SmsToTelegram", "No PDUs in bundle")
                return
            }

            // Собираем SmsMessage объекты (формат может быть null)
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

            // Получаем отправителя и полный текст (части соединяем в порядке timestamp)
            val sender = messages.firstOrNull()?.originatingAddress ?: "unknown"
            val fullText = messages
                .sortedBy { it.timestampMillis }        // на всякий случай
                .joinToString(separator = "") { it.messageBody ?: "" }

            Log.d("SmsToTelegram", "Merged SMS from $sender: ${fullText.take(200)}${if (fullText.length>200) "..." else ""}")

            // Читаем настройки из тех же SharedPreferences, что используешь в MainActivity
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val token = prefs.getString("bot_token", "") ?: ""
            val chatId = prefs.getString("chat_id", "") ?: ""
            val debug = prefs.getBoolean("debug_mode", true)

            // Сохраняем лог в БД и одновременно отправляем в Telegram (в bg-потоке)
            executor.execute {
                try {
                    // Сохраняем в Room
                    try {
                        val db = AppDatabase.get(context)
                        db.logDao().insert(LogEntity(0, sender ?: "unknown", fullText, System.currentTimeMillis()))
                    } catch (dbEx: Exception) {
                        Log.e("SmsToTelegram", "DB insert failed", dbEx)
                    }

                    // Если нет настроек — не шлём, но лог остаётся
                    if (token.isBlank() || chatId.isBlank()) {
                        Log.e("SmsToTelegram", "bot_token or chat_id empty — skipping Telegram send")
                        return@execute
                    }

                    // ВАЖНО: вызвать TelegramClient в соответствии с реализацией в твоём проекте.
                    // В твоём проекте TelegramClient(context).sendMessage(token, chatId, text)
                    try {
                        val client = TelegramClient(context) // конструктор с Context — как в проекте
                        val ok = client.sendMessage(token, chatId, "📩 SMS from $sender:\n\n$fullText")
                        if (debug) {
                            Log.d("SmsToTelegram", "Telegram send result: ok=$ok")
                        }
                    } catch (netEx: Exception) {
                        Log.e("SmsToTelegram", "Error sending to Telegram", netEx)
                    }

                } catch (e: Exception) {
                    Log.e("SmsToTelegram", "Unexpected error in SMS processing", e)
                }
            }

        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Error processing incoming SMS", e)
        }
    }
}
