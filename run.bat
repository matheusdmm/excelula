@echo off

set CLASSPATH=out;lib/*

echo Running...
java -cp "%CLASSPATH%" Main

if %errorlevel% equ 0 (
    echo Finished.
) else (
    echo Error.
)