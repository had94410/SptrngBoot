Set-StrictMode -Version Latest

# restart-full.ps1 - Stops process on port 8080, builds the project (using mvn or a temporary Maven), and starts the newest jar.
# Place this file in the project root and run from there (or use the provided .bat wrapper).

# Use the script directory as the project root
$projectRoot = $PSScriptRoot
Set-Location $projectRoot
Write-Host "Working directory: $projectRoot`n"

# 1) Stop process listening on port 8080 (if any)
$port = 8080
$ntc = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($ntc) {
    $ownPid = ($ntc | Select-Object -First 1).OwningProcess
    $ownPid = [int]$ownPid
    if ($ownPid -gt 0) {
        Write-Host "Process found listening on port $port : PID=$ownPid" -ForegroundColor Yellow
        try {
            $procInfo = Get-CimInstance Win32_Process -Filter "ProcessId=$ownPid" -ErrorAction SilentlyContinue | Select-Object ProcessId, CommandLine
            if ($procInfo) {
                Write-Host "CommandLine: $($procInfo.CommandLine)" -ForegroundColor Gray
            }
        } catch {
            Write-Host ("Could not query process info for PID " + $ownPid + ": " + $_) -ForegroundColor Red
        }

        $confirm = Read-Host "Stop this process? (y/N)"
        if ($confirm -match '^[Yy]') {
            try {
                Stop-Process -Id $ownPid -Force -ErrorAction Stop
                Write-Host "Stopped PID $ownPid" -ForegroundColor Green
            } catch {
                Write-Host ("Failed to stop PID " + $ownPid + ": " + $_) -ForegroundColor Red
                exit 1
            }
        } else {
            Write-Host "Aborting per user request." -ForegroundColor Yellow
            exit 1
        }
    } else {
        Write-Host "No owning process found for port $port (OwningProcess=$ownPid). Skipping stop step." -ForegroundColor Green
    }
} else {
    Write-Host "No process found listening on port $port" -ForegroundColor Green
}

Start-Sleep -Seconds 2

# 2) Build
function Run-MavenPackage {
    param($projectPath)
    Push-Location $projectPath
    try {
        Write-Host "Running: mvn -DskipTests package" -ForegroundColor Cyan
        & mvn -DskipTests package
        return $LASTEXITCODE
    } finally {
        Pop-Location
    }
}

$cmd = Get-Command mvn -ErrorAction SilentlyContinue
$mvnPath = $null
if ($cmd -and $cmd.Path) { $mvnPath = $cmd.Path }
if ($mvnPath) {
    Write-Host "Maven found at: $mvnPath" -ForegroundColor Green
    $rc = Run-MavenPackage $projectRoot
    if ($rc -ne 0) { Write-Host "mvn package failed (exit $rc)" -ForegroundColor Red; exit 1 }
} else {
    Write-Host "Maven not found in PATH. Attempting to download a local Maven to user profile and use it for this session." -ForegroundColor Yellow
    $userTools = Join-Path $env:USERPROFILE 'tools'
    $mvnDir = Join-Path $userTools 'apache-maven'
    $ver = '3.9.8'
    $zip = "apache-maven-$ver-bin.zip"
    $dlcdn = "https://dlcdn.apache.org/maven/maven-3/$ver/binaries/$zip"
    $archive = "https://archive.apache.org/dist/maven/maven-3/$ver/binaries/$zip"
    $apache = "https://www.apache.org/dyn/closer.lua/maven/maven-3/$ver/binaries/$zip"

    if (-Not (Test-Path $userTools)) { New-Item -ItemType Directory -Path $userTools | Out-Null }

    Push-Location $userTools
    if (-Not (Test-Path (Join-Path $userTools $zip))) {
        Write-Host "Attempting to download Maven $ver..." -ForegroundColor Cyan
        $candidates = @($dlcdn, $archive, $apache)
        $downloaded = $false
        foreach ($u in $candidates) {
            Write-Host "Trying: $u" -ForegroundColor Gray
            try {
                Invoke-WebRequest -Uri $u -OutFile $zip -UseBasicParsing -ErrorAction Stop
                Write-Host "Downloaded from: $u" -ForegroundColor Green
                $downloaded = $true
                break
            } catch {
                Write-Host ("Download failed from " + $u + ": " + $_.Exception.Message) -ForegroundColor Yellow
            }
        }
        if (-not $downloaded) {
            Write-Host "Failed to download Maven from known mirrors. Please install Maven locally and add 'mvn' to PATH, or download $zip manually into $userTools." -ForegroundColor Red
            Pop-Location
            exit 1
        }
    } else {
        Write-Host "Maven zip already downloaded." -ForegroundColor Gray
    }

    Write-Host "Extracting..." -ForegroundColor Cyan
    try {
        Expand-Archive -Path $zip -DestinationPath $userTools -Force
    } catch {
        Write-Host ("Expand-Archive failed: " + $_) -ForegroundColor Red
        Pop-Location
        exit 1
    }
    $extracted = Join-Path $userTools "apache-maven-$ver"
    if (Test-Path $extracted) {
        if (Test-Path $mvnDir) { Remove-Item $mvnDir -Recurse -Force -ErrorAction SilentlyContinue }
        Rename-Item -Path $extracted -NewName 'apache-maven' -ErrorAction SilentlyContinue
    }
    $env:PATH = $env:PATH + ';' + (Join-Path $mvnDir 'bin')
    Write-Host "Using Maven at: $mvnDir" -ForegroundColor Green

    $rc = Run-MavenPackage $projectRoot
    if ($rc -ne 0) { Write-Host "mvn package failed (exit $rc)" -ForegroundColor Red; Pop-Location; exit 1 }
    Pop-Location
}

# 3) Find jar and start
$jar = Get-ChildItem -Path (Join-Path $projectRoot 'target\*.jar') -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) {
    Write-Host "No jar found in target after build. Aborting." -ForegroundColor Red
    exit 1
}
Write-Host "Found jar: $($jar.FullName)" -ForegroundColor Green

Write-Host "Starting application in a new window..." -ForegroundColor Cyan
Start-Process -FilePath 'java' -ArgumentList '-jar', $jar.FullName -WorkingDirectory $projectRoot -WindowStyle Normal

Write-Host "Start command issued. Check the new window for application logs." -ForegroundColor Green
