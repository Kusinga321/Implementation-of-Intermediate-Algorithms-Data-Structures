package osrodek.kadra.graf;

import java.util.Arrays;

/**
 * Uporządkowana para węzłów.
 *
 * <p>Kolejność wartości pól pary ma znaczenie.
 */
public class UporzadkowanaParaWezlow implements Comparable<UporzadkowanaParaWezlow> {
    private final Wezel[] wezly;

    public UporzadkowanaParaWezlow(Wezel pierwszy, Wezel drugi) {
        if (pierwszy == null || drugi == null) {
            throw new IllegalArgumentException("Węzły nie mogą być nullem.");
        }

        wezly = new Wezel[]{pierwszy, drugi};
    }

    public Wezel pierwszy() {
        return wezly[0];
    }

    public Wezel drugi() {
        return wezly[1];
    }

    public NieuporzadkowanaParaWezlow tworzNieuporzadkowana() {
        return new NieuporzadkowanaParaWezlow(this);
    }

    // Leksykograficznie po numerach węzłów.
    @Override
    public int compareTo(UporzadkowanaParaWezlow druga) {
        if (druga == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < 2; i++) {
            int porzadek = Integer.compare(wezly[i].numer(), druga.wezly[i].numer());
            if (porzadek != 0) {
                return porzadek;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object drugi) {
        if (!(drugi instanceof UporzadkowanaParaWezlow drugaPara)) {
            return false;
        }
        return compareTo(drugaPara) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{wezly[0].numer(), wezly[1].numer()});
    }

    @Override
    public String toString() {
        return String.format(
            "UporzadkowanaParaWezlow{pierwszy=%s, drugi=%s}",
            pierwszy(),
            drugi()
        );
    }
}
