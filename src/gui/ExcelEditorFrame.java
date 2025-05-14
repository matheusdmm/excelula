package gui;

import excel.ExcelFileManager;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ExcelEditorFrame extends JFrame {

    private JTable dataTable;
    private ExcelTableModel tableModel;
    private JButton loadButton;
    private JButton saveButton;
    private JButton addRowButton;
    private JButton removeRowButton;
    private JButton addColumnButton;
    private JButton addRowWithDetailsButton;
    private JComponent rowHeader;

    public ExcelEditorFrame() {
        super("MTH - ExCel edit v0.2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setSize(1000, 700);
        setMinimumSize(new Dimension(600, 400));

        tableModel = new ExcelTableModel(new ArrayList<>());
        dataTable = new JTable(tableModel);

        dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color evenRowColor = getBackground();
            private final Color oddRowColor = new Color(240, 240, 240);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? evenRowColor : oddRowColor);
                } else {
                    c.setBackground(table.getSelectionBackground());
                }

                setHorizontalAlignment(SwingConstants.LEFT);

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);

        rowHeader = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, dataTable.getPreferredSize().height);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(getForeground());
                g2d.setFont(getFont());

                Rectangle visibleRect = dataTable.getVisibleRect();
                int firstRow = dataTable.rowAtPoint(visibleRect.getLocation());
                int lastRow = dataTable.rowAtPoint(new Point(visibleRect.x, visibleRect.y + visibleRect.height));

                if (lastRow < 0) {
                    lastRow = dataTable.getRowCount() - 1;
                }

                for (int row = firstRow; row <= lastRow; row++) {
                    if (row >= 0 && row < dataTable.getRowCount()) {
                        Rectangle rowBounds = dataTable.getCellRect(row, 0, true);
                        String rowNumber = String.valueOf(row + 1);
                        FontMetrics fm = g2d.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(rowNumber)) / 2;
                        int y = rowBounds.y + fm.getAscent() + (rowBounds.height - fm.getHeight()) / 2;

                        if (!dataTable.isRowSelected(row)) {
                            g2d.setColor(
                                    row % 2 == 0 ? UIManager.getColor("Table.background") : new Color(240, 240, 240));
                            g2d.fillRect(0, rowBounds.y, getWidth(), rowBounds.height);
                            g2d.setColor(getForeground());
                            g2d.drawString(rowNumber, x, y);
                        } else {
                            g2d.setColor(dataTable.getSelectionBackground());
                            g2d.fillRect(0, rowBounds.y, getWidth(), rowBounds.height);
                            g2d.setColor(dataTable.getSelectionForeground());
                            g2d.drawString(rowNumber, x, y);
                        }
                    }
                }
            }
        };

        scrollPane.setRowHeaderView(rowHeader);

        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> rowHeader.repaint());
        dataTable.getSelectionModel().addListSelectionListener(e -> rowHeader.repaint());

        dataTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                rowHeader.revalidate();
                rowHeader.repaint();
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        loadButton = new JButton("Load Excel");
        saveButton = new JButton("Save Excel");
        addRowButton = new JButton("Add Empty Row");
        removeRowButton = new JButton("Remove Selected Row");
        addColumnButton = new JButton("Add Column");
        addRowWithDetailsButton = new JButton("Add Row with Details");

        controlPanel.add(loadButton);
        controlPanel.add(saveButton);
        controlPanel.add(addRowButton);
        controlPanel.add(removeRowButton);
        controlPanel.add(addColumnButton);
        controlPanel.add(addRowWithDetailsButton);

        add(controlPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(this::loadExcelFile);
        saveButton.addActionListener(this::saveExcelFile);
        addRowButton.addActionListener(e -> {
            tableModel.addEmptyRow();
            rowHeader.revalidate();
            rowHeader.repaint();
        });
        removeRowButton.addActionListener(this::removeSelectedRow);
        addColumnButton.addActionListener(this::addColumn);
        addRowWithDetailsButton.addActionListener(this::showAddRowPopup);

        setLocationRelativeTo(null);
    }

    private void showAddRowPopup(ActionEvent e) {
        int columnCount = tableModel.getColumnCount();
        if (columnCount == 0) {
            JOptionPane.showMessageDialog(this, "Please load an Excel file first to define columns.", "No Columns",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel inputPanel = new JPanel(new GridLayout(columnCount, 2, 5, 5));

        List<JTextField> inputFields = new ArrayList<>();

        Vector<String> columnIdentifiers = new Vector<>();
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            columnIdentifiers.add(dataTable.getColumnName(i));
        }

        for (String header : columnIdentifiers) {
            inputPanel.add(new JLabel(header + ":"));
            JTextField textField = new JTextField(20);
            inputFields.add(textField);
            inputPanel.add(textField);
        }

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Enter Row Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            List<String> newRowData = new ArrayList<>();
            for (JTextField textField : inputFields) {
                newRowData.add(textField.getText().trim());
            }

            tableModel.addRow(newRowData);

            rowHeader.revalidate();
            rowHeader.repaint();

            int newRowIndex = tableModel.getRowCount() - 1;
            dataTable.setRowSelectionInterval(newRowIndex, newRowIndex);
            dataTable.scrollRectToVisible(dataTable.getCellRect(newRowIndex, 0, true));
        }
    }

    private void loadExcelFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter fileType = new FileNameExtensionFilter(
                "MS Excel (.xls, .xlsx)", "xls", "xlsx");

        fileChooser.addChoosableFileFilter(fileType);
        fileChooser.setFileFilter(fileType);
        fileChooser.setAcceptAllFileFilterUsed(true);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                String fileName = file.getName().toLowerCase();

                if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
                    JOptionPane.showMessageDialog(this, "Please, select a valid Excel file (.xls OR .xlsx).",
                            "Invalid Format", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                java.util.List<java.util.List<String>> data = ExcelFileManager.readExcel(file);
                tableModel.setData(data);
                rowHeader.revalidate();
                rowHeader.repaint();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void saveExcelFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getParentFile(), file.getName() + ".xlsx");
            }
            try {
                java.util.List<java.util.List<String>> dataToSave = tableModel.getData();
                ExcelFileManager.writeExcel(dataToSave, file);
                JOptionPane.showMessageDialog(this, "File saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void removeSelectedRow(ActionEvent e) {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            rowHeader.revalidate();
            rowHeader.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addColumn(ActionEvent e) {
        String columnName = JOptionPane.showInputDialog(this, "Enter column name:", "Add Column",
                JOptionPane.QUESTION_MESSAGE);
        if (columnName != null && !columnName.trim().isEmpty()) {
            tableModel.addColumn(columnName.trim());
        }
    }
}
