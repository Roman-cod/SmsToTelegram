package com.example.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * NetworkChangeReceiver
 * ---------------------
 * Этот BroadcastReceiver отслеживает изменение состояния сети (Wi-Fi / мобильный интернет).
 * Как только сеть становится доступной, он инициирует попытку отправки всех сообщений,
 * ранее сохранённых в офлайн-очереди (MessageQueueManager).
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Проверяем, есть ли активное сетевое соединение
        if (isNetworkAvailable(context)) {
            Log.d("NetworkChangeReceiver", "📶 Сеть доступна. Пробуем отправить отложенные сообщения...")

            // Получаем экземпляр менеджера очереди
            val queueManager = MessageQueueManager(context)

            // Пробуем отправить все отложенные SMS
            queueManager.trySendAllPending()
        } else {
            Log.d("NetworkChangeReceiver", "🚫 Сеть недоступна. Сообщения останутся в очереди.")
        }
    }

    /**
     * Проверяет доступность сети (Wi-Fi или мобильной).
     *
     * @param context Контекст приложения
     * @return true если интернет доступен, иначе false
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
