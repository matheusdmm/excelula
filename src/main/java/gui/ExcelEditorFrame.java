package gui;

import excel.ExcelFileManager;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader; // Importação adicionada
import javax.swing.table.TableRowSorter; // Importação adicionada
import javax.swing.ListCellRenderer; // Importação adicionada

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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

    // Fix: Explicitly get the logger for this class
    // Corrigido: Passando a classe explicitamente para LogManager.getLogger()
    protected static final Logger logger = LogManager.getLogger(ExcelEditorFrame.class);


    public ExcelEditorFrame() {
        super("MTH - ExCel edit v0.3"); // Título atualizado para v0.3
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Configuração do Tamanho da Janela ---
        setSize(1000, 700);
        setMinimumSize(new Dimension(600, 400));

        // Inicializa model com empty data
        tableModel = new ExcelTableModel(new ArrayList<>());
        dataTable = new JTable(tableModel);

        // --- Configuração do Sorting (Ordenação) ---
        // Inicializa o sorter aqui, mas pode ser null se nenhum arquivo for carregado inicialmente
        // O sorter serÃ¡ definido na JTable apÃ³s carregar os dados
        // sorter = new TableRowSorter<>(tableModel); // Removido: Inicializar apÃ³s carregar dados
        // dataTable.setRowSorter(sorter); // Removido: Definir apÃ³s carregar dados


        // --- Configuração da Renderização das Células (Cores Alternadas) ---
        dataTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color evenRowColor = getBackground();
            private final Color oddRowColor = new Color(240, 240, 240);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // A cor alternada deve ser baseada na linha *visual* (após a ordenação)
                // Usamos convertRowIndexToModel para obter o índice da linha no modelo original
                // Verificar se a tabela tem um sorter antes de converter o índice
                int modelRow = row; // Padrão: se não houver sorter, o índice visual é o do modelo
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

                // Desenha os números das linhas visíveis
                Rectangle visibleRect = dataTable.getVisibleRect();
                int firstRow = dataTable.rowAtPoint(visibleRect.getLocation());
                int lastRow = dataTable.rowAtPoint(new Point(visibleRect.x, visibleRect.y + visibleRect.height));

                if (lastRow < 0) { // Caso a tabela esteja vazia ou não visível
                    lastRow = dataTable.getRowCount() - 1;
                } else if (lastRow >= dataTable.getRowCount()) {
                    lastRow = dataTable.getRowCount() - 1;
                }


                for (int row = firstRow; row <= lastRow; row++) {
                    if (row >= 0 && row < dataTable.getRowCount()) {
                        Rectangle rowBounds = dataTable.getCellRect(row, 0, true);
                        // O número da linha deve corresponder ao índice no modelo original
                        // Verificar se a tabela tem um sorter antes de converter o índice
                        int modelRow = row; // Padrão: se não houver sorter, o índice visual é o do modelo
                        if (dataTable.getRowSorter() != null) {
                            modelRow = dataTable.convertRowIndexToModel(row);
                        }
                        String rowNumber = String.valueOf(modelRow + 1);
                        FontMetrics fm = g2d.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(rowNumber)) / 2;
                        int y = rowBounds.y + fm.getAscent() + (rowBounds.height - fm.getHeight()) / 2;

                        // Desenha a cor de fundo alternada na numeração também
                        if (!dataTable.isRowSelected(row)) {
                            // A cor alternada na numeração também deve ser baseada no índice do modelo
                            g2d.setColor(modelRow % 2 == 0 ? UIManager.getColor("Table.background") : new Color(240, 240, 240));
                            g2d.fillRect(0, rowBounds.y, getWidth(), rowBounds.height); // Usar rowBounds.height
                            g2d.setColor(getForeground());
                            g2d.drawString(rowNumber, x, y);
                        } else {
                            // Desenha a cor de seleção se a linha estiver selecionada
                            g2d.setColor(dataTable.getSelectionBackground());
                            g2d.fillRect(0, rowBounds.y, getWidth(), rowBounds.height); // Usar rowBounds.height
                            g2d.setColor(dataTable.getSelectionForeground());
                            g2d.drawString(rowNumber, x, y);
                        }
                    }
                }
            }
        };

        scrollPane.setRowHeaderView(rowHeader);

        // Listeners para repintar o cabeçalho quando a tabela rolar, seleção mudar ou ordenação mudar
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> rowHeader.repaint());
        dataTable.getSelectionModel().addListSelectionListener(e -> rowHeader.repaint());
        dataTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                rowHeader.revalidate();
                rowHeader.repaint();
            }
        });
        // Adicionar listener para repintar o cabeçalho quando a ordenação mudar
        // O listener do sorter serÃ¡ adicionado em loadExcelFile quando o sorter for criado
        // if (dataTable.getRowSorter() != null) {
        //      dataTable.getRowSorter().addRowSorterListener(e -> rowHeader.repaint());
        // }


        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        loadButton = new JButton("Load Excel");
        saveButton = new JButton("Save Excel");
        addRowButton = new JButton("Add Empty Row"); // Renomeado para clareza
        removeRowButton = new JButton("Remove Selected Row");
        addColumnButton = new JButton("Add Column");
        addRowWithDetailsButton = new JButton("Add Row with Details"); // Novo botão

        controlPanel.add(loadButton);
        controlPanel.add(saveButton);
        controlPanel.add(addRowButton);
        controlPanel.add(removeRowButton);
        controlPanel.add(addColumnButton);
        controlPanel.add(addRowWithDetailsButton); // Adiciona o novo botão ao painel

        add(controlPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        loadButton.addActionListener(this::loadExcelFile);
        saveButton.addActionListener(this::saveExcelFile);
        addRowButton.addActionListener(e -> {
            tableModel.addEmptyRow();
            // Ao adicionar/remover linhas, a ordenação pode ser afetada.
            // O sorter geralmente lida com isso, mas repintar o cabeçalho é bom.
            rowHeader.revalidate();
            rowHeader.repaint();
        });
        removeRowButton.addActionListener(this::removeSelectedRow);
        addColumnButton.addActionListener(this::addColumn);
        addRowWithDetailsButton.addActionListener(this::showAddRowPopup); // Listener para o novo botão

        setLocationRelativeTo(null); // Center the window
    }

    // Método para exibir o popup de adição de linha com detalhes
    private void showAddRowPopup(ActionEvent e) {
        // Verifica se há colunas na tabela
        int columnCount = tableModel.getColumnCount();
        if (columnCount == 0) {
            JOptionPane.showMessageDialog(this, "Please load an Excel file first to define columns.", "No Columns", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cria o painel para os inputs
        JPanel inputPanel = new JPanel(new GridLayout(columnCount, 2, 5, 5)); // Layout: linhas=num_colunas, colunas=2 (Label + TextField), hgap, vgap

        List<JTextField> inputFields = new ArrayList<>(); // Lista para armazenar os campos de texto

        // Obtém os nomes das colunas (headers)
        // Nota: Como ExcelTableModel removeu o header da data, pegamos diretamente do modelo da tabela
        Vector<String> columnIdentifiers = new Vector<>();
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            columnIdentifiers.add(dataTable.getColumnName(i));
        }


        // Cria um Label e um TextField para cada coluna
        for (String header : columnIdentifiers) {
            inputPanel.add(new JLabel(header + ":"));
            JTextField textField = new JTextField(20); // Tamanho preferencial do campo de texto
            inputFields.add(textField);
            inputPanel.add(textField);
        }

        // Exibe o popup
        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Enter Row Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Processa o resultado do popup
        if (result == JOptionPane.OK_OPTION) {
            List<String> newRowData = new ArrayList<>();
            for (JTextField textField : inputFields) {
                // Aplica trim() para remover espaços em branco no início e fim
                newRowData.add(textField.getText().trim());
            }

            // Adiciona a nova linha ao modelo da tabela
            tableModel.addRow(newRowData);

            // Atualiza a numeração das linhas e repinta o cabeçalho
            rowHeader.revalidate();
            rowHeader.repaint();

            // Opcional: Seleciona a nova linha adicionada
            int newRowIndex = tableModel.getRowCount() - 1;
            // A seleção e rolagem devem ser feitas no índice da linha *visual*
            int visualRowIndex = dataTable.convertRowIndexToView(newRowIndex);
            dataTable.setRowSelectionInterval(visualRowIndex, visualRowIndex);
            dataTable.scrollRectToVisible(dataTable.getCellRect(visualRowIndex, 0, true)); // Rola para a nova linha

        }
    }


    private void loadExcelFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter fileType = new FileNameExtensionFilter( // Nome da variável atualizado
                "Arquivos Excel (.xls, .xlsx)", "xls", "xlsx");

        fileChooser.addChoosableFileFilter(fileType);
        fileChooser.setFileFilter(fileType);
        fileChooser.setAcceptAllFileFilterUsed(true); // all extensions filter


        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                String fileName = file.getName().toLowerCase(); // Verificação de extensão re-adicionada

                if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
                    JOptionPane.showMessageDialog(this, "Please, select a valid Excel file (.xls OR .xlsx).",
                            "Invalid Format", JOptionPane.WARNING_MESSAGE);
                    return; // Sai do método se o formato for inválido
                }

                java.util.List<java.util.List<String>> data = ExcelFileManager.readExcel(file);
                tableModel.setData(data);
                // Quando os dados mudam, o sorter precisa ser notificado ou recriado.
                // setData no TableModel já chama fireTableStructureChanged, que notifica o sorter.
                // Apenas garantir que o sorter está definido na tabela.
                if (dataTable.getRowSorter() == null) {
                    sorter = new TableRowSorter<>(tableModel);
                    dataTable.setRowSorter(sorter);
                    // Adicionar o listener do sorter novamente se ele for recriado
                    dataTable.getRowSorter().addRowSorterListener(e2 -> rowHeader.repaint());
                } else {
                    // Se o sorter já existe, apenas garantir que ele está associado ao modelo atualizado
                    sorter.setModel(tableModel);
                    // Limpar a ordenação existente ao carregar novos dados para evitar comportamento inesperado
                    sorter.setSortKeys(null);
                }


                rowHeader.revalidate();
                rowHeader.repaint();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                //ex.printStackTrace(); // Comentado para usar o logger
                logger.error("Error loading Excel file", ex); // Usando logger para erros
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
                // Ao salvar, precisamos obter os dados na ordem do modelo original, não da visualização ordenada
                // O getData() do tableModel já retorna os dados na ordem original
                java.util.List<java.util.List<String>> dataToSave = tableModel.getData();
                ExcelFileManager.writeExcel(dataToSave, file);
                JOptionPane.showMessageDialog(this, "File saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                //ex.printStackTrace(); // Comentado para usar o logger
                logger.error("Error saving Excel file", ex); // Usando logger para erros
            }
        }
    }

    private void removeSelectedRow(ActionEvent e) {
        int selectedRow = dataTable.getSelectedRow(); // Índice da linha visual
        if (selectedRow != -1) {
            // Converter o índice da linha visual para o índice no modelo original antes de remover
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
            // Quando colunas são adicionadas, a estrutura do modelo muda.
            // Isso notifica o sorter, mas precisamos garantir que ele ainda está associado.
            if (dataTable.getRowSorter() == null) {
                sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                dataTable.getRowSorter().addRowSorterListener(e2 -> rowHeader.repaint());
            } else {
                sorter.setModel(tableModel);
            }
            rowHeader.revalidate();
            rowHeader.repaint(); // Repintar o cabeçalho caso a largura da tabela mude
        }
    }
}
