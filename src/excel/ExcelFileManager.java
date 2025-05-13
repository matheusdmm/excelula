package excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelFileManager {

    // Reads data from an Excel file, treating all cells as strings.
    public static List<List<String>> readExcel(File file) throws IOException {
        List<List<String>> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) { // Use HSSFWorkbook for .xls

            Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet

            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(formatter.formatCellValue(cell));
                }
                data.add(rowData);
            }
        }
        return data;
    }

    // Writes data to an Excel file.
    public static void writeExcel(List<List<String>> data, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream fos = new FileOutputStream(file)) {

            String sheetName = "Sheet1";

            Sheet sheet = workbook.createSheet(sheetName);

            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j));
                }
            }
            workbook.write(fos);
        }
    }
}