#!/bin/bash

CLASSPATH="out:lib/*"

echo "Running..."
java -cp "$CLASSPATH" Main

if [ $? -eq 0 ]; then
  echo "Finished."
else
  echo "Error."
fi
