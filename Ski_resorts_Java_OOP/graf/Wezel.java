package osrodek.graf;

public class Wezel {
    private final int wysokosc, x, y, nrWezla;
    private final boolean czySkomunikowany;

    public Wezel(int nrWezla, int wysokosc, int x, int y, boolean czySkomunikowany) {
        this.nrWezla = nrWezla;
        this.wysokosc = wysokosc;
        this.x = x;
        this.y = y;
        this.czySkomunikowany = czySkomunikowany;
    }

    public int getWysokosc() {
        return wysokosc;
    }

    public int getNumer() {
        return nrWezla;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isSkomunikowany() {
        return czySkomunikowany;
    }
}