@REM Maven Wrapper script for Windows
@REM Downloads and runs Maven if not already installed

@echo off
setlocal

set "MAVEN_VERSION=3.9.6"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd"

if exist "%MAVEN_BIN%" goto runMaven

echo Downloading Maven %MAVEN_VERSION%...
if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"

set "DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%TEMP%\maven-%MAVEN_VERSION%.zip'"
powershell -Command "Expand-Archive -Path '%TEMP%\maven-%MAVEN_VERSION%.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
del "%TEMP%\maven-%MAVEN_VERSION%.zip"
echo Maven %MAVEN_VERSION% installed.

:runMaven
set "PATH=%MAVEN_HOME%\bin;%PATH%"
mvn %*
