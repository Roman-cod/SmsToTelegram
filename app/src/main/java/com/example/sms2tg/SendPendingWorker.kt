package com.example.sms2tg

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Фоновая задача, которая пытается отправить все накопленные SMS,
 * если интернет снова доступен.
 */
class SendPendingWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("SmsToTelegram", "SendPendingWorker started")

        return try {
            val queueManager = MessageQueueManager(appContext)
            val count = queueManager.trySendAllPending() // попытка отправить все
            Log.i("SmsToTelegram", "SendPendingWorker sent $count messages")
            Result.success()
        } catch (e: Exception) {
            Log.e("SmsToTelegram", "Error in SendPendingWorker", e)
            Result.retry()
        }
    }

    companion object {
        /**
         * Запускает задачу при наличии сети.
         * WorkManager сам дождётся подключения к интернету.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SendPendingWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueue(workRequest)

            Log.d("SmsToTelegram", "SendPendingWorker scheduled with network constraint")
        }
    }
}
