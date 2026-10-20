package osrodek.graf;

import osrodek.sportowcy.Sportowiec;
import osrodek.symulacja.Resort;
import osrodek.kadra.styl.StylLinii;

import java.util.Objects;

public abstract class Krawedz {
    private final int poczatek, koniec;
    private final int numer;

    public Krawedz(int numerPoczatkowego, int numerKoncowego, int numer) {
        this.poczatek = numerPoczatkowego;
        this.koniec = numerKoncowego;
        this.numer = numer;
    }

    public int getNumer() {
        return numer;
    }

    public int getPoczatkowy() {
        return poczatek;
    }

    public int getKoncowy() {
        return koniec;
    }

    public abstract void przyjmijSportowca(Sportowiec s, int aktualnyCzas, Resort resort);

    public abstract String getPrefiksMapki();
    public abstract StylLinii getStylLiniiMapki();

    public abstract double ocenaJakoCel(Sportowiec s);
    public abstract double ocenaJakoKrok(Sportowiec s, Resort resort);

    public abstract void odnotujPrzejazd(Sportowiec s);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Krawedz krawedz = (Krawedz) o;
        return numer == krawedz.numer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numer, getClass());
    }
}