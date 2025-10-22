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
        Log.d("SmsToTelegram", "📩 SMS_RECEIVED triggered")

        val bundle: Bundle? = intent.extras
        if (bundle == null) {
            Log.w("SmsToTelegram", "⚠️ No extras in intent")
            return
        }

        try {
            val pdus = bundle["pdus"] as? Array<*>
            if (pdus == null || pdus.isEmpty()) {
                Log.w("SmsToTelegram", "⚠️ No PDUs in SMS intent")
                return
            }

            var messageBody = ""
            var sender = ""

            for (pdu in pdus) {
                val msg = SmsMessage.createFromPdu(pdu as ByteArray)
                messageBody += msg.messageBody
                sender = msg.originatingAddress ?: ""
            }

            Log.i("SmsToTelegram", "📨 SMS from $sender: $messageBody")

            // Обрабатываем SMS в фоне (не блокируя UI)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1️⃣ Сохраняем в очередь для отложенной отправки
                    val queueManager = MessageQueueManager(context)
                    queueManager.addToQueue(sender, messageBody, System.currentTimeMillis())
                    Log.d("SmsToTelegram", "✅ Added to queue: $sender -> $messageBody")

                    // 2️⃣ Записываем в лог для отображения в UI
                    try {
                        val db = AppDatabase.get(context)
                        db.logDao().insert(LogEntity(0, sender, messageBody, System.currentTimeMillis()))
                        Log.d("SmsToTelegram", "📝 Log inserted: $sender -> ${messageBody.take(50)}")
                    } catch (dbEx: Exception) {
                        Log.e("SmsToTelegram", "❌ Failed to insert log", dbEx)
                    }

                    // 3️⃣ Планируем отложенную отправку, если сети нет
                    SendPendingWorker.schedule(context)
                    Log.d("SmsToTelegram", "📆 SendPendingWorker scheduled with network constraint")

                } catch (e: Exception) {
                    Log.e("SmsToTelegram", "❌ Failed to process SMS", e)
                }
            }

        } catch (e: Exception) {
            Log.e("SmsToTelegram", "❌ Error in SmsReceiver", e)
        }
    }
}
