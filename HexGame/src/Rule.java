import java.awt.Graphics2D;
import java.util.List;
import javax.swing.JPanel;

/**
 * Интерфейс для правил в игре Hexcells.
 */
public interface Rule {
    /**
     * Возвращает список ячеек, к которым применяется правило.
     * @return Список координат ячеек
     */
    List<HexCoord> getCells();

    /**
     * Возвращает ожидаемое количество мин.
     * @return Количество мин
     */
    int getExpectedMines();

    /**
     * Проверяет, выполнено ли правило.
     * @param board Игровая доска
     * @return true, если правило выполнено
     */
    boolean isSatisfied(Board board);

    /**
     * Отрисовывает правило на панели.
     * @param g2d Графический контекст
     * @param panel Панель (GridPanel или EditorGridPanel)
     * @param board Игровая доска (может быть null в редакторе)
     */
    void draw(Graphics2D g2d, JPanel panel, Board board);
}