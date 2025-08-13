# Requires WiX v3 (candle.exe, light.exe) on PATH
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command light.exe  -ErrorAction SilentlyContinue)) {
    throw "WiX v3 not found. Install v3.14 (x86) and add its bin to PATH."
}

.\gradlew.bat --no-daemon clean jpackage -Ppkg=msi

$msi = Get-ChildItem build\jpackage -Filter *.msi | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $msi) { throw "MSI not found in build\jpackage" }
Write-Host "Built: $($msi.FullName)"
Write-Host "Install by double-clicking it or with: msiexec /i `"$($msi.FullName)`""
