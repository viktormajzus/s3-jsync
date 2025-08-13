@echo off
setlocal ENABLEDELAYEDEXPANSION
set TARGET=%~dp0bin
REM trim trailing backslash if any
IF "%TARGET:~-1%"=="\" SET TARGET=%TARGET:~0,-1%

REM read current user PATH
for /f "tokens=2,*" %%A in ('reg query HKCU\Environment /v Path 2^>nul') do set CUR=%%B
if not defined CUR set CUR=

echo %CUR% | find /I "%TARGET%" >nul
if %ERRORLEVEL%==0 (
  echo Already on PATH: %TARGET%
) else (
  if defined CUR (set NEW=%CUR%;%TARGET%) else (set NEW=%TARGET%)
  REM setx truncates >1024 chars on old Windows; this is fine for most users
  setx PATH "%NEW%" >nul
  echo Added to PATH for current user: %TARGET%
  echo Open a new terminal to pick up the change.
)
