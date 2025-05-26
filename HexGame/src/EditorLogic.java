import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Класс, управляющий данными и состоянием редактируемого уровня в Hexcells.
 */
public class EditorLogic {
    private LevelConfig currentLevelConfig; // Текущая конфигурация уровня
    private String currentTool; // Текущий инструмент редактирования
    private List<HexCoord> selectedCells; // Выбранные ячейки

    /**
     * Конструктор, инициализирующий пустой уровень.
     */
    public EditorLogic() {
        // Создаем пустой уровень 5x5
        this.currentLevelConfig = new LevelConfig(5, 5, new ArrayList<>(), new ArrayList<>());
        this.currentTool = "SELECT"; // Инструмент по умолчанию
        this.selectedCells = new ArrayList<>();
    }

    /**
     * Возвращает текущую конфигурацию уровня.
     * @return LevelConfig
     */
    public LevelConfig getLevelConfig() {
        return currentLevelConfig;
    }

    /**
     * Переключает состояние мины в указанной ячейке.
     * @param coord Координаты ячейки
     * @throws IllegalArgumentException если координаты вне сетки
     */
    public void toggleMine(HexCoord coord) {
        if (coord == null || coord.getQ() < 0 || coord.getQ() >= currentLevelConfig.getCols() ||
                coord.getR() < 0 || coord.getR() >= currentLevelConfig.getRows()) {
            throw new IllegalArgumentException("Недопустимые координаты: " + coord);
        }

        List<HexCoord> mines = new ArrayList<>(currentLevelConfig.getMines());
        if (mines.contains(coord)) {
            mines.remove(coord);
        } else {
            mines.add(coord);
        }
        currentLevelConfig.setMineCoordinates(mines);
    }

    /**
     * Добавляет новое правило в конфигурацию уровня.
     * @param ruleData Данные правила
     * @throws IllegalArgumentException если ruleData null
     */
    public void addRule(RuleData ruleData) {
        if (ruleData == null) {
            throw new IllegalArgumentException("Данные правила не могут быть null");
        }
        List<RuleData> rules = new ArrayList<>(currentLevelConfig.getRuleDataList());
        rules.add(ruleData);
        currentLevelConfig.setRuleDataList(rules);
    }

    /**
     * Изменяет размеры сетки уровня.
     * @param rows Новое количество строк
     * @param cols Новое количество столбцов
     * @throws IllegalArgumentException если размеры недопустимы
     */
    public void setGridSize(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Размеры сетки должны быть положительными: rows=" + rows + ", cols=" + cols);
        }

        // Проверяем, что мины и правила остаются в пределах новой сетки
        List<HexCoord> newMines = new ArrayList<>();
        for (HexCoord mine : currentLevelConfig.getMines()) {
            if (mine.getQ() < cols && mine.getR() < rows) {
                newMines.add(mine);
            }
        }

        List<RuleData> newRules = new ArrayList<>();
        for (RuleData rule : currentLevelConfig.getRuleDataList()) {
            boolean valid = true;
            if (rule instanceof SequenceRuleData) {
                SequenceRuleData data = (SequenceRuleData) rule;
                for (HexCoord cell : data.getCellsInSequence()) {
                    if (cell.getQ() >= cols || cell.getR() >= rows) {
                        valid = false;
                        break;
                    }
                }
            } else if (rule instanceof GroupRuleData) {
                GroupRuleData data = (GroupRuleData) rule;
                for (HexCoord cell : data.getCellsInGroup()) {
                    if (cell.getQ() >= cols || cell.getR() >= rows) {
                        valid = false;
                        break;
                    }
                }
            } else if (rule instanceof EdgeRuleData) {
                EdgeRuleData data = (EdgeRuleData) rule;
                HexCoord cell = data.getCellCoord();
                if (cell.getQ() >= cols || cell.getR() >= rows) {
                    valid = false;
                }
            }
            if (valid) {
                newRules.add(rule);
            }
        }

        currentLevelConfig = new LevelConfig(rows, cols, newMines, newRules);
        selectedCells.removeIf(cell -> cell.getQ() >= cols || cell.getR() >= rows);
    }

    /**
     * Сохраняет текущий уровень в файл.
     * @param filePath Путь к файлу
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public void saveLevel(String filePath) throws IOException {
        LevelFile.saveLevelToFile(currentLevelConfig, filePath);
    }

    /**
     * Загружает уровень из файла.
     * @param filePath Путь к файлу
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если формат файла некорректен
     */
    public void loadLevel(String filePath) throws IOException {
        currentLevelConfig = LevelFile.loadLevelFromFile(filePath);
        selectedCells.clear(); // Сбрасываем выбор
        currentTool = "SELECT"; // Сбрасываем инструмент
    }

    /**
     * Проверяет, есть ли мина в указанной ячейке.
     * @param coord Координаты ячейки
     * @return true, если в ячейке мина
     */
    public boolean isMine(HexCoord coord) {
        return currentLevelConfig.getMines().contains(coord);
    }

    /**
     * Возвращает список выбранных ячеек.
     * @return Список HexCoord
     */
    public List<HexCoord> getSelectedCells() {
        return new ArrayList<>(selectedCells);
    }

    /**
     * Возвращает текущий инструмент редактирования.
     * @return Название инструмента
     */
    public String getCurrentTool() {
        return currentTool;
    }

    /**
     * Устанавливает текущий инструмент редактирования.
     * @param tool Название инструмента
     */
    public void setCurrentTool(String tool) {
        if (tool == null) {
            throw new IllegalArgumentException("Инструмент не может быть null");
        }
        this.currentTool = tool;
    }

    /**
     * Добавляет или убирает ячейку из выбранных.
     * @param coord Координаты ячейки
     */
    public void toggleSelectedCell(HexCoord coord) {
        if (coord == null || coord.getQ() < 0 || coord.getQ() >= currentLevelConfig.getCols() ||
                coord.getR() < 0 || coord.getR() >= currentLevelConfig.getRows()) {
            return;
        }
        if (selectedCells.contains(coord)) {
            selectedCells.remove(coord);
        } else {
            selectedCells.add(coord);
        }
    }

    /**
     * Возвращает список ячеек для отрисовки.
     * @return Список всех ячеек в сетке
     */
    public List<HexCoord> getCellsToDraw() {
        List<HexCoord> cells = new ArrayList<>();
        for (int r = 0; r < currentLevelConfig.getRows(); r++) {
            for (int q = 0; q < currentLevelConfig.getCols(); q++) {
                cells.add(new HexCoord(q, r));
            }
        }
        return cells;
    }

    /**
     * Возвращает список правил для отрисовки.
     * @return Список RuleData
     */
    public List<RuleData> getRulesToDraw() {
        return new ArrayList<>(currentLevelConfig.getRuleDataList());
    }
}