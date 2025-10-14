package com.example.sms2tg

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sms2tg.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LogAdapter
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }
    private val bg = Executors.newSingleThreadExecutor()

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            val granted = map.entries.all { it.value == true }
            if (!granted) {
                Toast.makeText(this, "Permissions denied — SMS won't be received", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prefilled demo credentials (you can overwrite in UI)
        binding.etToken.setText(prefs.getString("bot_token", "1234569172:AAFT3s9yZqkVbL8nRmQp4Xc6Ht2UwYzBq5e"))
        binding.etChatId.setText(prefs.getString("chat_id", "-4123456781"))

        binding.btnSave.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            val chatId = binding.etChatId.text.toString().trim()
            prefs.edit().putString("bot_token", token).putString("chat_id", chatId).apply()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestSend.setOnClickListener {
            val token = prefs.getString("bot_token", "") ?: ""
            val chatId = prefs.getString("chat_id", "") ?: ""
            if (token.isBlank() || chatId.isBlank()) {
                Toast.makeText(this, "Please set token and chat id first", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnTestSend.isEnabled = false
                bg.execute {
                    val client = TelegramClient(this)
                    val ok = client.sendMessage(token, chatId, "Test message from SMS→TG app")
                    runOnUiThread {
                        binding.btnTestSend.isEnabled = true
                        Toast.makeText(this, if (ok) "Test sent" else "Failed to send", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnDefaultSms.setOnClickListener {
            checkAndRequestDefaultSmsRole()
        }

        binding.btnClearLogs.setOnClickListener {
            bg.execute {
                val db = AppDatabase.get(this)
                db.logDao().clearAll()
                runOnUiThread {
                    loadLogs()
                }
            }
        }

        // Debug checkbox
        binding.chDebug.isChecked = prefs.getBoolean("debug_mode", true)
        binding.chDebug.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefs.edit().putBoolean("debug_mode", isChecked).apply()
        }

        adapter = LogAdapter()
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter

        loadLogs()
        ensurePermissions()
        updateSmsStatus()
    }

    private fun ensurePermissions() {
        val permsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permsNeeded.add(Manifest.permission.RECEIVE_SMS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permsNeeded.add(Manifest.permission.READ_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permsNeeded.isNotEmpty()) {
            requestPermissionsLauncher.launch(permsNeeded.toTypedArray())
        }
    }

    private fun loadLogs() {
        val db = AppDatabase.get(this)
        bg.execute {
            val all = db.logDao().getLast100()
            runOnUiThread {
                adapter.submitList(all)
            }
        }
    }

    private fun checkAndRequestDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
                && !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Приложение уже установлено как SMS по умолчанию", Toast.LENGTH_SHORT).show()
            }
        } else {
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
            if (defaultSmsPackage != packageName) {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Приложение уже установлено как SMS по умолчанию", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSmsStatus() {
        val currentDefault = Telephony.Sms.getDefaultSmsPackage(this)
        if (currentDefault == packageName) {
            binding.btnDefaultSms.isEnabled = false
            binding.btnDefaultSms.text = "✅ Разрешение получено"
        } else {
            binding.btnDefaultSms.isEnabled = true
            binding.btnDefaultSms.text = "Разрешить приём SMS (по умолчанию)"
        }
    }
}
