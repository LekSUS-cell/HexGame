import java.util.List;

/**
 * Перечисление для типа подсказки.
 */
enum HintType {
    MINE, // Ячейка содержит мину
    SAFE  // Ячейка безопасна
}

/**
 * Класс для хранения результата подсказки.
 */
class HintResult {
    private final HexCoord coord;
    private final HintType type;

    public HintResult(HexCoord coord, HintType type) {
        this.coord = coord;
        this.type = type;
    }

    public HexCoord getCoord() {
        return coord;
    }

    public HintType getType() {
        return type;
    }
}

/**
 * Класс для анализа состояния Board и предоставления подсказок.
 */
public class HintBot {
    /**
     * Находит подсказку на основе текущего состояния доски.
     * @param board Текущая доска
     * @return HintResult с координатами и типом подсказки, или null, если подсказка не найдена
     */
    public static HintResult findHint(Board board) {
        if (board == null) {
            return null;
        }

        // Этап 1: Простая логика "Сапёра"
        HintResult mineSweeperHint = findMineSweeperHint(board);
        if (mineSweeperHint != null) {
            return mineSweeperHint;
        }

        // Этап 2: Анализ правил
        HintResult ruleHint = findRuleHint(board);
        return ruleHint;
    }

    /**
     * Проверяет открытые ячейки для простой логики "Сапёра".
     */
    private static HintResult findMineSweeperHint(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int q = 0; q < board.getCols(); q++) {
                Cell cell = board.getCell(new HexCoord(q, r));
                if (cell != null && cell.isRevealed() && !cell.isMine() && cell.getRevealedValue() > 0) {
                    List<HexCoord> neighbors = board.getNeighbors(new HexCoord(q, r));
                    int countFlagged = 0;
                    int countHiddenUnflagged = 0;
                    List<HexCoord> hiddenUnflaggedNeighbors = new java.util.ArrayList<>();

                    // Подсчет флагов и скрытых не-флагованных соседей
                    for (HexCoord neighborCoord : neighbors) {
                        Cell neighbor = board.getCell(neighborCoord);
                        if (neighbor != null) {
                            if (neighbor.isFlagged()) {
                                countFlagged++;
                            } else if (!neighbor.isRevealed()) {
                                countHiddenUnflagged++;
                                hiddenUnflaggedNeighbors.add(neighborCoord);
                            }
                        }
                    }

                    int revealedValue = cell.getRevealedValue();
                    // Случай 1: Все оставшиеся соседи безопасны
                    if (revealedValue == countFlagged && !hiddenUnflaggedNeighbors.isEmpty()) {
                        return new HintResult(hiddenUnflaggedNeighbors.get(0), HintType.SAFE);
                    }
                    // Случай 2: Все оставшиеся соседи — мины
                    if (revealedValue == countFlagged + countHiddenUnflagged && !hiddenUnflaggedNeighbors.isEmpty()) {
                        return new HintResult(hiddenUnflaggedNeighbors.get(0), HintType.MINE);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Анализирует активные правила для поиска подсказки.
     */
    private static HintResult findRuleHint(Board board) {
        for (Rule rule : board.getActiveRules()) {
            if (rule instanceof SequenceRule) {
                HintResult sequenceHint = analyzeSequenceRule((SequenceRule) rule, board);
                if (sequenceHint != null) {
                    return sequenceHint;
                }
            } else if (rule instanceof GroupRule) {
                HintResult groupHint = analyzeGroupRule((GroupRule) rule, board);
                if (groupHint != null) {
                    return groupHint;
                }
            } else if (rule instanceof EdgeRule) {
                HintResult edgeHint = analyzeEdgeRule((EdgeRule) rule, board);
                if (edgeHint != null) {
                    return edgeHint;
                }
            }
        }
        return null;
    }

    /**
     * Анализирует SequenceRule для подсказки.
     */
    private static HintResult analyzeSequenceRule(SequenceRule rule, Board board) {
        List<HexCoord> cells = rule.getCells();
        int expectedMines = rule.getExpectedMines();
        int countMinesOrFlagged = 0;
        int countHiddenUnflagged = 0;
        List<HexCoord> hiddenUnflaggedCells = new java.util.ArrayList<>();

        // Подсчет мин/флагов и скрытых ячеек
        for (HexCoord coord : cells) {
            Cell cell = board.getCell(coord);
            if (cell != null) {
                if (cell.isMine() || cell.isFlagged()) {
                    countMinesOrFlagged++;
                } else if (!cell.isRevealed()) {
                    countHiddenUnflagged++;
                    hiddenUnflaggedCells.add(coord);
                }
            }
        }

        // Случай 1: Все требуемые мины найдены, остальные безопасны
        if (countMinesOrFlagged == expectedMines && !hiddenUnflaggedCells.isEmpty()) {
            return new HintResult(hiddenUnflaggedCells.get(0), HintType.SAFE);
        }
        // Случай 2: Все скрытые ячейки — мины
        if (countMinesOrFlagged + countHiddenUnflagged == expectedMines && !hiddenUnflaggedCells.isEmpty()) {
            return new HintResult(hiddenUnflaggedCells.get(0), HintType.MINE);
        }
        return null;
    }

    /**
     * Анализирует GroupRule для подсказки.
     */
    private static HintResult analyzeGroupRule(GroupRule rule, Board board) {
        List<HexCoord> cells = rule.getCells();
        int expectedMines = rule.getExpectedMines();
        int countMinesOrFlagged = 0;
        int countHiddenUnflagged = 0;
        List<HexCoord> hiddenUnflaggedCells = new java.util.ArrayList<>();

        // Подсчет мин/флагов и скрытых ячеек
        for (HexCoord coord : cells) {
            Cell cell = board.getCell(coord);
            if (cell != null) {
                if (cell.isMine() || cell.isFlagged()) {
                    countMinesOrFlagged++;
                } else if (!cell.isRevealed()) {
                    countHiddenUnflagged++;
                    hiddenUnflaggedCells.add(coord);
                }
            }
        }

        // Случай 1: Все требуемые мины найдены, остальные безопасны
        if (countMinesOrFlagged == expectedMines && !hiddenUnflaggedCells.isEmpty()) {
            return new HintResult(hiddenUnflaggedCells.get(0), HintType.SAFE);
        }
        // Случай 2: Все скрытые ячейки — мины
        if (countMinesOrFlagged + countHiddenUnflagged == expectedMines && !hiddenUnflaggedCells.isEmpty()) {
            return new HintResult(hiddenUnflaggedCells.get(0), HintType.MINE);
        }
        return null;
    }

    /**
     * Анализирует EdgeRule для подсказки.
     */
    private static HintResult analyzeEdgeRule(EdgeRule rule, Board board) {
        HexCoord cellCoord = rule.getCell();
        int expectedMines = rule.getExpectedMines();
        List<HexCoord> neighbors = board.getNeighbors(cellCoord);
        int countMinesOrFlagged = 0;
        int countHiddenUnflagged = 0;
        List<HexCoord> hiddenUnflaggedNeighbors = new java.util.ArrayList<>();

        // Подсчет мин/флагов и скрытых соседей
        for (HexCoord neighborCoord : neighbors) {
            Cell neighbor = board.getCell(neighborCoord);
            if (neighbor != null) {
                if (neighbor.isMine() || neighbor.isFlagged()) {
                    countMinesOrFlagged++;
                } else if (!neighbor.isRevealed()) {
                    countHiddenUnflagged++;
                    hiddenUnflaggedNeighbors.add(neighborCoord);
                }
            }
        }

        // Случай 1: Все требуемые мины найдены, остальные безопасны
        if (countMinesOrFlagged == expectedMines && !hiddenUnflaggedNeighbors.isEmpty()) {
            return new HintResult(hiddenUnflaggedNeighbors.get(0), HintType.SAFE);
        }
        // Случай 2: Все скрытые соседи — мины
        if (countMinesOrFlagged + countHiddenUnflagged == expectedMines && !hiddenUnflaggedNeighbors.isEmpty()) {
            return new HintResult(hiddenUnflaggedNeighbors.get(0), HintType.MINE);
        }
        return null;
    }
}