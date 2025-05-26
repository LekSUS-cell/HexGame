import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JPanel;

/**
 * Правило для последовательности ячеек, определяющее количество последовательных мин.
 */
public class SequenceRule implements Rule {
    private final List<HexCoord> cellsInSequence;
    private final int expectedConsecutiveMines;

    /**
     * Конструктор.
     * @param cellsInSequence Список ячеек в последовательности
     * @param expectedConsecutiveMines Ожидаемое количество последовательных мин
     */
    public SequenceRule(List<HexCoord> cellsInSequence, int expectedConsecutiveMines) {
        this.cellsInSequence = List.copyOf(cellsInSequence);
        this.expectedConsecutiveMines = expectedConsecutiveMines;
    }

    @Override
    public List<HexCoord> getCells() {
        return cellsInSequence;
    }

    @Override
    public int getExpectedMines() {
        return expectedConsecutiveMines;
    }

    @Override
    public boolean isSatisfied(Board board) {
        int maxConsecutiveMines = 0;
        int currentConsecutive = 0;

        for (HexCoord coord : cellsInSequence) {
            Cell cell = board.getCell(coord);
            if (cell != null && cell.isMine()) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 0;
            }
        }

        return maxConsecutive == expectedConsecutiveMines;
    }

    @Override
    public void draw(Graphics2D g2d, JPanel panel, Board board) {
        if (cellsInSequence.size() < 2) {
            return;
        }

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < cellsInSequence.size() - 1; i++) {
            Point2D.Double p1, p2;
            if (panel instanceof GridPanel) {
                GridPanel gridPanel = (GridPanel) panel;
                p1 = gridPanel.hexToPixel(cellsInSequence.get(i).getQ(), cellsInSequence.get(i).getR());
                p2 = gridPanel.hexToPixel(cellsInSequence.get(i + 1).getQ(), cellsInSequence.get(i + 1).getR());
            } else if (panel instanceof EditorGridPanel) {
                EditorGridPanel editorGridPanel = (EditorGridPanel) panel;
                p1 = editorGridPanel.hexToPixel(cellsInSequence.get(i).getQ(), cellsInSequence.get(i).getR());
                p2 = editorGridPanel.hexToPixel(cellsInSequence.get(i + 1).getQ(), cellsInSequence.get(i + 1).getR());
            } else {
                return;
            }

            g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
        }

        // Отрисовка числа мин в центре первой ячейки
        Point2D.Double center;
        if (panel instanceof GridPanel) {
            GridPanel gridPanel = (GridPanel) panel;
            center = gridPanel.hexToPixel(cellsInSequence.get(0).getQ(), cellsInSequence.get(0).getR());
        } else if (panel instanceof EditorGridPanel) {
            EditorGridPanel editorGridPanel = (EditorGridPanel) panel;
            center = editorGridPanel.hexToPixel(cellsInSequence.get(0).getQ(), cellsInSequence.get(0).getR());
        } else {
            return;
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.valueOf(expectedConsecutiveMines), (int) center.x - 5, (int) center.y + 5);
    }
}