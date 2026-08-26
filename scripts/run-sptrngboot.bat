@echo off
SETLOCAL
REM Load environment from protected env file if present (created by install-windows-service.ps1)
SET "ENV_FILE=C:\ProgramData\sptrngboot\env"
IF EXIST "%ENV_FILE%" (
  for /f "usebackq tokens=1* delims==" %%A in ("%ENV_FILE%") do (
    set "%%A=%%B"
  )
)
REM Defaults (can be overridden by env file or system env)
IF "%SPRING_PROFILES_ACTIVE%"=="" SET SPRING_PROFILES_ACTIVE=mysql
IF "%DB_URL%"=="" SET DB_URL=jdbc:mysql://localhost:3306/sptrngboot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
IF "%DB_USERNAME%"=="" SET DB_USERNAME=sptrngboot
IF "%DB_PASSWORD%"=="" SET DB_PASSWORD=change-me
IF "%PORT%"=="" SET PORT=8080
IF "%JAVA_OPTS%"=="" SET JAVA_OPTS=-Xms256m -Xmx1024m
cd /d "%~dp0"
"C:\Program Files\Java\jdk-17\bin\java.exe" %JAVA_OPTS% -jar "%~dp0..\target\sptrngboot-0.0.1-SNAPSHOT.jar" --server.port=%PORT%
