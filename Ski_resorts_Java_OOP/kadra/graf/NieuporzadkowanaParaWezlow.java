package osrodek.kadra.graf;

/**
 * Nieuporządkowana para węzłów.
 *
 * <p>Kolejność wartości pól pary nie ma znaczenia.
 */
public class NieuporzadkowanaParaWezlow implements Comparable<NieuporzadkowanaParaWezlow> {
    private final UporzadkowanaParaWezlow wezly;

    public NieuporzadkowanaParaWezlow(UporzadkowanaParaWezlow uporzadkowana) {
        if (uporzadkowana == null) {
            throw new IllegalArgumentException("Podana para nie może być nullem.");
        }

        if (uporzadkowana.pierwszy().numer() <= uporzadkowana.drugi().numer()) {
            wezly = uporzadkowana;
        }
        else {
            wezly = new UporzadkowanaParaWezlow(uporzadkowana.drugi(), uporzadkowana.pierwszy());
        }
    }

    /**
     * Daje węzeł o niewiększym numerze.
     */
    public Wezel mniejszy() {
        return wezly.pierwszy();
    }

    /**
     * Daje węzeł o niemniejszym numerze.
     */
    public Wezel wiekszy() {
        return wezly.drugi();
    }

    @Override
    public int compareTo(NieuporzadkowanaParaWezlow druga) {
        if (druga == null) {
            throw new NullPointerException();
        }
        return this.wezly.compareTo(druga.wezly);
    }

    @Override
    public boolean equals(Object drugi) {
        if (!(drugi instanceof NieuporzadkowanaParaWezlow drugaPara)) {
            return false;
        }
        return this.compareTo(drugaPara) == 0;
    }

    @Override
    public int hashCode() {
        return wezly.hashCode();
    }

    @Override
    public String toString() {
        return String.format(
            "NieuporzadkowanaParaWezlow{mniejszy=%s, wiekszy=%s}",
            mniejszy(),
            wiekszy()
        );
    }
}
