@echo off
REM restart-full-fixed.bat - Wrapper to run restart-full.ps1 with PowerShell
REM Place this .bat in the project root (next to restart-full.ps1) and double-click it.

:: Resolve script directory
SET SCRIPT_DIR=%~dp0
:: Call PowerShell to execute the restart script with bypass execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%restart-full.ps1"
echo.
echo Restart script finished. Check output above and the spawned Java window for application logs.
echo Press any key to close...
pause >nul
