package com.example.sms2tg

import android.content.Context
import androidx.core.content.edit

object Prefs {
    private const val PREFS_NAME = "settings"
    private const val KEY_DEBUG = "debug_mode"

    fun isDebug(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DEBUG, false)

    fun setDebug(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_DEBUG, value)
        }
    }
}
