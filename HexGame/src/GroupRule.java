import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JPanel;

/**
 * Правило для группы ячеек, определяющее общее количество мин в группе.
 */
public class GroupRule implements Rule {
    private final List<HexCoord> cellsInGroup;
    private final int expectedGroupedMines;

    /**
     * Конструктор.
     * @param cellsInGroup Список ячеек в группе
     * @param expectedGroupedMines Ожидаемое количество мин
     */
    public GroupRule(List<HexCoord> cellsInGroup, int expectedGroupedMines) {
        this.cellsInGroup = List.copyOf(cellsInGroup);
        this.expectedGroupedMines = expectedGroupedMines;
    }

    @Override
    public List<HexCoord> getCells() {
        return cellsInGroup;
    }

    @Override
    public int getExpectedMines() {
        return expectedGroupedMines;
    }

    @Override
    public boolean isSatisfied(Board board) {
        int mineCount = 0;
        for (HexCoord coord : cellsInGroup) {
            Cell cell = board.getCell(coord);
            if (cell != null && cell.isMine()) {
                mineCount++;
            }
        }
        return mineCount == expectedGroupedMines;
    }

    @Override
    public void draw(Graphics2D g2d, JPanel panel, Board board) {
        g2d.setColor(new Color(0, 128, 0)); // Темно-зеленый
        g2d.setStroke(new BasicStroke(2));

        // Отрисовка контура группы
        for (HexCoord coord : cellsInGroup) {
            Point2D.Double center;
            if (panel instanceof GridPanel) {
                GridPanel gridPanel = (GridPanel) panel;
                center = gridPanel.hexToPixel(coord.getQ(), coord.getR());
            } else if (panel instanceof EditorGridPanel) {
                EditorGridPanel editorGridPanel = (EditorGridPanel) panel;
                center = editorGridPanel.hexToPixel(coord.getQ(), coord.getR());
            } else {
                return;
            }

            // Рисуем небольшой круг вокруг ячейки
            g2d.drawOval((int) (center.x - 10), (int) (center.y - 10), 20, 20);
        }

        // Отрисовка числа мин в центре первой ячейки
        if (!cellsInGroup.isEmpty()) {
            Point2D.Double center;
            if (panel instanceof GridPanel) {
                GridPanel gridPanel = (GridPanel) panel;
                center = gridPanel.hexToPixel(cellsInGroup.get(0).getQ(), cellsInGroup.get(0).getR());
            } else if (panel instanceof EditorGridPanel) {
                EditorGridPanel editorGridPanel = (EditorGridPanel) panel;
                center = editorGridPanel.hexToPixel(cellsInGroup.get(0).getQ(), cellsInGroup.get(0).getR());
            } else {
                return;
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(String.valueOf(expectedGroupedMines), (int) center.x - 5, (int) center.y + 5);
        }
    }
}