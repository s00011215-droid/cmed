@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6
set MAVEN_OPTS=-Xmx1024m

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven 3.9.6...
    if not exist "%USERPROFILE%\.m2\wrapper\dists" mkdir "%USERPROFILE%\.m2\wrapper\dists"
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%USERPROFILE%\.m2\wrapper\dists\maven-3.9.6.zip'"
    powershell -Command "Expand-Archive -Path '%USERPROFILE%\.m2\wrapper\dists\maven-3.9.6.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
)

set PATH=%MAVEN_HOME%\bin;%PATH%
mvn %*
