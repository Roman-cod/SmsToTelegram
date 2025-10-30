# 📦 PROJECT SNAPSHOT — SmsToTelegram v1.3.1

**Author:** Roman Babenko  
**Repository:** [https://github.com/Roman-cod/SmsToTelegram](https://github.com/Roman-cod/SmsToTelegram)  
**Version:** 1.3.1 (commit: latest on main, 2025-10-30)

---

## 🧱 Tech Stack
- Android Studio **Narwhal 4 Feature Drop (2025.1.4)**
- Kotlin **K2 (stable)**
- Android Gradle Plugin (AGP) **8.6+**
- Gradle **8.9+**, JDK **17**
- Target SDK **34**, Min SDK **26**
- **Room** persistence (AppDatabase, LogDao, PendingDao, BlockedSenderDao)
- **WorkManager** — отложенная отправка при отсутствии сети
- **DataBinding** (`buildFeatures.dataBinding = true`)
- **Telegram Bot API** (через TelegramClient)
- **Material Components + RecyclerView** UI
- **Coroutines + Flow** для обновления логов в реальном времени

---

## 📲 Core Features

### 1️⃣ SMS → Telegram forwarding
- Принимает входящие SMS через `SmsReceiver`
- Отправляет их в Telegram через `TelegramClient`
- При offline сохраняет в Room (`PendingMessage`)
- WorkManager доставляет их при восстановлении сети

### 2️⃣ Offline Mode
- Очередь сообщений через `PendingMessage`
- Фоновая пересылка при появлении сети

### 3️⃣ Logging System
- Все события фиксируются в `LogEntity`
- Отображаются в списке логов
- Очистка логов одной кнопкой
- Новый `Logger` — пишет логи только в Debug Mode

### 4️⃣ Blocklist System
- Экран `BlockedListActivity` для управления блокировками
- Добавление шаблонов номеров или имён отправителей
- Проверка уникальности (нельзя добавить дубликат)
- Удаление через кнопку 🗑️
- Кнопка **Назад** в AppBar
- Заблокированные SMS не пересылаются, но логируются

### 5️⃣ Settings
- Ввод и сохранение `Bot Token` и `Chat ID`
- Тестовая отправка сообщения
- Проверка разрешений RECEIVE_SMS / READ_SMS
- Новый чекбокс **Debug Mode** управляет выводом логов в БД

---

## 💾 Room Entities
| Entity | Назначение |
|--------|-------------|
| `LogEntity(id, timestamp, sender, body)` | Журнал событий |
| `PendingMessage(id, sender, body, timestamp)` | Очередь неотправленных SMS |
| `BlockedSender(id, pattern)` | Список блокировок |

---

## 📂 Основные исходники
📁 `com.example.sms2tg`  
┣ 📄 `MainActivity.kt` — главный экран, Debug Mode, настройки  
┣ 📄 `SmsReceiver.kt` — приём SMS, фильтрация, логирование  
┣ 📄 `SendPendingWorker.kt` — отложенная отправка  
┣ 📄 `MessageQueueManager.kt` — управление очередью сообщений  
┣ 📄 `TelegramClient.kt` — взаимодействие с Telegram Bot API  
┣ 📄 `BlockedListActivity.kt` — экран управления блокировками  
┣ 📄 `AppDatabase.kt` — конфигурация Room  
┣ 📄 `LogDao.kt`, `PendingDao.kt`, `BlockedSenderDao.kt` — DAO-интерфейсы  
┣ 📄 `Logger.kt`, `Prefs.kt` — утилиты логирования и настроек  
┗ 📄 `activity_main.xml`, `activity_blocked_list.xml`, `item_blocked_sender.xml` — UI layouts

---

## 💾 Data Flow
📩 `SMS_RECEIVED`
↓  
📦 `SmsReceiver` → Room (`PendingMessage`)  
↓  
⚙️ `WorkManager` (`SendPendingWorker`)  
↓  
🌐 `TelegramClient` → Telegram API  
↓  
🧾 `Logger` → Room (`LogEntity`)

---

## 🧱 Build & Runtime Notes
- Совместимо с Android 7.0+  
- Используется Room, WorkManager, Coroutines, Flow  
- Поддержка Material Design 3 и тёмной темы  
- DataBinding включён  
- Логи отображаются в реальном времени  
- Совместимо с JDK 17 / AGP 8.6 / Gradle 8.9

---

## 🚀 Next Steps (Roadmap v1.4.x)
- 🔍 Поиск и фильтрация логов в UI
- 💾 Экспорт / импорт списка блокировок (JSON / TXT)
- 📢 Уведомления о неудачных отправках
- ✉️ Автоответы (SMS ↔ Telegram)
- 🎨 Улучшение Material3 UI / Dark theme

---

### 📌 Version Summary
| Версия | Дата | Основные изменения |
|--------|------|--------------------|
| **1.3.1** | 2025-10-30 | Debug Mode fix, улучшения Blocklist, оптимизация логов |
| **1.3.0** | 2025-10-25 | Новый экран Blocklist, фильтрация SMS |
| **1.2.0** | 2025-10-21 | Offline Mode через WorkManager |
| **1.0.0** | 2025-10-15 | Первая рабочая версия приложения |
