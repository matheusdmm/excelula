import gui.ExcelEditorFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Opção 2: Look and Feel Metal (padrão cross-platform do Java)
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Opção 3: Look and Feel Nimbus (moderno, cross-platform)
            // Note: Nimbus pode precisar de tratamento especial em alguns casos.
            // for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            // if ("Nimbus".equals(info.getName())) {
            // UIManager.setLookAndFeel(info.getClassName());
            // break;
            // }
            // }

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            // std Metal
            System.err.println("Error at loading theme, using default.");
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ExcelEditorFrame frame = new ExcelEditorFrame();
            frame.setVisible(true);
        });
    }
}