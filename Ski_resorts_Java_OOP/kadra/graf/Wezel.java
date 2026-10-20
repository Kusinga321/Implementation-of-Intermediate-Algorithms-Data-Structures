package osrodek.kadra.graf;

import osrodek.kadra.styl.StylWezla;

/** Reprezentuje węzeł grafu mapki. */
public class Wezel {
    private final int numer;
    private final Punkt punkt;
    private final StylWezla styl;

    public Wezel(int numer, int x, int y, StylWezla styl) {
        if (styl == null) {
            throw new IllegalArgumentException("Styl nie może być nullem.");
        }

        this.numer = numer;
        this.punkt = new Punkt(x, y);
        this.styl = styl;
    }

    public int numer() {
        return numer;
    }

    public Punkt punkt() {
        return punkt;
    }

    public StylWezla styl() {
        return styl;
    }

    @Override
    public boolean equals(Object drugi) {
        if (!(drugi instanceof Wezel drugiWezel)) {
            return false;
        }
        return this.numer == drugiWezel.numer;
    }

    @Override
    public int hashCode() {
        return numer;
    }

    @Override
    public String toString() {
        return String.format(
            "Wezel{numer=%d, punkt=%s, styl=%s}",
            numer, punkt, styl
        );
    }
}
