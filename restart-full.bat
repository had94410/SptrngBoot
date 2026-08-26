@echo off
REM restart-full.bat - Wrapper to run restart-full.ps1 with PowerShell
REM Place this .bat in the project root (next to restart-full.ps1) and double-click it.
n:: Resolve script directory
SET SCRIPT_DIR=%~dp0n:: Call PowerShell to execute the restart script with bypass execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%restart-full.ps1"necho.necho Restart script finished. Check output above and the spawned Java window for application logs.necho Press any key to close...npause >nul
