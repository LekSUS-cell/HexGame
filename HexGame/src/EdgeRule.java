import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JPanel;

/**
 * Правило для ячейки, определяющее количество мин среди её соседей.
 */
public class EdgeRule implements Rule {
    private final HexCoord cellCoord;
    private final int expectedMines;

    /**
     * Конструктор.
     * @param cellCoord Координаты ячейки
     * @param expectedMines Ожидаемое количество мин среди соседей
     */
    public EdgeRule(HexCoord cellCoord, int expectedMines) {
        this.cellCoord = cellCoord;
        this.expectedMines = expectedMines;
    }

    @Override
    public List<HexCoord> getCells() {
        return List.of(cellCoord);
    }

    @Override
    public int getExpectedMines() {
        return expectedMines;
    }

    @Override
    public boolean isSatisfied(Board board) {
        int mineCount = 0;
        List<HexCoord> neighbors = board.getNeighbors(cellCoord); // Исправлено: используем cellCoord
        for (HexCoord neighborCoord : neighbors) {
            Cell neighborCell = board.getCell(neighborCoord); // Получаем Cell для каждой координаты
            if (neighborCell != null && neighborCell.isMine()) {
                mineCount++;
            }
        }
        return mineCount == expectedMines;
    }

    @Override
    public void draw(Graphics2D g2d, JPanel panel, Board board) {
        Point2D.Double center;
        if (panel instanceof GridPanel) {
            GridPanel gridPanel = (GridPanel) panel;
            center = gridPanel.hexToPixel(cellCoord.getQ(), cellCoord.getR());
        } else if (panel instanceof EditorGridPanel) {
            EditorGridPanel editorGridPanel = (EditorGridPanel) panel;
            center = editorGridPanel.hexToPixel(cellCoord.getQ(), cellCoord.getR());
        } else {
            return;
        }

        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.valueOf(expectedMines), (int) center.x - 5, (int) center.y + 5);
    }
}