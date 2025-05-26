import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Главное окно игры, отображающее гексагональную сетку и интерфейс.
 * Использует остроконечные (pointy-topped) гексагоны.
 */
public class GameWindow extends JFrame {
    private final GridPanel gridPanel; // Панель с гексагональной сеткой
    private final Board gameBoard; // Игровая доска
    private final JLabel statusLabel; // Метка для сообщений игроку
    private final JButton hintButton; // Кнопка для получения подсказки
    private boolean isGameOver; // Флаг, указывающий, закончена ли игра

    /**
     * Конструктор, инициализирующий окно игры.
     *  board Игровая доска
     */
    public GameWindow(Board board) {
        this.gameBoard = board;
        this.isGameOver = false;

        // Настраиваем окно
        setTitle("Hexcells Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Инициализируем панель сетки
        this.gridPanel = new GridPanel(gameBoard);
        add(gridPanel, BorderLayout.CENTER);

        // Инициализируем метку статуса
        this.statusLabel = new JLabel("Игра началась!", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Инициализируем кнопку подсказки
        this.hintButton = new JButton("Подсказка");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(hintButton);
        add(buttonPanel, BorderLayout.NORTH);

        // Добавляем обработчик кликов мыши на gridPanel
        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isGameOver) {
                    return; // Игнорируем клики, если игра завершена
                }

                Point point = e.getPoint();
                HexCoord coord = gridPanel.pixelToHex(point);

                // Проверяем, валидны ли координаты (в пределах доски)
                if (isValidCoord(coord)) {
                    Cell cell = gameBoard.getCell(coord);
                    if (cell != null) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            // Левая кнопка: открываем ячейку
                            gameBoard.revealCell(cell);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            // Правая кнопка: ставим/снимаем флажок
                            gameBoard.toggleFlag(cell);
                        }
                        gridPanel.repaint(); // Перерисовываем сетку
                        checkGameStatus(); // Проверяем состояние игры
                    }
                }
            }
        });

        // Добавляем обработчик для кнопки подсказки
        hintButton.addActionListener(e -> {
            if (!isGameOver) {
                // Предполагается, что Board или HintBot имеет метод getHint()
                String hint = gameBoard.getHint(); // Заменить на реальный метод
                statusLabel.setText(hint != null ? hint : "Подсказок нет!");
                gridPanel.repaint();
            }
        });

        // Упаковываем компоненты и делаем окно видимым
        pack();
        setVisible(true);
    }

    /**
     * Проверяет, находятся ли координаты в пределах доски.
     *  coord Координаты ячейки
     */
    private boolean isValidCoord(HexCoord coord) {
        if (coord == null || gameBoard == null || gameBoard.getGrid() == null) {
            return false;
        }
        int q = coord.getQ();
        int r = coord.getR();
        Cell[][] grid = gameBoard.getGrid();
        return q >= 0 && q < grid.length && r >= 0 && r < grid[q].length;
    }

    /**
     * Проверяет состояние игры (победа или поражение).
     */
    private void checkGameStatus() {
        if (gameBoard.checkWinCondition()) {
            statusLabel.setText("Победа!");
            isGameOver = true;
        } else if (gameBoard.isGameLost()) { // Предполагаемый метод в Board
            statusLabel.setText("Поражение!");
            isGameOver = true;
        }
    }
}