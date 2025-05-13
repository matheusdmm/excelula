@echo off
REM Define o classpath para execução: inclui a pasta de classes compiladas (out) e todas as libs
set CLASSPATH=out;lib/*

echo Running...
java -cp "%CLASSPATH%" Main

if %errorlevel% equ 0 (
    echo Finished.
) else (
    echo Error.
)