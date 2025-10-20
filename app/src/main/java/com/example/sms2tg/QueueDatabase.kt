package com.example.sms2tg

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Отдельная небольшая Room база для очереди сообщений.
 * Версию увеличивать, если схему будем менять.
 */
@Database(entities = [PendingMessage::class], version = 1, exportSchema = false)
abstract class QueueDatabase : RoomDatabase() {
    abstract fun pendingDao(): PendingDao

    companion object {
        @Volatile
        private var INSTANCE: QueueDatabase? = null

        fun get(context: Context): QueueDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    QueueDatabase::class.java,
                    "sms2tg_queue.db"
                )
                    // allowMainThreadQueries() — не используем; работа идёт в background (Worker / executor)
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
