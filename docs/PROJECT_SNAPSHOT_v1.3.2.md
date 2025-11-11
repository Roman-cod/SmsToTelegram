# 📦 PROJECT SNAPSHOT — SmsToTelegram v1.3.2

**Author:** Roman Babenko  
**Repository:** [https://github.com/Roman-cod/SmsToTelegram](https://github.com/Roman-cod/SmsToTelegram)  
**Version:** 1.3.2 (commit: latest on main, 2025-10-31)

---

## 🧱 Tech Stack
- Android Studio **Narwhal 4 Feature Drop (2025.1.4)**
- Kotlin **K2 (stable)**
- Android Gradle Plugin (AGP) **8.6+**
- Gradle **8.9+**, **JDK 17**
- Target SDK **34**, Min SDK **26**
- **Room** persistence (`AppDatabase`, `LogDao`, `PendingDao`, `BlockedSenderDao`)
- **WorkManager** — отложенная отправка при offline
- **DataBinding** (`buildFeatures.dataBinding = true`)
- **Telegram Bot API** через `TelegramClient`
- **Material Components + RecyclerView**
- **Coroutines + Flow** для асинхронного обновления логов

---

## 📲 Core Features

### 1️⃣ SMS → Telegram forwarding
- Приём SMS через `SmsReceiver`
- Отправка в Telegram через `TelegramClient`
- При отсутствии сети сохраняет SMS в Room (`PendingMessage`), и WorkManager автоматически пересылает их при восстановлении соединения.

### 2️⃣ Offline Mode
- Очередь сообщений (`PendingMessage`)
- Фоновая отправка при появлении сети

### 3️⃣ Logging System
- Все события фиксируются в `LogEntity`
- Отображаются в UI через `LogAdapter`
- Очистка логов одной кнопкой
- Централизованный `Logger.kt`, пишет логи **только в Debug Mode**

### 4️⃣ Blocklist System
- Экран `BlockedListActivity` — управление заблокированными отправителями
- Добавление шаблонов номеров или имён (`Tele2Info`, `Beeline`)
- Проверка уникальности (нельзя добавить дубликат)
- Удаление через кнопку 🗑️
- Кнопка “Назад” в AppBar
- Заблокированные SMS не пересылаются, но фиксируются в логах

### 5️⃣ Settings
- Ввод и сохранение `Bot Token` и `Chat ID`
- Тестовая отправка сообщений
- Проверка разрешений RECEIVE_SMS / READ_SMS
- Чекбокс **Debug Mode** управляет записью логов в БД

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
┣ 📄 `MainActivity.kt` — главный экран, настройки, Debug Mode  
┣ 📄 `SmsReceiver.kt` — приём SMS, фильтрация, логирование  
┣ 📄 `SendPendingWorker.kt` — отложенная пересылка WorkManager  
┣ 📄 `MessageQueueManager.kt` — управление очередью сообщений (через `AppDatabase`)  
┣ 📄 `TelegramClient.kt` — отправка в Telegram  
┣ 📄 `BlockedListActivity.kt` — экран управления блокировками  
┣ 📄 `AppDatabase.kt` — конфигурация Room  
┣ 📄 `LogDao.kt`, `PendingDao.kt`, `BlockedSenderDao.kt` — DAO-интерфейсы  
┣ 📄 `LogEntity.kt`, `PendingMessage.kt`, `BlockedSender.kt` — Room Entities  
┣ 📄 `Logger.kt` — централизованное логирование  
┣ 📄 `Prefs.kt` — хранение пользовательских настроек  
┗ 📄 `LogAdapter.kt` — адаптер для списка логов

---

## 💾 Data Flow (основная логика)
📩 **SMS_RECEIVED**  
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
- Android 7.0+ (API 26 min)  
- Совместимо с **JDK 17**, **AGP 8.6**, **Gradle 8.9**  
- DataBinding включён  
- Полностью мигрировано на единую базу `AppDatabase`  
- Все устаревшие классы (`QueueDatabase`, `AddBlockedSenderActivity`, `BlockedAdapter`, `BlockedVH`) удалены  
- Структура проекта унифицирована и упрощена

---

## 🆕 Version 1.3.2 Highlights
- 💾 Все DAO объединены под `AppDatabase`
- 🧹 Удалены старые и дублирующиеся классы (`QueueDatabase`, `BlockedAdapter`, `BlockedVH`, `AddBlockedSenderActivity`)
- 🧠 Обновлён `MessageQueueManager`
- ⚙️ Структура проекта упрощена и готова для следующих фич (v1.4.x)
- ✅ Тестовая сборка успешно собрана и проверена

---

## 🚀 Next Steps (v1.4.x Roadmap)
- 🔍 Поиск и фильтрация логов в UI
- 💾 Экспорт / импорт списка блокировок (JSON / TXT)
- 📢 Уведомления о неудачных отправках
- ✉️ Автоответы (SMS ↔ Telegram)
- 🎨 Полная интеграция Material 3 / Dark theme

---

### 📌 Version Summary
| Версия | Дата | Основные изменения |
|--------|------|--------------------|
| **1.3.2** | 2025-10-31 | Cleanup / Refactor / Unification |
| **1.3.1** | 2025-10-30 | Debug Mode fix, Blocklist UX улучшения |
| **1.3.0** | 2025-10-25 | Новый экран Blocklist |
| **1.2.0** | 2025-10-21 | Offline Mode |
| **1.0.0** | 2025-10-15 | Первая рабочая версия |
