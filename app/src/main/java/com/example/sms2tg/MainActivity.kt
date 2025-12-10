package com.example.sms2tg

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sms2tg.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Инициализация списка логов ---
        adapter = LogAdapter()
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter

        val db = AppDatabase.get(this)
        // --- Инициализация EncryptedSharedPreferences ---
        val masterKey = androidx.security.crypto.MasterKey.Builder(this)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = androidx.security.crypto.EncryptedSharedPreferences.create(
            this,
            "secret_settings",
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // --- Загрузка сохранённых настроек ---
        binding.etToken.setText(prefs.getString("bot_token", ""))
        binding.etChatId.setText(prefs.getString("chat_id", ""))

        // --- Инициализация чекбокса Debug Mode ---
        val savedDebug = prefs.getBoolean("debug_mode", false)
        binding.chDebug.isChecked = savedDebug

        binding.chDebug.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("debug_mode", isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) "🪲 Debug Mode: ON" else "🚫 Debug Mode: OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        // --- Инициализация чекбокса Battery Monitor ---
        val savedBattery = prefs.getBoolean("battery_monitor", false)
        binding.chBatteryMonitor.isChecked = savedBattery
        
        // Перезапускаем сервис, если галочка стояла (для восстановления после закрытия activity, если сервис мог умереть)
        if (savedBattery) { 
           val intent = Intent(this, BatteryMonitorService::class.java)
           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
               startForegroundService(intent)
           } else {
               startService(intent)
           }
        }

        binding.chBatteryMonitor.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("battery_monitor", isChecked).apply()
            val intent = Intent(this, BatteryMonitorService::class.java)
            if (isChecked) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Toast.makeText(this, "🔋 Battery Monitor: ON", Toast.LENGTH_SHORT).show()
            } else {
                stopService(intent)
                Toast.makeText(this, "🛑 Battery Monitor: OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Сохранить настройки ---
        binding.btnSave.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()
            prefs.edit()
                .putString("bot_token", token)
                .putString("chat_id", chatId)
                .apply()
            Toast.makeText(this, "✅ Настройки сохранены (Зашифровано)", Toast.LENGTH_SHORT).show()
        }

        // --- Тестовая отправка ---
        binding.btnTestSend.setOnClickListener {
            val token = prefs.getString("bot_token", "")
            val chatId = prefs.getString("chat_id", "")
            if (token.isNullOrBlank() || chatId.isNullOrBlank()) {
                Toast.makeText(this, "⚠️ Заполните Token и Chat ID", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val tg = TelegramClient(this@MainActivity)
                    val result = tg.sendMessage(token, chatId, "🤖 Тестовое сообщение из SmsToTelegram!")
                    withContext(Dispatchers.Main) {
                        if (result is TelegramClient.Result.Success) {
                            Toast.makeText(this@MainActivity, "✅ Сообщение отправлено", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMsg = (result as? TelegramClient.Result.Error)?.message ?: "Unknown error"
                            Toast.makeText(this@MainActivity, "❌ Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // --- Очистить лог ---
        binding.btnClearLogs.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.logDao().clearAll()
            }
            Toast.makeText(this, "🧹 Логи очищены", Toast.LENGTH_SHORT).show()
        }

        // --- Открыть список заблокированных отправителей ---
        binding.btnBlockedList.setOnClickListener {
            val intent = Intent(this, BlockedListActivity::class.java)
            startActivity(intent)
        }

        // --- Автоматическое обновление логов ---
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                db.logDao().observeLast(100).collectLatest { logs ->
                    adapter.submitList(logs)
                }
            }
        }

        // --- Проверка разрешений ---
        checkAndRequestSmsPermissions()
    }

    private fun checkAndRequestSmsPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "📱 Разрешения получены", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // При возврате с экрана блок-листа перезагружаем логи
        lifecycleScope.launch {
            val db = AppDatabase.get(this@MainActivity)
            val logs = withContext(Dispatchers.IO) { db.logDao().getLast(100) }
            adapter.submitList(logs)
        }
    }
}
