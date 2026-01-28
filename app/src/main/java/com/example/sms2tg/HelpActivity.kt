package com.example.sms2tg

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.IOException

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Устанавливаем заголовок и кнопку "Назад" в тулбаре
        supportActionBar?.apply {
            title = getString(R.string.help)
            setDisplayHomeAsUpEnabled(true)
        }

        val tvHelpContent: TextView = findViewById(R.id.tvHelpContent)

        try {
            // Чтение файла README.md из assets
            val inputStream: InputStream = assets.open("README.md")
            val content = inputStream.bufferedReader().use { it.readText() }

            // Простейшая конвертация Markdown в HTML для базового форматирования
            val htmlContent = content
                .replace(Regex("(?m)^# (.*)\$"), "<h1>$1</h1>")
                .replace(Regex("(?m)^## (.*)\$"), "<h2>$1</h2>")
                .replace(Regex("(?m)^---$"), "<hr/>")
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                .replace("\n", "<br/>")

            // Отображение как HTML
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvHelpContent.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                tvHelpContent.text = Html.fromHtml(htmlContent)
            }

        } catch (e: IOException) {
            tvHelpContent.text = "Ошибка загрузки справки: ${e.message}"
            e.printStackTrace()
        }
    }

    // Обработка кнопки "Назад" в тулбаре
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
