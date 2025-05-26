/**
 * Класс, представляющий ячейку на игровой доске Hexcells.
 */
public class Cell {
    private boolean mine;
    private boolean revealed;
    private boolean flagged;
    private int revealedValue;

    /**
     * Конструктор, создающий пустую ячейку.
     */
    public Cell() {
        this.mine = false;
        this.revealed = false;
        this.flagged = false;
        this.revealedValue = -1;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public int getRevealedValue() {
        return revealedValue;
    }

    public void setRevealedValue(int revealedValue) {
        this.revealedValue = revealedValue;
    }
}