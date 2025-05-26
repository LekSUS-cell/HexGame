import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import java.util.List;

/**
 * Панель для визуального редактирования уровня в Hexcells.
 * Отображает гексагональную сетку, мины, правила и подсвечивает выбранные ячейки.
 */
public class EditorGridPanel extends JPanel {
    private final EditorLogic editorLogic; // Логика редактора
    private static final int HEX_RADIUS = 30; // Радиус гексагона

    /**
     * Конструктор, инициализирующий панель.
     * @param editorLogic Объект логики редактора
     */
    public EditorGridPanel(EditorLogic editorLogic) {
        this.editorLogic = editorLogic;
        setBackground(Color.LIGHT_GRAY);

        // Установка предпочтительного размера на основе размеров сетки
        LevelConfig config = editorLogic.getLevelConfig();
        int width = (int) (config.getCols() * HEX_RADIUS * 1.5 + HEX_RADIUS);
        int height = (int) (config.getRows() * HEX_RADIUS * Math.sqrt(3) + HEX_RADIUS);
        setPreferredSize(new Dimension(width, height));
    }

    /**
     * Отрисовывает компонент: сетку гексагонов, мины, правила и выбранные ячейки.
     * @param g Графический контекст
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LevelConfig config = editorLogic.getLevelConfig();
        int rows = config.getRows();
        int cols = config.getCols();

        // Отрисовка гексагонов
        for (int r = 0; r < rows; r++) {
            for (int q = 0; q < cols; q++) {
                HexCoord coord = new HexCoord(q, r);
                Point2D.Double center = hexToPixel(q, r);

                // Рисуем гексагон
                Polygon hex = createHexagon(center, HEX_RADIUS);
                g2d.setColor(Color.GRAY);
                g2d.fillPolygon(hex);
                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(hex);

                // Рисуем мину, если она есть
                if (editorLogic.isMine(coord)) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval((int) (center.x - HEX_RADIUS / 2), (int) (center.y - HEX_RADIUS / 2),
                            HEX_RADIUS, HEX_RADIUS);
                }
            }
        }

        // Подсветка выбранных ячеек
        List<HexCoord> selectedCells = editorLogic.getSelectedCells();
        for (HexCoord coord : selectedCells) {
            Point2D.Double center = hexToPixel(coord.getQ(), coord.getR());
            Polygon hex = createHexagon(center, HEX_RADIUS);
            g2d.setColor(new Color(255, 255, 0, 100)); // Полупрозрачный желтый
            g2d.fillPolygon(hex);
            g2d.setColor(Color.YELLOW);
            g2d.drawPolygon(hex);
        }

        // Отрисовка правил
        for (RuleData ruleData : config.getRuleDataList()) {
            Rule rule = createRuleFromData(ruleData);
            if (rule != null) {
                rule.draw(g2d, this, null); // Board не нужен, используем null
            }
        }

        // Визуализация текущего инструмента (пример)
        String tool = editorLogic.getCurrentTool();
        if (tool.equals("SEQUENCE") && selectedCells.size() > 1) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i < selectedCells.size() - 1; i++) {
                Point2D.Double p1 = hexToPixel(selectedCells.get(i).getQ(), selectedCells.get(i).getR());
                Point2D.Double p2 = hexToPixel(selectedCells.get(i + 1).getQ(), selectedCells.get(i + 1).getR());
                g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        }
    }

    /**
     * Преобразует пиксельные координаты в координаты гексагона.
     * @param pixelPoint Точка в пикселях
     * @return Координаты HexCoord или null, если точка вне сетки
     */
    public HexCoord pixelToHex(Point pixelPoint) {
        double x = pixelPoint.x;
        double y = pixelPoint.y;

        // Преобразование в кубические координаты
        double q = (2.0 / 3 * x) / HEX_RADIUS;
        double r = (-x / 3 + Math.sqrt(3) / 3 * y) / HEX_RADIUS;

        // Округление кубических координат
        double z = -q - r;
        int rq = (int) Math.round(q);
        int rr = (int) Math.round(r);
        int rz = (int) Math.round(z);

        double qDiff = Math.abs(rq - q);
        double rDiff = Math.abs(rr - r);
        double zDiff = Math.abs(rz - z);

        if (qDiff > rDiff && qDiff > zDiff) {
            rq = -rr - rz;
        } else if (rDiff > zDiff) {
            rr = -rq - rz;
        }

        // Проверка, находится ли координата в пределах сетки
        LevelConfig config = editorLogic.getLevelConfig();
        if (rq >= 0 && rq < config.getCols() && rr >= 0 && rr < config.getRows()) {
            return new HexCoord(rq, rr);
        }
        return null;
    }

    /**
     * Преобразует координаты гексагона в пиксельные координаты центра.
     * @param q Координата q
     * @param r Координата r
     * @return Точка центра гексагона
     */
    public Point2D.Double hexToPixel(int q, int r) { // Изменено на public
        double x = HEX_RADIUS * (3.0 / 2 * q);
        double y = HEX_RADIUS * (Math.sqrt(3) * (r + q / 2.0));
        return new Point2D.Double(x + HEX_RADIUS, y + HEX_RADIUS); // Смещение для отступа
    }

    /**
     * Создает полигон гексагона.
     * @param center Центр гексагона
     * @param radius Радиус гексагона
     * @return Полигон гексагона
     */
    private Polygon createHexagon(Point2D.Double center, int radius) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            int x = (int) (center.x + radius * Math.cos(angle));
            int y = (int) (center.y + radius * Math.sin(angle));
            hex.addPoint(x, y);
        }
        return hex;
    }

    /**
     * Создает объект Rule на основе RuleData.
     * @param ruleData Данные правила
     * @return Объект Rule или null, если тип неизвестен
     */
    private Rule createRuleFromData(RuleData ruleData) {
        if (ruleData instanceof SequenceRuleData) {
            SequenceRuleData data = (SequenceRuleData) ruleData;
            return new SequenceRule(data.getCellsInSequence(), data.getExpectedConsecutiveMines());
        } else if (ruleData instanceof GroupRuleData) {
            GroupRuleData data = (GroupRuleData) ruleData;
            return new GroupRule(data.getCellsInGroup(), data.getExpectedGroupedMines());
        } else if (ruleData instanceof EdgeRuleData) {
            EdgeRuleData data = (EdgeRuleData) ruleData;
            return new EdgeRule(data.getCellCoord(), data.getExpectedNeighborMines());
        }
        return null;
    }
}