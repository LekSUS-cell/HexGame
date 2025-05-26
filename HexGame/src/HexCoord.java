/**
 * Класс, представляющий осевые координаты (q, r) для гексагона в сетке.
 * Используется для остроконечных (pointy-topped) гексагонов.
 * Координаты неизменяемы после создания.
 */
public class HexCoord {
    private final int q; // Осевая координата q
    private final int r; // Осевая координата r

    /**
     * Конструктор, инициализирующий координаты гексагона.
     *  q Осевая координата q
     *  r Осевая координата r
     */
    public HexCoord(int q, int r) {
        this.q = q;
        this.r = r;
    }

    /**
     * Возвращает координату q.
     */
    public int getQ() {
        return q;
    }

    /**
     * Возвращает координату r.
     */
    public int getR() {
        return r;
    }

    /**
     * Сравнивает этот объект с другим на равенство.
     *  obj Объект для сравнения
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HexCoord other = (HexCoord) obj;
        return q == other.q && r == other.r;
    }

    /**
     * Вычисляет хэш-код объекта на основе q и r.
     */
    @Override
    public int hashCode() {
        // Простая формула для хэш-кода, комбинирующая q и r
        return 31 * q + r;
    }

    /**
     * Возвращает строковое представление координат.
     */
    @Override
    public String toString() {
        return "HexCoord{q=" + q + ", r=" + r + "}";
    }
}