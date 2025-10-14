<# make_backup_fixed.ps1
   Создаёт чистый архив SmsToTelegram_backup.zip, исключая build/ и IDE-кеши.
   Требует robocopy (входит в Windows).
#>

# Настройки — поправь путь, если нужно
$ProjectPath = "C:\u\brv\Android_my_prg\SmsToTelegram_windows"
$BackupName  = "SmsToTelegram_backup.zip"
$BackupPath  = Join-Path $ProjectPath $BackupName

# Временная папка (в %TEMP%)
$TempRoot = Join-Path $env:TEMP ("SmsToTelegram_export_" + (Get-Random))
$TempProject = Join-Path $TempRoot "SmsToTelegram_windows"

Write-Host "Project: $ProjectPath"
Write-Host "Temp copy: $TempProject"
Write-Host "Backup: $BackupPath"
Write-Host ""

# Проверки
if (-not (Test-Path $ProjectPath)) {
    Write-Error "Project path not found: $ProjectPath"
    exit 1
}

# 1) Создать .gitignore, если нет
$gitignorePath = Join-Path $ProjectPath ".gitignore"
if (-not (Test-Path $gitignorePath)) {
    @"
# Gradle & Android Studio
.gradle/
build/
**/build/
!gradle/wrapper/gradle-wrapper.jar
!gradle/wrapper/gradle-wrapper.properties

# IDE and OS
.idea/
*.iml
local.properties

# Binaries, archives, logs
*.apk
*.aab
*.keystore
*.jks
*.zip
*.log
SmsToTelegram_backup.zip

# Others
captures/
.externalNativeBuild/
"@ | Set-Content -Path $gitignorePath -Encoding UTF8
    Write-Host "Created .gitignore"
} else {
    Write-Host ".gitignore exists"
}

# 2) Создать .gitattributes, если нет
$gitattributesPath = Join-Path $ProjectPath ".gitattributes"
if (-not (Test-Path $gitattributesPath)) {
    @"
# Normalize line endings
* text=auto

# Force LF for code files (useful for cross-platform)
*.kt text eol=lf
*.java text eol=lf
*.gradle text eol=lf
*.xml text eol=lf
*.md text eol=lf
*.sh text eol=lf

# Binary
*.jar binary
*.png binary
*.jpg binary
*.zip binary
*.apk binary
gradle/wrapper/gradle-wrapper.jar binary
"@ | Set-Content -Path $gitattributesPath -Encoding UTF8
    Write-Host "Created .gitattributes"
} else {
    Write-Host ".gitattributes exists"
}

# 3) Создать временную папку
New-Item -ItemType Directory -Path $TempProject -Force | Out-Null

# 4) Формируем robocopy параметры исключений
$excludeDirs = @(
    ".git",
    ".idea",
    ".gradle",
    "build",
    "app\build",
    "captures",
    ".externalNativeBuild"
)

$excludeFiles = @(
    "local.properties",
    "*.iml",
    "*.apk",
    "*.aab",
    "*.zip",
    "*.log",
    "SmsToTelegram_backup.zip"
)

$XD = $excludeDirs -join " "
$XF = $excludeFiles -join " "

Write-Host "Copying project to temp folder (robocopy)..."
$robocopyArgs = @(
    ('"' + $ProjectPath + '"'),
    ('"' + $TempProject + '"'),
    '*',
    "/E",
    "/COPY:DAT",
    "/R:1",
    "/W:1",
    "/NFL",
    "/NDL",
    "/NJH",
    "/NJS",
    "/XD",
    $XD,
    "/XF",
    $XF
)

# Запуск robocopy
$robocopyExe = "robocopy"
# Запускаем команду и сохраняем текст вывода
$rcOutput = & $robocopyExe $robocopyArgs 2>&1
$robocopyExit = $LASTEXITCODE

Write-Host "robocopy exit code: $robocopyExit"
if ($robocopyExit -ge 8) {
    Write-Warning "robocopy failed with code $robocopyExit. See robocopy output below:"
    Write-Host $rcOutput
    Remove-Item -Recurse -Force $TempRoot -ErrorAction SilentlyContinue
    exit 1
} else {
    # Для информации выведем краткий фрагмент вывода robocopy (если есть)
    if ($rcOutput) {
        Write-Host "robocopy output (preview):"
        $rcOutput | Select-Object -First 20 | ForEach-Object { Write-Host $_ }
    }
}

# 5) Удалим ненужные файлы, которые могли попасть (на всякий случай)
$maybeRemove = @("*.log","*.zip","SmsToTelegram_backup.zip")
foreach ($pat in $maybeRemove) {
    Get-ChildItem -Path $TempProject -Recurse -Filter $pat -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
}

# 6) Создать zip из содержимого временной папки
if (Test-Path $BackupPath) {
    Remove-Item -Force $BackupPath -ErrorAction SilentlyContinue
}

Write-Host "Creating ZIP archive..."
Compress-Archive -Path (Join-Path $TempProject '*') -DestinationPath $BackupPath -Force

# 7) Удаляем временную папку
Remove-Item -Recurse -Force $TempRoot

# 8) Информационный вывод (с форматированием строки отдельно)
if (Test-Path $BackupPath) {
    $sizeMB = (Get-Item $BackupPath).Length / 1MB
    $msg = "Backup created: $BackupPath (size: {0:N2} MB)" -f $sizeMB
    Write-Host $msg -ForegroundColor Green
} else {
    Write-Host "Backup file was not created." -ForegroundColor Red
}

Write-Host "Done."
