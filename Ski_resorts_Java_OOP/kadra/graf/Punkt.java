package osrodek.kadra.graf;

/** Punkt na płaszczyźnie. */
public class Punkt {
    private final int x;
    private final int y;

    public Punkt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int[] wspolrzedne() {
        return new int[]{x, y};
    }

    public double odleglosc(Punkt drugi) {
        int deltaX = this.x - drugi.x;
        int deltaY = this.y - drugi.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public String toString() {
        return "Punkt{x=%d, y=%d}".formatted(x, y);
    }
}
