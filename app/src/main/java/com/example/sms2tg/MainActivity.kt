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
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // --- Загрузка сохранённых настроек ---
        binding.etToken.setText(prefs.getString("bot_token", ""))
        binding.etChatId.setText(prefs.getString("chat_id", ""))

        // --- Сохранить настройки ---
        binding.btnSave.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()
            prefs.edit()
                .putString("bot_token", token)
                .putString("chat_id", chatId)
                .apply()
            Toast.makeText(this, "✅ Настройки сохранены", Toast.LENGTH_SHORT).show()
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
                    val ok = tg.sendMessage(token, chatId, "🤖 Тестовое сообщение из SmsToTelegram!")
                    withContext(Dispatchers.Main) {
                        if (ok) Toast.makeText(this@MainActivity, "✅ Сообщение отправлено", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this@MainActivity, "❌ Ошибка отправки", Toast.LENGTH_SHORT).show()
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
