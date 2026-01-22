package com.example.sms2tg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            Logger.i(context, "BootReceiver", "Received action: ${intent.action}")

            // Check if Battery Monitor is enabled (default is true now)
            if (Prefs.isBatteryMonitorEnabled(context)) {
                startBatteryService(context)
            }
        }
    }

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
