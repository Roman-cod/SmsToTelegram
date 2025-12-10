package com.example.sms2tg

import android.content.Context
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class TelegramClient(private val context: Context) {

    sealed class Result {
        object Success : Result()
        data class Error(val message: String, val exception: Exception? = null) : Result()
    }

    fun sendMessage(token: String, chatId: String, text: String): Result {
        try {
            val url = URL("https://api.telegram.org/bot${token}/sendMessage")
            val postData = "chat_id=" + URLEncoder.encode(chatId, "UTF-8") +
                    "&text=" + URLEncoder.encode(text, "UTF-8")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
            conn.outputStream.use { os ->
                OutputStreamWriter(os, "UTF-8").use { writer ->
                    writer.write(postData)
                    writer.flush()
                }
            }
            val code = conn.responseCode
            val responseMessage = conn.responseMessage
            conn.disconnect()

            return if (code in 200..299) {
                Result.Success
            } else {
                Result.Error("HTTP Error $code: $responseMessage")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Error("Network Error: ${e.message}", e)
        }
    }
}
