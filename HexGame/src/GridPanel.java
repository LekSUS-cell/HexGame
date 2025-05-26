import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/**
 * Панель для отображения игровой сетки Hexcells.
 */
public class GridPanel extends JPanel {
    private final Board board;
    private static final int HEX_RADIUS = 30; // Радиус гексагона

    /**
     * Конструктор, инициализирующий панель.
     * @param board Игровая доска
     */
    public GridPanel(Board board) {
        this.board = board;
        setBackground(Color.LIGHT_GRAY);

        // Установка предпочтительного размера
        int width = (int) (board.getCols() * HEX_RADIUS * 1.5 + HEX_RADIUS);
        int height = (int) (board.getRows() * HEX_RADIUS * Math.sqrt(3) + HEX_RADIUS);
        setPreferredSize(new Dimension(width, height));

        // Добавление обработчика мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HexCoord coord = pixelToHex(e.getPoint());
                if (coord != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) { // Левая кнопка
                        board.revealCell(coord);
                    } else if (e.getButton() == MouseEvent.BUTTON3) { // Правая кнопка
                        board.toggleFlag(coord);
                    }
                    repaint();
                }
            }
        });
    }

    /**
     * Отрисовывает компонент: сетку гексагонов, ячейки, флаги, мины и правила.
     * @param g Графический контекст
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Отрисовка гексагонов
        for (int r = 0; r < board.getRows(); r++) {
            for (int q = 0; q < board.getCols(); q++) {
                HexCoord coord = new HexCoord(q, r);
                Point2D.Double center = hexToPixel(q, r);
                Polygon hex = createHexagon(center, HEX_RADIUS);

                Cell cell = board.getCell(coord);
                if (cell == null) {
                    continue;
                }

                // Отрисовка ячейки
                if (cell.isRevealed()) {
                    if (cell.isMine()) {
                        g2d.setColor(Color.BLACK);
                        g2d.fillPolygon(hex);
                    } else {
                        g2d.setColor(Color.WHITE);
                        g2d.fillPolygon(hex);
                        if (cell.getRevealedValue() > 0) {
                            g2d.setColor(Color.BLACK);
                            g2d.drawString(String.valueOf(cell.getRevealedValue()),
                                    (int) center.x - 5, (int) center.y + 5);
                        }
                    }
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.fillPolygon(hex);
                    if (cell.isFlagged()) {
                        g2d.setColor(Color.RED);
                        g2d.fillOval((int) (center.x - HEX_RADIUS / 2), (int) (center.y - HEX_RADIUS / 2),
                                HEX_RADIUS, HEX_RADIUS);
                    }
                }

                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(hex);
            }
        }

        // Отрисовка правил
        for (Rule rule : board.getActiveRules()) {
            rule.draw(g2d, this, board);
        }

        // Если игра завершена и проиграна, показать все мины
        if (board.isGameOver() && !board.isGameWon()) {
            for (int r = 0; r < board.getRows(); r++) {
                for (int q = 0; q < board.getCols(); q++) {
                    HexCoord coord = new HexCoord(q, r);
                    Cell cell = board.getCell(coord);
                    if (cell != null && cell.isMine() && !cell.isRevealed()) {
                        Point2D.Double center = hexToPixel(q, r);
                        g2d.setColor(Color.BLACK);
                        g2d.fillOval((int) (center.x - HEX_RADIUS / 2), (int) (center.y - HEX_RADIUS / 2),
                                HEX_RADIUS, HEX_RADIUS);
                    }
                }
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
        if (rq >= 0 && rq < board.getCols() && rr >= 0 && rr < board.getRows()) {
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
        return new Point2D.Double(x + HEX_RADIUS, y + HEX_RADIUS);
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
}