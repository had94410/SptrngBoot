@echo off
REM restart-app.bat - Stops process on port 8080, builds (if mvn available), and starts the app jar.
REM Usage: double-click or run from project root. Requires PowerShell for the stop step.

SETLOCAL
cd /d "%~dp0"
echo Working directory: %CD%
necho.
echo === Stopping process listening on port 8080 ===
powershell -NoProfile -Command "$p=(Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess; if ($p) { Write-Output ('Found PID: ' + $p); Stop-Process -Id $p -Force; Write-Output 'Stopped.' } else { Write-Output 'No process found on port 8080.' }"
ntimeout /t 2 /nobreak >nul
necho.
echo === Looking for existing jar in target ===
if exist "%CD%\target\sptrngboot-0.0.1-SNAPSHOT.jar" (
    echo Found jar: target\sptrngboot-0.0.1-SNAPSHOT.jar
    echo Starting application...
    start "SptrngBoot" java -jar "%CD%\target\sptrngboot-0.0.1-SNAPSHOT.jar"
    goto :EOF
) else (
    echo No jar found in target.
)
necho.
echo === Checking for mvn ===
where mvn >nul 2>&1
if %ERRORLEVEL%==0 (
    echo Maven found. Building project (skip tests)...
    mvn -DskipTests package
    if exist "%CD%\target\sptrngboot-0.0.1-SNAPSHOT.jar" (
        echo Build succeeded. Starting jar...
        start "SptrngBoot" java -jar "%CD%\target\sptrngboot-0.0.1-SNAPSHOT.jar"
        goto :EOF
    ) else (
        echo Build finished but jar was not found in target.
        pause
        goto :EOF
    )
) else (
    echo Maven (mvn) not found in PATH and no jar present.
    echo Please build the project with your IDE or install Maven, then run this script again.
    pause
    goto :EOF
)

:EOF
ENDLOCAL
exit /b 0
