import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Класс для чтения и записи конфигураций уровней Hexcells из/в файлы.
 */
public class LevelFile {
    /**
     * Загружает конфигурацию уровня из файла.
     * @param filePath Путь к файлу уровня
     * @return Объект LevelConfig
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если формат файла некорректен
     */
    public static LevelConfig loadLevelFromFile(String filePath) throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            throw new IOException("Не удалось прочитать файл: " + filePath, e);
        }

        int lineIndex = 0;
        try {
            // Парсим размеры сетки
            if (lines.size() < 2 || !lines.get(lineIndex).startsWith("ROWS ")) {
                throw new IllegalArgumentException("Ожидается ROWS в первой строке");
            }
            int rows = Integer.parseInt(lines.get(lineIndex++).substring(5).trim());
            if (!lines.get(lineIndex).startsWith("COLS ")) {
                throw new IllegalArgumentException("Ожидается COLS во второй строке");
            }
            int cols = Integer.parseInt(lines.get(lineIndex++).substring(5).trim());
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("Размеры сетки должны быть положительными: rows=" + rows + ", cols=" + cols);
            }

            // Парсим мины
            if (!lines.get(lineIndex).startsWith("MINES ")) {
                throw new IllegalArgumentException("Ожидается MINES после размеров");
            }
            int mineCount = Integer.parseInt(lines.get(lineIndex++).substring(6).trim());
            if (mineCount < 0) {
                throw new IllegalArgumentException("Количество мин не может быть отрицательным: " + mineCount);
            }
            List<HexCoord> mineCoordinates = new ArrayList<>();
            for (int i = 0; i < mineCount; i++, lineIndex++) {
                if (lineIndex >= lines.size()) {
                    throw new IllegalArgumentException("Недостаточно строк для мин");
                }
                String[] coords = lines.get(lineIndex).split(",");
                if (coords.length != 2) {
                    throw new IllegalArgumentException("Неверный формат координат мины: " + lines.get(lineIndex));
                }
                int q = Integer.parseInt(coords[0].trim());
                int r = Integer.parseInt(coords[1].trim());
                if (q < 0 || q >= cols || r < 0 || r >= rows) {
                    throw new IllegalArgumentException("Координаты мины вне сетки: q=" + q + ", r=" + r);
                }
                mineCoordinates.add(new HexCoord(q, r));
            }

            // Парсим правила
            if (!lines.get(lineIndex).startsWith("RULES ")) {
                throw new IllegalArgumentException("Ожидается RULES после мин");
            }
            int ruleCount = Integer.parseInt(lines.get(lineIndex++).substring(6).trim());
            if (ruleCount < 0) {
                throw new IllegalArgumentException("Количество правил не может быть отрицательным: " + ruleCount);
            }
            List<RuleData> ruleDataList = new ArrayList<>();
            for (int i = 0; i < ruleCount; i++, lineIndex++) {
                if (lineIndex >= lines.size()) {
                    throw new IllegalArgumentException("Недостаточно строк для правил");
                }
                String[] parts = lines.get(lineIndex).split("\\s+");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Неверный формат правила: " + lines.get(lineIndex));
                }

                String ruleType = parts[0];
                if (ruleType.equals("SEQUENCE")) {
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Неверный формат SEQUENCE: " + lines.get(lineIndex));
                    }
                    int expectedMines = Integer.parseInt(parts[1]);
                    int cellCount = Integer.parseInt(parts[2]);
                    if (parts.length != 3 + cellCount * 2) {
                        throw new IllegalArgumentException("Неверное количество координат в SEQUENCE: " + lines.get(lineIndex));
                    }
                    List<HexCoord> cells = new ArrayList<>();
                    for (int j = 0; j < cellCount; j++) {
                        int q = Integer.parseInt(parts[3 + j * 2]);
                        int r = Integer.parseInt(parts[4 + j * 2]);
                        if (q < 0 || q >= cols || r < 0 || r >= rows) {
                            throw new IllegalArgumentException("Координаты SEQUENCE вне сетки: q=" + q + ", r=" + r);
                        }
                        cells.add(new HexCoord(q, r));
                    }
                    ruleDataList.add(new SequenceRuleData(cells, expectedMines));
                } else if (ruleType.equals("GROUP")) {
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Неверный формат GROUP: " + lines.get(lineIndex));
                    }
                    int expectedMines = Integer.parseInt(parts[1]);
                    int cellCount = Integer.parseInt(parts[2]);
                    if (parts.length != 3 + cellCount * 2) {
                        throw new IllegalArgumentException("Неверное количество координат в GROUP: " + lines.get(lineIndex));
                    }
                    List<HexCoord> cells = new ArrayList<>();
                    for (int j = 0; j < cellCount; j++) {
                        int q = Integer.parseInt(parts[3 + j * 2]);
                        int r = Integer.parseInt(parts[4 + j * 2]);
                        if (q < 0 || q >= cols || r < 0 || r >= rows) {
                            throw new IllegalArgumentException("Координаты GROUP вне сетки: q=" + q + ", r=" + r);
                        }
                        cells.add(new HexCoord(q, r));
                    }
                    ruleDataList.add(new GroupRuleData(cells, expectedMines));
                } else if (ruleType.equals("EDGE")) {
                    if (parts.length != 4) {
                        throw new IllegalArgumentException("Неверный формат EDGE: " + lines.get(lineIndex));
                    }
                    int expectedMines = Integer.parseInt(parts[1]);
                    int q = Integer.parseInt(parts[2]);
                    int r = Integer.parseInt(parts[3]);
                    if (q < 0 || q >= cols || r < 0 || r >= rows) {
                        throw new IllegalArgumentException("Координаты EDGE вне сетки: q=" + q + ", r=" + r);
                    }
                    ruleDataList.add(new EdgeRuleData(new HexCoord(q, r), expectedMines));
                } else {
                    throw new IllegalArgumentException("Неизвестный тип правила: " + ruleType);
                }
            }

            return new LevelConfig(rows, cols, mineCoordinates, ruleDataList);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка парсинга числовых данных в строке " + (lineIndex + 1), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ошибка формата в строке " + (lineIndex + 1) + ": " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет конфигурацию уровня в файл.
     * @param config Объект LevelConfig
     * @param filePath Путь к файлу
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если config null
     */
    public static void saveLevelToFile(LevelConfig config, String filePath) throws IOException {
        if (config == null) {
            throw new IllegalArgumentException("Конфигурация уровня не может быть null");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            // Записываем размеры
            writer.write("ROWS ");
            writer.write(String.valueOf(config.getRows()));
            writer.newLine();
            writer.write("COLS ");
            writer.write(String.valueOf(config.getCols()));
            writer.newLine();

            // Записываем мины
            List<HexCoord> mines = config.getMines();
            writer.write("MINES ");
            writer.write(String.valueOf(mines.size()));
            writer.newLine();
            for (HexCoord coord : mines) {
                writer.write(coord.getQ() + "," + coord.getR());
                writer.newLine();
            }

            // Записываем правила
            List<RuleData> rules = config.getRuleDataList();
            writer.write("RULES ");
            writer.write(String.valueOf(rules.size()));
            writer.newLine();
            for (RuleData rule : rules) {
                if (rule instanceof SequenceRuleData) {
                    SequenceRuleData data = (SequenceRuleData) rule;
                    List<HexCoord> cells = data.getCellsInSequence();
                    writer.write("SEQUENCE ");
                    writer.write(data.getExpectedConsecutiveMines() + " " + cells.size());
                    for (HexCoord cell : cells) {
                        writer.write(" " + cell.getQ() + "," + cell.getR());
                    }
                    writer.newLine();
                } else if (rule instanceof GroupRuleData) {
                    GroupRuleData data = (GroupRuleData) rule;
                    List<HexCoord> cells = data.getCellsInGroup();
                    writer.write("GROUP ");
                    writer.write(data.getExpectedGroupedMines() + " " + cells.size());
                    for (HexCoord cell : cells) {
                        writer.write(" " + cell.getQ() + "," + cell.getR());
                    }
                    writer.newLine();
                } else if (rule instanceof EdgeRuleData) {
                    EdgeRuleData data = (EdgeRuleData) rule;
                    HexCoord coord = data.getCellCoord();
                    writer.write("EDGE ");
                    writer.write(data.getExpectedNeighborMines() + " " + coord.getQ() + "," + coord.getR());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IOException("Не удалось записать файл: " + filePath, e);
        }
    }
}