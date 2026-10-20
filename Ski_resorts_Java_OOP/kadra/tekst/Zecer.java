package osrodek.kadra.tekst;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Układa tekst na krawędzi. */
public class Zecer {
    private static final String SYGNAL_PRZYCIECIA = "...";

    private final int maksLiczbaLinii;

    private final int limitDlugosciLinii;

    /**
     * Tworzy instancję Zecera.
     *
     * @param maksLiczbaLinii maksymalna liczba linii, która mieści się na krawędzi.
     * @param limitDlugosciLinii maksymalna liczba znaków, którą Zecer może umieścić w jednej
     *                           linii tekstu. Jeśli tekst w linii jest za długi, Zecer docina go do
     *                           limitu i dopisuje wielokropek (przycięta linia z wielokropkiem
     *                           może nieznacznie przekraczać limitDlugosciLinii).
     */
    public Zecer(int maksLiczbaLinii, int limitDlugosciLinii) {
        if (maksLiczbaLinii < 0) {
            throw new IllegalArgumentException("Maksymalna liczba linii nie może być ujemna.");
        }
        if (limitDlugosciLinii < 0) {
            throw new IllegalArgumentException("Limit długości linii nie może być ujemny.");
        }

        this.maksLiczbaLinii = maksLiczbaLinii;
        this.limitDlugosciLinii = limitDlugosciLinii;
    }

    /**
     * Układa tekst na krawędzi.
     *
     * <p>Dzieli tekst między linie zgodnie z limitem długości linii. Nie łamie słów. Jeśli
     * tekst jest za długi, dopisuje w ostatniej linii wielokropek.
     *
     * @param tekst tekst to ułożenia.
     * @return lista linii tekstu do zapisania na krawędzi.
     */
    public List<String> ulozTekst(String tekst) {
        if (tekst == null) {
            throw new IllegalArgumentException("Tekst nie może być nullem.");
        }

        ArrayList<String> leksemy = podzielNaLeksemy(tekst);

        ArrayList<String> linie = podzielNaLinie(leksemy);

        if (linie.size() > maksLiczbaLinii) {
            linie = new ArrayList<>(linie.subList(0, maksLiczbaLinii));
            int ostatnia = linie.size() - 1;
            // Akceptujemy, że SYGNAL_PRZYCIECIA może wykraczać ponad limit.
            linie.set(ostatnia, linie.get(ostatnia) + SYGNAL_PRZYCIECIA);
        }

        return linie;
    }

    private ArrayList<String> podzielNaLeksemy(String tekst) {
        String znakiSlowne = "a-zA-Z0-9_" + InspektorZnakow.POLSKIE_ZNAKI;
        String ciagZnakowSlownych = "[%s]+".formatted(znakiSlowne);
        String znakNieslowny = "[^%s]".formatted(znakiSlowne);
        Pattern wzorzec = Pattern.compile(
            "(%s|%s)".formatted(ciagZnakowSlownych, znakNieslowny)
        );
        Matcher dopasowania = wzorzec.matcher(tekst);

        ArrayList<String> leksemy = new ArrayList<>();
        int sumaDlugosci = 0;
        while (dopasowania.find()) {
            String leksem = dopasowania.group();
            sumaDlugosci += leksem.length();
            leksemy.add(leksem);
        }

        if (sumaDlugosci != tekst.length()) {
            throw new RuntimeException(String.format(
                "Błąd podziału na leksemy tekstu \"%s\". Otrzymano podział %s.",
                tekst,
                leksemy
            ));
        }
        return leksemy;
    }

    private ArrayList<String> podzielNaLinie(ArrayList<String> leksemy) {
        ArrayList<String> linie = new ArrayList<>();
        StringBuilder obecnaLinia = new StringBuilder();
        for (String leksem : leksemy) {
            if (obecnaLinia.length() + leksem.length() <= limitDlugosciLinii) {
                obecnaLinia.append(leksem);
            }
            else {
                if (obecnaLinia.isEmpty()) {
                    // Jeśli leksem nie mieści się nawet w pustej linii, to po prostu trzeba
                    // go wpisać - przeniesienie do nowej linii nic by nie dało.
                    obecnaLinia.append(leksem);
                    linie.add(obecnaLinia.toString());
                    obecnaLinia = new StringBuilder();
                }
                else {
                    linie.add(obecnaLinia.toString());
                    obecnaLinia = new StringBuilder(leksem);
                }
            }
        }
        if (!obecnaLinia.isEmpty()) {
            linie.add(obecnaLinia.toString());
        }
        return linie;
    }

    /**
     * Przycina linie tekstu (z osobna) do limitu długości linii.
     *
     * <p>Jeśli linia jest niedłuższa od limitu, nic z nią nie robi. W przeciwnym razie skraca
     * ją do limitu długości linii i dopisuje wielokropek (który nieznacznie wyjdzie ponad
     * limit długości linii).
     *
     * @param linie lista linii do przycięcia.
     * @return lista linii po przycięciu.
     */
    public List<String> przytnijLinie(List<String> linie) {
        if (linie.size() > maksLiczbaLinii) {
            throw new IllegalArgumentException(
                "Podano %d linii, a można co najwyżej %d.".formatted(
                    linie.size(), maksLiczbaLinii
                )
            );
        }
        ArrayList<String> przycieteLinie = new ArrayList<>();
        for (String linia : linie) {
            int[] znakiUnicode = linia.codePoints().toArray();
            if (znakiUnicode.length > limitDlugosciLinii) {
                // Akceptujemy, że SYGNAL_PRZYCIECIA może wykraczać poza limit.
                linia = new String(znakiUnicode, 0, limitDlugosciLinii) + SYGNAL_PRZYCIECIA;
            }
            przycieteLinie.add(linia);
        }
        return przycieteLinie;
    }
}
