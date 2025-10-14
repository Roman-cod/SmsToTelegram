package com.example.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import java.util.concurrent.Executors

class SmsReceiver : BroadcastReceiver() {
    private val bg = Executors.newSingleThreadExecutor()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle: Bundle? = intent.extras
            try {
                if (bundle != null) {
                    @Suppress("DEPRECATION")
                    val pdus = bundle.get("pdus") as? Array<*> ?: return
                    for (pdu in pdus) {
                        val format = bundle.getString("format")
                        val sms = SmsMessage.createFromPdu(pdu as ByteArray, format)
                        val from = sms.originatingAddress ?: "unknown"
                        val body = sms.messageBody ?: ""
                        val debug = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("debug_mode", true)
                        if (debug) {
                            Log.d("SmsToTelegram", "Received: from=$from body=$body")
                        }
                        bg.execute {
                            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            val token = prefs.getString("bot_token", "") ?: ""
                            val chatId = prefs.getString("chat_id", "") ?: ""
                            if (token.isNotBlank() && chatId.isNotBlank()) {
                                val client = TelegramClient(context)
                                val ok = client.sendMessage(token, chatId, "SMS from $from: $body")
                                if (debug) {
                                    Log.d("SmsToTelegram", "Sent to Telegram: ok=$ok")
                                }
                            }
                            val db = AppDatabase.get(context)
                            db.logDao().insert(LogEntity(0, from, body, System.currentTimeMillis()))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsToTelegram", "Error processing SMS", e)
            }
        }
    }
}
