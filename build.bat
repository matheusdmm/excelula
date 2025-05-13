    @echo off
    echo Compiling...

    set CLASSPATH=lib/*;src/

    set OUT_DIR=out

    if exist %OUT_DIR% rd /s /q %OUT_DIR%
    mkdir %OUT_DIR%

    javac -cp "%CLASSPATH%" -d %OUT_DIR% ^
    src\excel\ExcelFileManager.java ^
    src\gui\ExcelEditorFrame.java src\gui\ExcelTableModel.java ^
    src\Main.java

    if %errorlevel% neq 0 (
        echo Error during compiling time.
        goto end
    )

    echo Success!
    echo Will create .JAR now...

    set JAR_NAME=excelula.jar

    jar cvfm %JAR_NAME% manifest.txt -C %OUT_DIR% .

    if %errorlevel% neq 0 (
        echo Error during JAR creation.
        goto end
    )

    echo JAR created: %JAR_NAME%

    :end
    echo Proccess finished with success.
    