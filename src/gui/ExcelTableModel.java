package gui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ExcelTableModel extends AbstractTableModel {

    private List<List<String>> data;
    private List<String> columnNames;

    public ExcelTableModel(List<List<String>> data) {
        this.data = data != null ? data : new ArrayList<>();

        if (this.data.isEmpty()) {
            this.columnNames = new ArrayList<>();
        } else {
            // Assuming the first row is the header for column names in this MVP
            this.columnNames = this.data.get(0);
            this.data.remove(0); // Remove header row from data for simplicity
        }
    }

    public void setData(List<List<String>> data) {
        this.data = data != null ? data : new ArrayList<>();
        if (this.data.isEmpty()) {
            this.columnNames = new ArrayList<>();
        } else {
            this.columnNames = this.data.get(0);
            this.data.remove(0);
        }
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < data.size() && columnIndex >= 0 && columnIndex < data.get(rowIndex).size()) {
            return data.get(rowIndex).get(columnIndex);
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        while (rowIndex >= data.size()) {
            data.add(new ArrayList<>());
        }
        List<String> row = data.get(rowIndex);
        while (columnIndex >= row.size()) {
            row.add("");
        }
        row.set(columnIndex, aValue.toString());
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void addRow(List<String> rowData) {
        data.add(rowData);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void addEmptyRow() {
        List<String> newRow = new ArrayList<>();
        for (int i = 0; i < getColumnCount(); i++) {
            newRow.add("");
        }
        addRow(newRow);
    }

    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < data.size()) {
            data.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public List<List<String>> getData() {
        List<List<String>> dataToSave = new ArrayList<>();
        dataToSave.add(columnNames);
        dataToSave.addAll(data);
        return dataToSave;
    }

    public void addColumn(String columnName) {
        columnNames.add(columnName);
        for (List<String> row : data) {
            row.add("");
        }
        fireTableStructureChanged();
    }
}