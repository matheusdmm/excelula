package excel;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelFileManager {
    // Handles both .xls and .xlsx formats using WorkbookFactory.
    public static List<List<String>> readExcel(File file) throws IOException {
        List<List<String>> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet

            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(formatter.formatCellValue(cell)); // Get cell value as string
                }
                data.add(rowData);
            }
        }
        return data;
    }

    public static void writeExcel(List<List<String>> data, File file) throws IOException {
        // will save only as .xlsx
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("Sheet1");

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
