package com.example.sms2tg

import android.content.Context
import androidx.core.content.edit

/**
 * Объект-утилита для централизованного управления настройками приложения
 * через [SharedPreferences].
 *
 * Содержит методы для чтения и записи различных пользовательских настроек и флагов,
 * таких как режим отладки и состояние мониторинга батареи.
 */
object Prefs {
    // Имя файла SharedPreferences, используемого для хранения настроек.
    private const val PREFS_NAME = "settings"
    // Ключ для хранения флага режима отладки.
    private const val KEY_DEBUG = "debug_mode"

    /**
     * Проверяет, включен ли режим отладки.
     *
     * Режим отладки может влиять на уровень детализации логирования или поведение
     * некоторых функций.
     *
     * @param context Контекст приложения.
     * @return true, если режим отладки включен (по умолчанию true).
     */
    fun isDebug(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DEBUG, true)

    /**
     * Устанавливает состояние режима отладки.
     *
     * @param context Контекст приложения.
     * @param value Новое значение для режима отладки (true/false).
     */
    fun setDebug(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_DEBUG, value)
        }
    }

    /**
     * Проверяет, включен ли мониторинг состояния батареи.
     *
     * Эта настройка используется [BootReceiver] для принятия решения о запуске
     * службы мониторинга батареи.
     *
     * @param context Контекст приложения.
     * @return true, если мониторинг батареи включен (по умолчанию true).
     */
    fun isBatteryMonitorEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("battery_monitor", true)
}