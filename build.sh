#!/bin/bash

echo "Compiling..."

CLASSPATH="lib/*:src/"
OUT_DIR="out"

if [ -d "$OUT_DIR" ]; then
  rm -rf "$OUT_DIR"
fi

mkdir "$OUT_DIR"

javac -cp "$CLASSPATH" -d "$OUT_DIR" \
  src/excel/ExcelFileManager.java \
  src/gui/ExcelEditorFrame.java src/gui/ExcelTableModel.java \
  src/Main.java

if [ $? -ne 0 ]; then
  echo "Error during compiling time."
  exit 1
fi

echo "Success!"
echo "Will create .JAR now..."

JAR_NAME="excelula.jar"

jar cvfm "$JAR_NAME" manifest.txt -C "$OUT_DIR" .

if [ $? -ne 0 ]; then
  echo "Error during JAR creation."
  exit 1
fi

echo "JAR created: $JAR_NAME"
echo "Process finished with success."
