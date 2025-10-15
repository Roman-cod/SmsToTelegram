# make_backup_fixed_v7.ps1
# ----------------------------------------------
# Создает резервную копию Android проекта, исключая временные и бинарные файлы
# ----------------------------------------------

$projectPath = "C:\u\brv\Android_my_prg\SmsToTelegram_windows"
$projectName = Split-Path $projectPath -Leaf
$tempFolder = Join-Path $env:TEMP "SmsToTelegram_export_$([int](Get-Random -Minimum 100000000 -Maximum 999999999))"
$tempProject = Join-Path $tempFolder $projectName

# Папка для временных архивов
$backupFolder = Join-Path $env:TEMP "SmsToTelegram_backups"
New-Item -ItemType Directory -Force -Path $backupFolder | Out-Null
$backupFileTemp = Join-Path $backupFolder "SmsToTelegram_backup.zip"

# Финальный ZIP в проекте
$backupFileFinal = Join-Path $projectPath "SmsToTelegram_backup.zip"

Write-Host "Removing old backup..."

# Удаляем старые архивы
Remove-Item $backupFileTemp -ErrorAction SilentlyContinue
Remove-Item $backupFileFinal -ErrorAction SilentlyContinue

Write-Host "`nProject: $projectPath"
Write-Host "Temp copy: $tempProject"
Write-Host "Backup temp: $backupFileTemp"
Write-Host "Backup final: $backupFileFinal`n"

# ----------------------------------------------
# Создание временной папки
# ----------------------------------------------
New-Item -ItemType Directory -Force -Path $tempFolder | Out-Null

# ----------------------------------------------
# Копирование проекта
# ----------------------------------------------
Write-Host "Copying project..."
Copy-Item -Path $projectPath -Destination $tempFolder -Recurse -Force `
    -Exclude *.zip, *.apk, *.aab `
    -ErrorAction SilentlyContinue

# ----------------------------------------------
# Удаляем ненужные каталоги (.gradle, build, .git, .idea)
# ----------------------------------------------
$excludeDirs = @(".gradle", "build", ".git", ".idea")
foreach ($dir in $excludeDirs) {
    Get-ChildItem -Path $tempProject -Directory -Recurse -Force -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -eq $dir } |
        ForEach-Object {
            Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
        }
}

# ----------------------------------------------
# Проверяем, что временная копия создана
# ----------------------------------------------
if (!(Test-Path $tempProject)) {
    Write-Host "❌ ERROR: Temp project folder not found. Copy failed!"
    exit 1
}

# ----------------------------------------------
# Создаем архив
# ----------------------------------------------
Write-Host "Creating ZIP..."
Compress-Archive -Path $tempProject -DestinationPath $backupFileTemp -Force

# ----------------------------------------------
# Проверяем размер и копируем в проект
# ----------------------------------------------
if (Test-Path $backupFileTemp) {
    $fileSizeMB = [Math]::Round((Get-Item $backupFileTemp).Length / 1MB, 2)
    Copy-Item -Path $backupFileTemp -Destination $backupFileFinal -Force
    Write-Host "Backup created: $backupFileFinal (size: $fileSizeMB MB)`n"

    if ($fileSizeMB -gt 20) {
        Write-Host "⚠ WARNING: Backup size exceeds 20 MB!"
        Write-Host "Analyzing largest files in project...`n"

        Get-ChildItem -Path $projectPath -Recurse -File |
            Sort-Object Length -Descending |
            Select-Object FullName, @{Name="SizeMB";Expression={[math]::Round($_.Length/1MB,2)}} -First 10 |
            Format-Table -AutoSize

        Write-Host "`nHint: Check these files or add them to exclusion list."
    }
} else {
    Write-Host "❌ ERROR: Backup ZIP was not created!"
}

# ----------------------------------------------
# Очистка временных файлов
# ----------------------------------------------
Write-Host "`nCleaning up temp folders..."
Remove-Item $tempFolder -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $backupFolder -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "✅ Done. Backup saved to:"
Write-Host $backupFileFinal
# ----------------------------------------------
