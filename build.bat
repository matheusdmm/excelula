@echo off
echo Compiling...

set CLASSPATH=lib/*

javac -cp "%CLASSPATH%" -d out ^
src\excel\ExcelFileManager.java ^
src\gui\ExcelEditorFrame.java src\gui\ExcelTableModel.java ^
src\Main.java

REM Alternativa: compilar todos os arquivos .java recursivamente a partir de src
REM javac -cp "%CLASSPATH%" -d out src/**/*.java


if %errorlevel% equ 0 (
    echo Compilation Done!
) else (
    echo Error at compile time.
)

echo Finished!