package osrodek.kadra.tekst;

import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Obsługuje znaki, które mogłyby uszkodzić wynikowy kod LaTeX.
 */
public class InspektorZnakow {
    /** Litery, liczby, spacja, sekwencje nowej linii (Unix i Windows). */
    private static final Pattern ZNAKI_STANDARODWE = Pattern.compile("[a-zA-Z0-9 ]|\\R");

    /** Litery polskiego alfabetu, które nie występują w alfabecie łacińskim. */
    public static final String POLSKIE_ZNAKI = "ĄąĆćĘęŁłŃńÓóŚśŹźŻż";

    /**
     * Znaki specjalne, które trzeba zabezpieczyć, ale wystarczy poprzedzić je odwróconym
     * ukośnikiem.
     */
    private static final String PROSTE_NIEBEZPIECZNE_ZNAKI = "%{}_";

    /** Znaki specjalne, które trzeba zabezpieczyć, ale wymagają sprytniejszej metody. */
    private static final String TRUDNE_NIEBEZPIECZNE_ZNAKI = "^\\";

    /** Wszystkie znaki specjalne. */
    private static final String ZNAKI_SPECJALNE = (
        "`!()/+-–—*=:;,.[]<>?\"'"
        + PROSTE_NIEBEZPIECZNE_ZNAKI
        + TRUDNE_NIEBEZPIECZNE_ZNAKI
    );

    /** Wszystkie dozwolone znaki spoza zestawu ZNAKI_STANDARODWE. */
    private static final String ZNAKI_NIESTANDARDOWE = POLSKIE_ZNAKI + ZNAKI_SPECJALNE;

    /** Słownik kodNiebezpiecznegoZnaku -> bezpiecznyOdpowiednikWLatex. */
    private static final Map<Integer, String> SLOWNIK_ZAMIAN = Map.copyOf(tworzSlownikZamian());

    private static HashMap<Integer, String> tworzSlownikZamian() {
        HashMap<Integer, String> zamiany = new HashMap<>();

        // Znaki, które wystarczy poprzedzić odwróconym ukośnikiem.
        for (int kod : PROSTE_NIEBEZPIECZNE_ZNAKI.codePoints().toArray()) {
            String odpowiednik = new StringBuilder("\\").appendCodePoint(kod).toString();
            zamiany.put(kod, odpowiednik);
        }

        // Trudniejsze zamiany.
        HashMap<Integer, String> trudneZamiany = new HashMap<>();

        trudneZamiany.put(dajKodZnaku("\\"), "{\\textbackslash}");
        trudneZamiany.put(dajKodZnaku("^"), "{\\textasciicircum}");

        if (trudneZamiany.size() != TRUDNE_NIEBEZPIECZNE_ZNAKI.length()) {
            throw new IllegalStateException(String.format(
                "Zestaw trudnych zamian (rozmiar: %d) rozsynchronizował się z zestawem " +
                    "TRUDNE_NIEBEZPIECZNE_ZNAKI (rozmiar: %d).",
                trudneZamiany.size(),
                TRUDNE_NIEBEZPIECZNE_ZNAKI.length()
            ));
        }

        zamiany.putAll(trudneZamiany);
        return zamiany;
    }

    private static int dajKodZnaku(String znak) {
        int[] kody = znak.codePoints().toArray();
        if (kody.length != 1) {
            throw new IllegalArgumentException(String.format(
                "Nieprawidłowa liczba kodów znaków. Powinien być 1, a jest %d.",
                kody.length
            ));
        }
        return kody[0];
    }

    /** Pusty konstruktor domyślny. */
    public InspektorZnakow() {

    }

    /**
     * Sprawdza, że w tekście nie ma niedozwolonych znaków. W przeciwnym razie zgłasza wyjątek.
     *
     * @param tekst tekst do sprawdzenia. Nie może być nullem.
     * @throws IllegalArgumentException jeśli tekst zawiera niedozwolony znak lub tekst jest nullem.
     */
    public void sprawdzZeWszystkieZnakiDozwolone(String tekst) {
        if (tekst == null) {
            throw new IllegalArgumentException("Tekst nie może być nullem.");
        }

        String niestandardoweZnakiTekstu = ZNAKI_STANDARODWE.matcher(tekst).replaceAll("");
        int[] kodyNiestandardowychZnakowTekstu = niestandardoweZnakiTekstu.codePoints().toArray();

        HashSet<Integer> dozwoloneZnakiNiestandardowe = new HashSet<>(
            ZNAKI_NIESTANDARDOWE.codePoints().boxed().toList()
        );

        LinkedHashSet<Integer> niedozwoloneZnaki = new LinkedHashSet<>();
        for (int kodZnaku : kodyNiestandardowychZnakowTekstu) {
            if (!dozwoloneZnakiNiestandardowe.contains(kodZnaku)) {
                niedozwoloneZnaki.add(kodZnaku);
            }
        }
        if (!niedozwoloneZnaki.isEmpty()) {
            int[] tablicaZnakow = niedozwoloneZnaki.stream().mapToInt(Integer::intValue).toArray();
            throw new IllegalArgumentException(String.format(
                "Niedozwolone znaki \"%s\" w tekście \"%s\"",
                new String(tablicaZnakow, 0, tablicaZnakow.length),
                tekst
            ));
        }
    }

    /**
     * Zamienia niebezpieczne znaki specjalne tekstu na ich bezpieczne odpowiedniki. Zakłada, że
     * podany tekst nie zawiera znaków niedozwolonych.
     *
     * @param tekst tekst do zabezpieczenia. Nie może być nullem.
     * @return tekst, z zastąpionymi niebezpiecznymi znakami.
     */
    public String zabezpieczZnakiSpecjalne(String tekst) {
        if (tekst == null) {
            throw new IllegalArgumentException("Tekst nie może być nullem.");
        }

        StringBuilder bezpiecznyTekst = new StringBuilder();
        for (int kodZnaku : tekst.codePoints().toArray()) {
            String mozeZamiana = SLOWNIK_ZAMIAN.get(kodZnaku);
            if (mozeZamiana == null) {
                bezpiecznyTekst.appendCodePoint(kodZnaku);
            }
            else {
                bezpiecznyTekst.append(mozeZamiana);
            }
        }
        return bezpiecznyTekst.toString();
    }
}
