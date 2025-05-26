import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.List;

/**
 * Главное окно редактора уровней Hexcells.
 */
public class EditorWindow extends JFrame {
    private final EditorGridPanel editorGridPanel;
    private final EditorLogic editorLogic;
    private JTextField minesInput;
    private String currentRuleType; // Для хранения типа создаваемого правила

    /**
     * Конструктор, создающий окно редактора.
     */
    public EditorWindow() {
        // Настройка окна
        setTitle("Hexcells - Редактор уровней");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Инициализация логики и панели
        editorLogic = new EditorLogic();
        editorGridPanel = new EditorGridPanel(editorLogic);

        // Создание панели инструментов
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);

        // Создание панели управления
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // Добавление EditorGridPanel
        add(editorGridPanel, BorderLayout.CENTER);

        // Добавление MouseListener для EditorGridPanel
        editorGridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HexCoord coord = editorGridPanel.pixelToHex(e.getPoint());
                if (coord != null) {
                    String tool = editorLogic.getCurrentTool();
                    if (tool.equals("MINE")) {
                        editorLogic.toggleMine(coord);
                    } else if (tool.equals("SELECT") || tool.equals("SEQUENCE") ||
                            tool.equals("GROUP") || tool.equals("EDGE")) {
                        editorLogic.toggleSelectedCell(coord);
                    }
                    editorGridPanel.repaint();
                }
            }
        });

        // Упаковка и отображение
        pack();
        setLocationRelativeTo(null); // Центрирование
        setVisible(true);
    }

    /**
     * Создает панель инструментов.
     * @return JToolBar с инструментами
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Группа радиокнопок для инструментов
        ButtonGroup toolGroup = new ButtonGroup();
        JRadioButton mineTool = new JRadioButton("Мина", true);
        JRadioButton selectTool = new JRadioButton("Выбрать");
        JRadioButton sequenceTool = new JRadioButton("Sequence");
        JRadioButton groupTool = new JRadioButton("Group");
        JRadioButton edgeTool = new JRadioButton("Edge");

        // Добавление радиокнопок в группу и панель
        toolGroup.add(mineTool);
        toolGroup.add(selectTool);
        toolGroup.add(sequenceTool);
        toolGroup.add(groupTool);
        toolGroup.add(edgeTool);
        toolBar.add(mineTool);
        toolBar.add(selectTool);
        toolBar.add(sequenceTool);
        toolBar.add(groupTool);
        toolBar.add(edgeTool);

        // ActionListener для инструментов
        mineTool.addActionListener(e -> editorLogic.setCurrentTool("MINE"));
        selectTool.addActionListener(e -> editorLogic.setCurrentTool("SELECT"));
        sequenceTool.addActionListener(e -> {
            editorLogic.setCurrentTool("SEQUENCE");
            currentRuleType = "SEQUENCE";
        });
        groupTool.addActionListener(e -> {
            editorLogic.setCurrentTool("GROUP");
            currentRuleType = "GROUP";
        });
        edgeTool.addActionListener(e -> {
            editorLogic.setCurrentTool("EDGE");
            currentRuleType = "EDGE";
        });

        // Поле ввода количества мин
        toolBar.addSeparator();
        toolBar.add(new JLabel("Мины: "));
        minesInput = new JTextField("0", 5);
        toolBar.add(minesInput);

        // Кнопка создания правила
        JButton createRuleButton = new JButton("Создать правило");
        createRuleButton.addActionListener(e -> createRule());
        toolBar.add(createRuleButton);

        return toolBar;
    }

    /**
     * Создает панель управления.
     * @return JPanel с кнопками управления
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Сохранить");
        JButton testButton = new JButton("Опробовать");
        JButton loadButton = new JButton("Загрузить");
        JButton exitButton = new JButton("Выйти в меню");

        // ActionListener для кнопок
        saveButton.addActionListener(e -> saveLevel());
        testButton.addActionListener(e -> testLevel());
        loadButton.addActionListener(e -> loadLevel());
        exitButton.addActionListener(e -> exitToMenu());

        // Добавление кнопок
        panel.add(saveButton);
        panel.add(testButton);
        panel.add(loadButton);
        panel.add(exitButton);

        return panel;
    }

    /**
     * Создает правило на основе выбранного типа и данных.
     */
    private void createRule() {
        try {
            int expectedMines = Integer.parseInt(minesInput.getText().trim());
            List<HexCoord> selectedCells = editorLogic.getSelectedCells();

            RuleData ruleData = null;
            if (currentRuleType != null) {
                switch (currentRuleType) {
                    case "SEQUENCE":
                        if (selectedCells.isEmpty()) {
                            throw new IllegalArgumentException("Выберите ячейки для Sequence");
                        }
                        ruleData = new SequenceRuleData(selectedCells, expectedMines);
                        break;
                    case "GROUP":
                        if (selectedCells.isEmpty()) {
                            throw new IllegalArgumentException("Выберите ячейки для Group");
                        }
                        ruleData = new GroupRuleData(selectedCells, expectedMines);
                        break;
                    case "EDGE":
                        if (selectedCells.size() != 1) {
                            throw new IllegalArgumentException("Выберите ровно одну ячейку для Edge");
                        }
                        ruleData = new EdgeRuleData(selectedCells.get(0), expectedMines);
                        break;
                    default:
                        throw new IllegalArgumentException("Выберите тип правила");
                }
                editorLogic.addRule(ruleData);
                editorLogic.getSelectedCells().clear(); // Очистка выбора
                editorGridPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Выберите тип правила", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректное число мин", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Сохраняет уровень в файл.
     */
    private void saveLevel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getPath() + ".txt");
            }
            try {
                editorLogic.saveLevel(file.getPath());
                JOptionPane.showMessageDialog(this, "Уровень сохранен: " + file.getPath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Тестирует текущий уровень.
     */
    private void testLevel() {
        try {
            LevelConfig config = editorLogic.getLevelConfig();
            Board board = new Board(config.getRows(), config.getCols());
            board.initializeLevel(config);
            GameWindow testWindow = new GameWindow(board);
            testWindow.setVisible(true);
            setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при запуске уровня: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Загружает уровень из файла.
     */
    private void loadLevel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                editorLogic.loadLevel(fileChooser.getSelectedFile().getPath());
                editorGridPanel.repaint();
                JOptionPane.showMessageDialog(this, "Уровень загружен");
            } catch (IOException | IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Выходит в главное меню.
     */
    private void exitToMenu() {
        dispose();
        new MenuWindow();
    }
}