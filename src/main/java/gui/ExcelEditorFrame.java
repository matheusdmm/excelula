package gui;

import excel.ExcelFileManager;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader; // Importação adicionada
import javax.swing.table.TableRowSorter; // Importação adicionada
import javax.swing.ListCellRenderer; // Importação adicionada
import javax.swing.event.DocumentEvent; // Importação adicionada
import javax.swing.event.DocumentListener; // Importação adicionada


import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.PatternSyntaxException; // Importação adicionada

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExcelEditorFrame extends JFrame {

    private JTable dataTable;
    private ExcelTableModel tableModel;
    private JButton loadButton;
    private JButton saveButton;
    private JButton addRowButton; // Botão original para adicionar linha vazia
    private JButton removeRowButton;
    private JButton addColumnButton;
    private JButton addRowWithDetailsButton; // Novo botão para adicionar linha com detalhes
    private JComponent rowHeader;
    private TableRowSorter<ExcelTableModel> sorter; // Declarar o sorter

    // Search function
    private JTextField searchField;
    private JButton clearSearchButton; // Botão para limpar a busca

    protected static final Logger logger = LogManager.getLogger(ExcelEditorFrame.class);

    public ExcelEditorFrame() {
        super("MTH - ExCel edit v0.3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setSize(1000, 700);
        setMinimumSize(new Dimension(600, 400));

        tableModel = new ExcelTableModel(new ArrayList<>());
        dataTable = new JTable(tableModel);

        // sorter = new TableRowSorter<>(tableModel); // Inicialize after loading
        // dataTable.setRowSorter(sorter); // Define after loading

        dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color evenRowColor = getBackground();
            private final Color oddRowColor = new Color(240, 240, 240);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int modelRow = row;
                if (table.getRowSorter() != null) {
                    modelRow = table.convertRowIndexToModel(row);
                }


                if (!isSelected) {
                    c.setBackground(modelRow % 2 == 0 ? evenRowColor : oddRowColor);
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

                // Lines
                Rectangle visibleRect = dataTable.getVisibleRect();
                int firstRow = dataTable.rowAtPoint(visibleRect.getLocation());
                int lastRow = dataTable.rowAtPoint(new Point(visibleRect.x, visibleRect.y + visibleRect.height));

                if (lastRow < 0) { // If empty
                    lastRow = dataTable.getRowCount() - 1;
                } else if (lastRow >= dataTable.getRowCount()) {
                    lastRow = dataTable.getRowCount() - 1;
                }


                for (int row = firstRow; row <= lastRow; row++) {
                    if (row >= 0 && row < dataTable.getRowCount()) {
                        Rectangle rowBounds = dataTable.getCellRect(row, 0, true);
                        int modelRow = row;

                        if (dataTable.getRowSorter() != null) {
                            modelRow = dataTable.convertRowIndexToModel(row);
                        }
                        String rowNumber = String.valueOf(modelRow + 1);
                        FontMetrics fm = g2d.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(rowNumber)) / 2;
                        int y = rowBounds.y + fm.getAscent() + (rowBounds.height - fm.getHeight()) / 2;

                        if (!dataTable.isRowSelected(row)) {
                            g2d.setColor(modelRow % 2 == 0 ? UIManager.getColor("Table.background") : new Color(240, 240, 240));
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

        // Search panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
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

        bottomPanel.add(controlPanel, BorderLayout.WEST);

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        searchField = new JTextField(20);
        clearSearchButton = new JButton("Clear Search");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(clearSearchButton);
        bottomPanel.add(searchPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

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

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
        });

        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            filterTable();
        });


        setLocationRelativeTo(null);
    }

    private void filterTable() {
        String text = searchField.getText();
        if (sorter == null) {
            return;
        }
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                RowFilter<ExcelTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + text);
                sorter.setRowFilter(rowFilter);
            } catch (PatternSyntaxException pse) {
                logger.error("Invalid regex pattern in search field", pse);
                sorter.setRowFilter(null);
            }
        }
        rowHeader.revalidate();
        rowHeader.repaint();
    }


    private void showAddRowPopup(ActionEvent e) {
        int columnCount = tableModel.getColumnCount();
        if (columnCount == 0) {
            JOptionPane.showMessageDialog(this, "Please load an Excel file first to define columns.", "No Columns", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cria o painel para os inputs
        JPanel inputPanel = new JPanel(new GridLayout(columnCount, 2, 5, 5)); // Layout: linhas=num_colunas, colunas=2 (Label + TextField), hgap, vgap
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

        // Exibe o popup
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
            int visualRowIndex = dataTable.convertRowIndexToView(newRowIndex);
            dataTable.setRowSelectionInterval(visualRowIndex, visualRowIndex);
            dataTable.scrollRectToVisible(dataTable.getCellRect(visualRowIndex, 0, true)); // Rola para a nova linha

        }
    }


    private void loadExcelFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter fileType = new FileNameExtensionFilter(
                "Arquivos Excel (.xls, .xlsx)", "xls", "xlsx");

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

                if (dataTable.getRowSorter() == null) {
                    sorter = new TableRowSorter<>(tableModel);
                    dataTable.setRowSorter(sorter);
                    dataTable.getRowSorter().addRowSorterListener(e2 -> rowHeader.repaint());
                } else {
                    sorter.setModel(tableModel);
                    sorter.setSortKeys(null);
                }

                rowHeader.revalidate();
                rowHeader.repaint();
                searchField.setText("");
                filterTable();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                //ex.printStackTrace();
                logger.error("Error loading Excel file", ex);
            }
        }
    }

    private void saveExcelFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure file has a .xlsx extension if not provided
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
                //ex.printStackTrace();
                logger.error("Error saving Excel file", ex);
            }
        }
    }

    private void removeSelectedRow(ActionEvent e) {
        int selectedRow = dataTable.getSelectedRow(); // Índice da linha visual

        if (selectedRow != -1) {
            int modelRow = dataTable.convertRowIndexToModel(selectedRow);
            tableModel.removeRow(modelRow);

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

            if (dataTable.getRowSorter() == null) {
                sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                dataTable.getRowSorter().addRowSorterListener(e2 -> rowHeader.repaint());
            } else {
                sorter.setModel(tableModel);
            }
            rowHeader.revalidate();
            rowHeader.repaint();
        }
    }
}
