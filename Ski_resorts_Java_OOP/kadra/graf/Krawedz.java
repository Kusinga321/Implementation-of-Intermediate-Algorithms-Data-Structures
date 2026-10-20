package osrodek.kadra.graf;

import osrodek.kadra.styl.StylKrawedzi;

import java.util.ArrayList;
import java.util.List;

/** Reprezentuje krawędź grafu mapki. */
public class Krawedz {
    private final int numer;
    private final UporzadkowanaParaWezlow wezly;
    private final StylKrawedzi styl;
    private final ArrayList<String> linieTekstu;

    public Krawedz(
        int numer,
        Wezel wezelPoczatkowy,
        Wezel wezelKoncowy,
        StylKrawedzi styl,
        List<String> linieTekstu
    ) {
        if (wezelPoczatkowy == null || wezelKoncowy == null || styl == null
            || linieTekstu == null) {

            throw new IllegalArgumentException("Argumenty nie mogą być nullem.");
        }
        this.numer = numer;
        this.wezly = new UporzadkowanaParaWezlow(wezelPoczatkowy, wezelKoncowy);
        this.styl = styl;
        this.linieTekstu = new ArrayList<>(linieTekstu);
    }

    public int numer() {
        return numer;
    }

    public Wezel wezelPoczatkowy() {
        return wezly.pierwszy();
    }

    public Wezel wezelKoncowy() {
        return wezly.drugi();
    }

    public List<String> linieTekstu() {
        return new ArrayList<>(linieTekstu);
    }

    public StylKrawedzi styl() {
        return styl;
    }

    public UporzadkowanaParaWezlow uporzadkowanaParaWezlow() {
        return wezly;
    }

    public NieuporzadkowanaParaWezlow nieuporzadkowanaParaWezlow() {
        return wezly.tworzNieuporzadkowana();
    }

    @Override
    public boolean equals(Object drugi) {
        if (!(drugi instanceof Krawedz drugaKrawedz)) {
            return false;
        }
        return this.numer == drugaKrawedz.numer;
    }

    @Override
    public int hashCode() {
        return numer;
    }

    @Override
    public String toString() {
        return String.format(
            "Krawedz{numer=%d, wezelPoczatkowy=%s, wezelKoncowy=%s, styl=%s, linieTekstu=%s}",
            numer, wezly.pierwszy(), wezly.drugi(), styl, linieTekstu
        );
    }
}
