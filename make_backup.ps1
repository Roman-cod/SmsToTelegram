# =====================================================================
# PowerShell script: make_backup.ps1
# Purpose: Create a clean project archive (SmsToTelegram_backup.zip)
#          excluding build artifacts, IDE files, and local configs.
#          Also ensures .gitignore and .gitattributes exist.
# =====================================================================

$projectDir = "C:\u\brv\Android_my_prg\SmsToTelegram_windows"
$backupFile = Join-Path $projectDir "SmsToTelegram_backup.zip"

Write-Host "Creating backup for project in: $projectDir"

# ---------------------------------------------------------------------
# 1. Ensure .gitignore exists
# ---------------------------------------------------------------------
$gitignorePath = Join-Path $projectDir ".gitignore"
if (-not (Test-Path $gitignorePath)) {
@"
# Gradle
.gradle/
build/
**/build/
!gradle/wrapper/gradle-wrapper.jar
!gradle/wrapper/gradle-wrapper.properties

# Android Studio
.idea/
*.iml
local.properties

# APKs / keystores / archives
*.apk
*.aab
*.keystore
*.jks
*.zip

# Logs / cache
*.log
captures/
output.json

# OS files
.DS_Store
Thumbs.db
"@ | Out-File -Encoding UTF8 $gitignorePath
    Write-Host "✅ Created .gitignore"
} else {
    Write-Host "ℹ️  .gitignore already exists"
}

# ---------------------------------------------------------------------
# 2. Ensure .gitattributes exists
# ---------------------------------------------------------------------
$gitattributesPath = Join-Path $projectDir ".gitattributes"
if (-not (Test-Path $gitattributesPath)) {
@"
# Normalize line endings
* text=auto

# Force LF for code files
*.kt text eol=lf
*.java text eol=lf
*.gradle text eol=lf
*.xml text eol=lf
*.md text eol=lf
*.sh text eol=lf

# Keep binary files untouched
*.jar binary
*.png binary
*.jpg binary
*.jpeg binary
*.gif binary
*.zip binary
*.aab binary
*.apk binary

# Prevent line-ending conversions in Gradle wrapper jar
gradle/wrapper/gradle-wrapper.jar binary
"@ | Out-File -Encoding UTF8 $gitattributesPath
    Write-Host "✅ Created .gitattributes"
} else {
    Write-Host "ℹ️  .gitattributes already exists"
}

# ---------------------------------------------------------------------
# 3. Build archive excluding unwanted files
# ---------------------------------------------------------------------
Write-Host "📦 Building archive..."
$exclude = @(
    ".git", ".idea", ".gradle", "build", "captures",
    "*.iml", "*.zip", "*.log", "*.apk", "*.aab",
    "local.properties", "SmsToTelegram_backup.zip"
)

if (Test-Path $backupFile) { Remove-Item $backupFile -Force }

# Use built-in Compress-Archive with exclusions
$items = Get-ChildItem -Path $projectDir -Recurse -File | Where-Object {
    $path = $_.FullName
    foreach ($pattern in $exclude) {
        if ($path -like "*\$pattern*") { return $false }
    }
    return $true
}

Compress-Archive -Path $items.FullName -DestinationPath $backupFile -Force

Write-Host "✅ Backup created successfully: $backupFile"
