import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий игровую доску Hexcells.
 */
public class Board {
    private final int rows;
    private final int cols;
    private final Cell[][] grid;
    private final List<Rule> activeRules;
    private boolean gameOver;
    private boolean gameWon;

    /**
     * Конструктор, создающий доску заданного размера.
     * @param rows Количество строк
     * @param cols Количество столбцов
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        this.activeRules = new ArrayList<>();
        this.gameOver = false;
        this.gameWon = false;

        // Инициализация ячеек
        for (int r = 0; r < rows; r++) {
            for (int q = 0; q < cols; q++) {
                grid[r][q] = new Cell();
            }
        }
    }

    /**
     * Инициализирует уровень на основе конфигурации.
     * @param config Конфигурация уровня
     */
    public void initializeLevel(LevelConfig config) {
        gameOver = false;
        gameWon = false;
        activeRules.clear();

        // Сбрасываем ячейки
        for (int r = 0; r < rows; r++) {
            for (int q = 0; q < cols; q++) {
                Cell cell = grid[r][q];
                cell.setMine(false);
                cell.setRevealed(false);
                cell.setFlagged(false);
                cell.setRevealedValue(-1);
            }
        }

        // Устанавливаем мины
        for (HexCoord mineCoord : config.getMines()) {
            Cell cell = getCell(mineCoord);
            if (cell != null) {
                cell.setMine(true);
            }
        }

        // Добавляем правила
        for (RuleData ruleData : config.getRuleDataList()) {
            if (ruleData instanceof SequenceRuleData) {
                SequenceRuleData data = (SequenceRuleData) ruleData;
                activeRules.add(new SequenceRule(data.getCellsInSequence(), data.getExpectedConsecutiveMines()));
            } else if (ruleData instanceof GroupRuleData) {
                GroupRuleData data = (GroupRuleData) ruleData;
                activeRules.add(new GroupRule(data.getCellsInGroup(), data.getExpectedGroupedMines()));
            } else if (ruleData instanceof EdgeRuleData) {
                EdgeRuleData data = (EdgeRuleData) ruleData;
                activeRules.add(new EdgeRule(data.getCellCoord(), data.getExpectedNeighborMines()));
            }
        }

        // Рассчитываем revealedValue для всех ячеек
        calculateAllNeighborRules();
    }

    /**
     * Открывает ячейку по координатам.
     * @param coord Координаты ячейки
     * @return true, если ячейка открыта успешно, false, если игра завершена или ячейка уже открыта/флагована
     */
    public boolean revealCell(HexCoord coord) {
        if (gameOver || gameWon) {
            return false;
        }

        Cell cell = getCell(coord);
        if (cell == null || cell.isRevealed() || cell.isFlagged()) {
            return false;
        }

        cell.setRevealed(true);
        if (cell.isMine()) {
            gameOver = true;
            return true;
        }

        // Рассчитываем revealedValue
        int mineCount = 0;
        for (HexCoord neighborCoord : getNeighbors(coord)) {
            Cell neighbor = getCell(neighborCoord);
            if (neighbor != null && neighbor.isMine()) {
                mineCount++;
            }
        }
        cell.setRevealedValue(mineCount);

        // Проверяем условия победы
        checkWinCondition();
        return true;
    }

    /**
     * Устанавливает или снимает флаг на ячейке.
     * @param coord Координаты ячейки
     * @return true, если флаг изменен, false, если ячейка открыта или игра завершена
     */
    public boolean toggleFlag(HexCoord coord) {
        if (gameOver || gameWon) {
            return false;
        }

        Cell cell = getCell(coord);
        if (cell == null || cell.isRevealed()) {
            return false;
        }

        cell.setFlagged(!cell.isFlagged());
        return true;
    }

    /**
     * Возвращает ячейку по координатам.
     * @param coord Координаты
     * @return Ячейка или null, если координаты вне сетки
     */
    public Cell getCell(HexCoord coord) {
        int q = coord.getQ();
        int r = coord.getR();
        if (q >= 0 && q < cols && r >= 0 && r < rows) {
            return grid[r][q];
        }
        return null;
    }

    /**
     * Возвращает соседей ячейки в гексагональной сетке.
     * @param coord Координаты ячейки
     * @return Список координат соседей
     */
    public List<HexCoord> getNeighbors(HexCoord coord) {
        List<HexCoord> neighbors = new ArrayList<>();
        int q = coord.getQ();
        int r = coord.getR();

        // Смещения для соседей в гексагональной сетке (остроконечные гексагоны)
        int[][] offsets = {
                {+1, 0}, {-1, 0}, {0, +1}, {0, -1}, {+1, -1}, {-1, +1}
        };

        for (int[] offset : offsets) {
            int nq = q + offset[0];
            int nr = r + offset[1];
            if (nq >= 0 && nq < cols && nr >= 0 && nr < rows) {
                neighbors.add(new HexCoord(nq, nr));
            }
        }

        return neighbors;
    }

    /**
     * Возвращает список активных правил.
     * @return Список Rule
     */
    public List<Rule> getActiveRules() {
        return new ArrayList<>(activeRules);
    }

    /**
     * Рассчитывает revealedValue для всех ячеек.
     */
    private void calculateAllNeighborRules() {
        for (int r = 0; r < rows; r++) {
            for (int q = 0; q < cols; q++) {
                Cell cell = grid[r][q];
                if (cell.isRevealed() && !cell.isMine()) {
                    int mineCount = 0;
                    for (HexCoord neighborCoord : getNeighbors(new HexCoord(q, r))) {
                        Cell neighbor = getCell(neighborCoord);
                        if (neighbor != null && neighbor.isMine()) {
                            mineCount++;
                        }
                    }
                    cell.setRevealedValue(mineCount);
                }
            }
        }
    }

    /**
     * Проверяет условие победы.
     */
    private void checkWinCondition() {
        // Победа: все мины помечены флагами, а все не-мины открыты
        boolean allMinesFlagged = true;
        boolean allNonMinesRevealed = true;

        for (int r = 0; r < rows; r++) {
            for (int q = 0; q < cols; q++) {
                Cell cell = grid[r][q];
                if (cell.isMine() && !cell.isFlagged()) {
                    allMinesFlagged = false;
                }
                if (!cell.isMine() && !cell.isRevealed()) {
                    allNonMinesRevealed = false;
                }
            }
        }

        if (allMinesFlagged && allNonMinesRevealed) {
            gameWon = true;
            gameOver = true;
        }
    }

    /**
     * Возвращает количество строк.
     * @return rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Возвращает количество столбцов.
     * @return cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * Проверяет, завершена ли игра.
     * @return true, если игра завершена
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Проверяет, выиграна ли игра.
     * @return true, если игра выиграна
     */
    public boolean isGameWon() {
        return gameWon;
    }

    /**
     * Устанавливает состояние завершения игры.
     * @param gameOver Новое состояние
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Устанавливает состояние победы.
     * @param gameWon Новое состояние
     */
    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }
}