package osrodek.narzedzia;

public final class NarzedziaCzasu {

    /**
     * Zamienia czas w formacie "HH:MM:SS" na łączną liczbę sekund.
     */

    private NarzedziaCzasu() {}

    public static int czasNaSekundy(String czas) {
        String[] czesci = czas.split(":");
        int godziny = Integer.parseInt(czesci[0]);
        int minuty = Integer.parseInt(czesci[1]);
        int sekundy = Integer.parseInt(czesci[2]);

        return godziny * 3600 + minuty * 60 + sekundy;
    }
}