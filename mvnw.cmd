@REM ----------------------------------------------------------------------------
@REM MAKAUT CampusSense - Cross-Platform Maven Batch Script (Windows)
@REM ----------------------------------------------------------------------------
@echo off
setlocal EnableDelayedExpansion

set "DIRNAME=%~dp0"
if "%DIRNAME%" == "" set "DIRNAME=."
set "MAVEN_HOME=%DIRNAME%apache-maven-3.9.9"

@REM 1. Auto-detect JAVA_HOME if not already pointing to a valid java.exe
if not exist "%JAVA_HOME%\bin\java.exe" (
    if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    ) else if exist "C:\Program Files\Java\jdk-24\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-24"
    ) else if exist "C:\Program Files\Java\jdk-22\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-22"
    ) else if exist "C:\Program Files\Java\jdk-23\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-23"
    ) else (
        for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        if not defined JAVA_HOME for /d %%D in ("C:\Program Files\Microsoft\jdk-21*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        if not defined JAVA_HOME for /d %%D in ("C:\Program Files\Amazon Corretto\jdk21*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        if not defined JAVA_HOME for /d %%D in ("C:\Program Files\Zulu\zulu-21*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        if not defined JAVA_HOME for /d %%D in ("C:\Program Files\BellSoft\LibericaJDK-21*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

@REM 2. Execute Maven (bundled apache-maven-3.9.9 or global mvn)
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
    set "EXIT_CODE=!ERRORLEVEL!"
) else (
    where mvn >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        call mvn %*
        set "EXIT_CODE=!ERRORLEVEL!"
    ) else (
        echo [ERROR] Maven not found! Neither bundled apache-maven-3.9.9 nor global mvn was located.
        echo Please ensure Java 21+ and Maven are installed and on your PATH.
        set "EXIT_CODE=1"
    )
)

exit /b !EXIT_CODE!
