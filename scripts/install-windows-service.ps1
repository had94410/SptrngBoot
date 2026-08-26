<#
Install the SptrngBoot application as a Windows service.
Run this script as Administrator.
Usage: .\install-windows-service.ps1 -ServiceName sptrngboot -JarPath .\target\sptrngboot-0.0.1-SNAPSHOT.jar -JavaPath 'C:\Program Files\Java\jdk-17\bin\java.exe' -Profile mysql
#>
param(
    [string]$ServiceName = 'sptrngboot',
    [string]$DisplayName = 'SptrngBoot Application',
    [string]$JarPath = "target\sptrngboot-0.0.1-SNAPSHOT.jar",
    [string]$JavaPath = 'C:\Program Files\Java\jdk-17\bin\java.exe',
    [string]$Profile = 'mysql',
    [int]$Port = 8080,
    [string]$DB_URL = 'jdbc:mysql://localhost:3306/sptrngboot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC',
    [string]$DB_USERNAME = 'sptrngboot',
    [string]$DB_PASSWORD = 'change-me',
    [string]$AdminUsername = 'admin',
    [string]$AdminPassword = 'change-me'
)

if (-not ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Error "This script must be run as Administrator."
    exit 1
}

# Helper: convert SecureString to plain text (used only in install flow, not stored by script)
function Convert-SecureStringToPlain([System.Security.SecureString]$secureString) {
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureString)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

# If passwords were not supplied on the command line, prompt the administrator interactively (secure input)
if ($DB_PASSWORD -eq 'change-me' -or [string]::IsNullOrWhiteSpace($DB_PASSWORD)) {
    Write-Host -NoNewline "Enter database password for user '$DB_USERNAME': "
    $dbSec = Read-Host -AsSecureString
    $DB_PASSWORD = Convert-SecureStringToPlain $dbSec
}

if ($AdminPassword -eq 'change-me' -or [string]::IsNullOrWhiteSpace($AdminPassword)) {
    Write-Host -NoNewline "Enter initial admin password to write into env (will be hashed by app on startup): "
    $admSec = Read-Host -AsSecureString
    $AdminPassword = Convert-SecureStringToPlain $admSec
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$jarFull = Resolve-Path -Path (Join-Path $projectRoot $JarPath) -ErrorAction SilentlyContinue
if (-not $jarFull) {
    Write-Error "Jar not found at path: $JarPath (relative to script). Build the project first and re-run."
    exit 1
}
$jarFull = $jarFull.Path

# Ensure a protected env directory exists and write a service-scoped env file
$envDir = 'C:\ProgramData\sptrngboot'
if (-not (Test-Path $envDir)) {
    New-Item -ItemType Directory -Path $envDir -Force | Out-Null
}
$envFile = Join-Path $envDir 'env'
$envContent = @"
SPRING_PROFILES_ACTIVE=$Profile
DB_URL=$DB_URL
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD
APP_ADMIN_USERNAME=$AdminUsername
APP_ADMIN_PASSWORD=$AdminPassword
PORT=$Port
"@

# Write env file (overwrites if exists) and secure ACL so only SYSTEM and Administrators have access
Set-Content -Path $envFile -Value $envContent -Encoding ASCII
$acl = Get-Acl $envFile
$acl.SetAccessRuleProtection($true,$false)
$ruleSys = New-Object System.Security.AccessControl.FileSystemAccessRule("NT AUTHORITY\\SYSTEM","FullControl","Allow")
$ruleAdmin = New-Object System.Security.AccessControl.FileSystemAccessRule("BUILTIN\\Administrators","FullControl","Allow")
# Clear existing rules and add protected ones
$acl.Access | Out-Null
$acl.ResetAccessRule($ruleSys) | Out-Null
$acl.AddAccessRule($ruleSys)
$acl.AddAccessRule($ruleAdmin)
Set-Acl -Path $envFile -AclObject $acl
Write-Host "Wrote protected env file: $envFile"

# Create wrapper batch that sources the env file at runtime (does NOT embed secrets in the script)
$wrapper = Join-Path $projectRoot 'run-sptrngboot.bat'
$batContent = @"
@echo off
SETLOCAL
REM Load environment variables from the protected env file if present
SET "ENV_FILE=C:\ProgramData\sptrngboot\env"
IF EXIST "%ENV_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%ENV_FILE%") do (
    set "%%A=%%B"
  )
)
REM Provide defaults if not set
IF "%PORT%"=="" SET PORT=8080
IF "%JAVA_OPTS%"=="" SET JAVA_OPTS=-Xms256m -Xmx1024m
cd /d "%~dp0"
"$JavaPath" %JAVA_OPTS% -jar "$jarFull" --server.port=%PORT%
"@

Set-Content -Path $wrapper -Value $batContent -Encoding ASCII
Write-Host "Created wrapper batch: $wrapper"

# Compose command for the service to run cmd.exe /c "path\to\run-sptrngboot.bat"
$cmd = "`"C:\\Windows\\System32\\cmd.exe`" /c `"$wrapper`""

# Create or update the service
if (Get-Service -Name $ServiceName -ErrorAction SilentlyContinue) {
    Write-Host "Service $ServiceName already exists. Stopping and removing old service..."
    Stop-Service -Name $ServiceName -Force -ErrorAction SilentlyContinue
    sc.exe delete $ServiceName | Out-Null
    Start-Sleep -Seconds 1
}

New-Service -Name $ServiceName -BinaryPathName $cmd -DisplayName $DisplayName -Description "SptrngBoot Spring Boot application service" -StartupType Automatic
Write-Host "Service $ServiceName created. Starting service..."
Start-Service -Name $ServiceName
Start-Sleep -Seconds 2
Get-Service -Name $ServiceName | Format-List Name,Status,DisplayName
Write-Host "If the service failed to start, check Windows Event Viewer or the project's logs under the application working directory (the wrapper's folder)."

