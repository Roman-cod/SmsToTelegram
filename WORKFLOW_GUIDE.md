# 🚀 Workflow: Google Antigravity + Android Studio

Этот документ описывает оптимальный процесс разработки (Best Practices 2025), объединяющий генеративную мощь **Google Antigravity** и надежность экосистемы **Android Studio**.

---

## 🛠 Роли инструментов

Для максимальной эффективности мы разделяем зоны ответственности:

| Инструмент | Роль | Основные задачи |
| :--- | :--- | :--- |
| **Google Antigravity** | 🧠 **Архитектор & Кодер** | • Генерация UI (Jetpack Compose)<br>• Написание бизнес-логики (Kotlin)<br>• Рефакторинг и Unit-тесты<br>• Работа с Git (AI commits) |
| **Android Studio** | ⚙️ **Инженер & Сборщик** | • Сборка проекта (Gradle Build)<br>• Управление зависимостями (Sync)<br>• Запуск на эмуляторах<br>• Logcat и отладка<br>• Разрешение Git-конфликтов |

---

## ⚡ Алгоритм работы (The Loop)

### 1. Старт проекта
* ⛔ **Не создавать** проект через Antigravity с нуля.
* ✅ **Создать** проект через **Android Studio** (New Project -> Empty Activity). Это гарантирует корректную структуру Gradle и манифеста.
* 📂 После успешной первой сборки — открыть папку проекта в **Antigravity**.

### 2. Процесс разработки (Dual-Monitor Setup)

1.  **Задача:** В Antigravity ставим задачу агенту: *"Создай экран профиля пользователя с полями имя, фото и кнопкой выхода"*.
2.  **Генерация:** Агент создает файлы (`ProfileScreen.kt`, `ProfileViewModel.kt`) и обновляет навигацию.
3.  **Синхронизация (Критично!):**
    * Если агент добавил библиотеку в `build.gradle.kts`:
    * Переключаемся в Android Studio -> Нажимаем 🐘 **"Sync Now"**.
4.  **Визуализация:**
    * Держим Android Studio открытой на втором мониторе (или в сплите).
    * Открываем созданный `ProfileScreen.kt`.
    * Панель **Compose Preview** автоматически обновится после сохранения файла в Antigravity.

### 3. Отладка
* 🐛 **Логи:** Смотрим **Logcat** только в Android Studio.
* 🛠 **Фикс:** Копируем текст ошибки из Logcat -> Вставляем в чат Antigravity -> Агент исправляет код.

---

## 🐙 Работа с Git

В этой связке мы используем **Antigravity** как основной Git-клиент, а **Android Studio** — как инструмент "спасения" при конфликтах.

### 1. Повседневные коммиты (в Antigravity)
Используйте агентов для автоматизации рутины. Вы можете попросить ИИ не просто написать код, но и зафиксировать его.
* **AI Commit:** Напишите в чат:
    > *"Закоммить эти изменения. Напиши семантическое сообщение коммита, описывающее, что мы добавили во ViewModel."*
* **Push:**
    > *"Отправь изменения (push) в ветку feature/login"*

### 2. Разрешение конфликтов (в Android Studio)
Antigravity (VS Code) не так удобен для визуального разрешения конфликтов слияния, как IntelliJ.
* **Сценарий:** Вы сделали `git pull` и получили `CONFLICT`.
* **Действие:**
    1.  Не пытайтесь править `<<<<HEAD` вручную в Antigravity.
    2.  Переключитесь в **Android Studio**.
    3.  Нажмите `Git` -> `Resolve Conflicts`.
    4.  Используйте удобный визуальный интерфейс (3 колонки) для слияния веток.

---

## 💡 Лайфхаки производительности

1.  **Android Studio Power Save Mode:**
    * Перейдите в `File` -> `Power Save Mode`. Это отключит лишние проверки в AS, сэкономив память для Antigravity.
    * *Примечание:* Не забудьте выключить этот режим, если решите писать код внутри AS.

---

## 🤖 System Prompt (Правила для AI)

*Скопируйте этот текст в `Project Rules` или `.cursorrules` в корне проекта Antigravity, чтобы настроить агента.*

```text
You are an expert Senior Android Developer specializing in Modern Android Development (MAD).

CORE WORKFLOW CONTEXT:
We are using a hybrid setup. You (Antigravity) are responsible for WRITING code, refactoring, and logic. I (The User) use Android Studio concurrently for building, syncing Gradle, and running the app.
- DO NOT try to launch emulators or build APKs directly unless explicitly asked.
- Focus on generating high-quality Kotlin code.
- Use Git commands via terminal when asked to commit or push.

TECHNOLOGY STACK PREFERENCES:
- Language: Kotlin (Strictly).
- UI: Jetpack Compose (Material 3). Avoid XML layouts unless dealing with legacy code.
- Architecture: MVVM or MVI with Clean Architecture principles.
- DI: Hilt (or Koin, if specified).
- Async: Coroutines & Flow.
- Networking: Retrofit + OkHttp.

CODING GUIDELINES:
1. COMPOSE PREVIEWS: Always generate a @Preview composable for every UI component so I can see it immediately in Android Studio's split view. Use @Preview(showBackground = true).
2. DEPENDENCIES: If you modify `build.gradle.kts` or `libs.versions.toml`, you MUST end your response with a bold warning: "**⚠️ PLEASE SYNC GRADLE IN ANDROID STUDIO NOW**".
3. ERROR HANDLING: Do not swallow exceptions. Use sealed classes (Result wrappers) for UI states (Loading/Success/Error).

Use this persona for all interactions in this workspace.