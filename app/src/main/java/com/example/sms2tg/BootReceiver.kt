package com.example.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * BroadcastReceiver, который запускает необходимые сервисы после
 * завершения загрузки устройства или после обновления приложения.
 * В частности, запускает [BatteryMonitorService] для отслеживания
 * состояния батареи, если эта функция включена в настройках.
 */
class BootReceiver : BroadcastReceiver() {
    /**
     * Вызывается при получении широковещательного сообщения.
     * Обрабатывает события [Intent.ACTION_BOOT_COMPLETED] и [Intent.ACTION_MY_PACKAGE_REPLACED].
     * Если получено одно из этих действий, проверяет настройки и запускает
     * [BatteryMonitorService].
     *
     * @param context Контекст приложения.
     * @param intent Полученное сообщение.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            Logger.i(context, "BootReceiver", "Received action: ${intent.action}")

            // Проверяем, включен ли мониторинг батареи (по умолчанию включен)
            if (Prefs.isBatteryMonitorEnabled(context)) {
                startBatteryService(context)
            }
        }
    }

    /**
     * Запускает [BatteryMonitorService].
     * Использует [Context.startForegroundService] для Android O (API 26) и выше,
     * чтобы соответствовать требованиям запуска фоновых служб.
     *
     * @param context Контекст приложения.
     */
    private fun startBatteryService(context: Context) {
        val serviceIntent = Intent(context, BatteryMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        Logger.i(context, "BootReceiver", "Starting BatteryMonitorService")
    }
}