# EXCELULA

This project is an quick editor for Excel file (`.xls` and `.xlsx`) with a Java Swing Graphical User Interface (GUI). It allows loading data from an Excel file, viewing and editing cells (treated as strings), adding/removing rows, adding columns, and saving changes to a new Excel file.

It was created to be a simple tool for the job of ading inputs into a .xlsx database file, since I hate to use Excel i rather use a specific tool that I managed to create 😁.

## Overview

![main interface](/assets/img2.jpg)
![main interface](/assets/img1.jpg)
![main interface](/assets/img3.jpg)

## Dependencies

This project uses the following external libraries:

- **Apache POI:** For reading and writing Excel files. You need to download the full binary distribution of Apache POI and include all necessary JARs (core, OOXML, OOXML-schemas, xmlbeans, commons-collections4, commons-compress, commons-io) in the `lib/` folder.

  - Download link: <https://poi.apache.org/download.html>

**Ensure that all necessary `.jar` files from the Apache POI binary distribution are in the `lib/` folder for successful compilation and execution.**

## Building the Project

To build the project, use the `build.bat` script on windows. This script will compile the `.java` files and create an executable JAR file. If you’re using `Linux/Unix`, you can use the `build.sh/run.sh` scripts.

1. Open a terminal or Command Prompt in the project root directory.

2. Execute the build script:

```bash
.\build.bat
.\build.sh
```

3. If the compilation is successful, an `excelula.jar` file will be created in the project root.

## Running the Application

To run the application, use the `run.bat` script. This script will execute the executable JAR file.

1. Open a terminal or Command Prompt in the project root directory.

2. Execute the run script:

```bash
.\run.bat
.\run.sh
```

3. The GUI application window should be displayed.

Alternatively, you can run the JAR file directly from the terminal, ensuring the `lib` folder is in the same directory:

```bash
java -jar excelula.jar
```

## Features

- Load Excel files (.xls and .xlsx).
- View spreadsheet data in a GUI table.
- Edit cell values (treated as strings).
- Add new empty rows.
- Remove selected rows.
- Add new columns.
- Add rows with details via a popup, filling fields for each column.
- Save changes to a new Excel file (.xlsx).
- Row numbering in the table.
- Alternating row colors for improved readability.
- .xls and .xlsx file filter in the file chooser.

## Known behavior

- if your file has filters or complex headers, it will fail to parse
