<#
Uninstall the Windows service created by install-windows-service.ps1
Run as Administrator.
Usage: .\uninstall-windows-service.ps1 -ServiceName sptrngboot
#>
param(
    [string]$ServiceName = 'sptrngboot'
)

if (-not ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Error "This script must be run as Administrator."
    exit 1
}

if (Get-Service -Name $ServiceName -ErrorAction SilentlyContinue) {
    Write-Host "Stopping service $ServiceName..."
    Stop-Service -Name $ServiceName -Force -ErrorAction SilentlyContinue
    Write-Host "Deleting service $ServiceName..."
    sc.exe delete $ServiceName | Out-Null
    Start-Sleep -Seconds 1
    Write-Host "Service removed."
} else {
    Write-Host "Service $ServiceName does not exist."
}

# Optionally remove wrapper batch
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$wrapper = Join-Path $projectRoot 'run-sptrngboot.bat'
if (Test-Path $wrapper) {
    Remove-Item $wrapper -Force
    Write-Host "Removed wrapper: $wrapper"
}
