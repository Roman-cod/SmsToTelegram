package com.example.sms2tg

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BatteryMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lastReportedLevel: Int? = null

    // Thresholds: 50, 40, 30, 25, 20, 15, 10, 8, 6, 4, 3, 2, 1
    private val thresholds = setOf(50, 40, 30, 25, 20, 15, 10, 8, 6, 4, 3, 2, 1)

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

                if (level != -1 && scale != -1) {
                    val batteryPct = (level * 100 / scale.toFloat()).toInt()
                    checkBatteryLevel(batteryPct)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Logger.i(this, "BatteryService", "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure foreground is active if restarted
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "battery_monitor_channel"
        val channelName = "Battery Monitor Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmsToTelegram Battery Monitor")
            .setContentText("Monitoring battery levels and SMS ...")
        //    .setSmallIcon(R.drawable.ic_notification)
            .setSmallIcon(R.drawable.ic_notification5)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(101, notification)
    }

    private fun checkBatteryLevel(currentLevel: Int) {
        // 1. Initialize state if unknown
        if (lastReportedLevel == null) {
            lastReportedLevel = currentLevel
            return
        }

        // 2. Re-arm: If battery level increased, update our known level.
        if (currentLevel > lastReportedLevel!!) {
            lastReportedLevel = currentLevel
            return
        }

        // 3. Trigger: If battery level decreased, check if we crossed any threshold.
        if (currentLevel < lastReportedLevel!!) {
            val crossed = thresholds.any { t -> lastReportedLevel!! > t && currentLevel <= t }
            
            if (crossed) {
                sendTelegramAlert(currentLevel)
            }
            
            // Always update tracking to current level after checking
            lastReportedLevel = currentLevel
        }
    }

    private fun sendTelegramAlert(level: Int) {
        serviceScope.launch {
            try {
                val message = "🔋 Battery Level: $level%"
                
                // Записываем в локальный лог приложения
                Logger.i(applicationContext, "Battery", message)

                // Добавляем в очередь сообщений (та же логика, что и для SMS)
                val qm = MessageQueueManager(applicationContext)
                qm.addToQueue(sender = "Battery", body = message)

                // Планируем отправку через WorkManager (он отправит сразу, если есть сеть, или позже)
                SendPendingWorker.schedule(applicationContext)

            } catch (e: Exception) {
                Logger.e(applicationContext, "BatteryService", "Error queuing battery alert: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        serviceScope.cancel()
        Logger.i(this, "BatteryService", "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
