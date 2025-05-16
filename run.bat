@echo off
echo Maven .jar running...

set ARTIFACT_ID=mth-excelula
set VERSION=1.0-SNAPSHOT
set JAR_FILE=target/%ARTIFACT_ID%-%VERSION%.jar

if not exist %JAR_FILE% (
    echo Error: JAR not present in %JAR_FILE%.
    goto end
)
javaw -jar %JAR_FILE%

:end
echo Process ended.
