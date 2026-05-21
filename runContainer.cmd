@echo off
for /f "usebackq delims=" %%i in (`mvnw.cmd -q -DforceStdout help:evaluate "-Dexpression=project.version"`) do set "PROJECT_VERSION=%%i"
echo PROJECT_VERSION=%PROJECT_VERSION%
rem rm -rf app/src/main/container/data-volume
call podman compose -f app/src/main/container/compose.yaml up
pause