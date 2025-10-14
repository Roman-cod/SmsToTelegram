SmsToTelegram Android Studio project (Kotlin)
- Package: com.example.sms2tg
- minSdk 26, targetSdk 34
- Uses HttpURLConnection for Telegram API (no external libs)
- Room DB included for logs (last 100 shown)
- Buttons: Save, Test Send, Request Permissions, Clear Logs
- Dark theme enabled
- Debug-Mode (checkbox) saved in SharedPreferences

Open in Android Studio Narwhal 2025.1.4:
1. File -> Open -> select SmsToTelegram folder
2. Set Gradle JDK to JDK17 (File -> Settings -> Build Tools -> Gradle)
3. File -> Sync Project with Gradle Files
4. Build -> Build APK(s)

