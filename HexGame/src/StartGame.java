import javax.swing.SwingUtilities;

/**
 * Точка входа в приложение Hexcells.
 */
public class StartGame {
    /**
     * Главный метод, запускающий приложение.
     * @param args Аргументы командной строки
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MenuWindow menuWindow = new MenuWindow();
                menuWindow.setVisible(true);
            }
        });
    }
}