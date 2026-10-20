package osrodek.kadra.pliki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Wykonuje operacje na systemie plików. */
public class OperatorSystemuPlikow {
    /**
     * Jeśli podany w konstruktorze katalog już istnieje, operator systemu plików spróbuje znaleźć
     * pierwszy wolny numeryczny sufiks z przedziału {1, ..., MAKS_PROB_KATALOGU}, który nie jest
     * jeszcze zajęty w tej lokalizacji.
     */
    private static final int MAKS_PROB_KATALOGU = 5000;

    /**
     * Pilnuje łącznej liczby katalogów/plików/znaków zapisanych na dysku przez wszystkie instancje
     * OperatoraSystemuPlikow. Zgodnie z wymaganiami zadania w programie powinna być tylko jedna
     * instancja GeneratoraMapek (więc i OperatoraSystemuPlikow), ale w przypadku błędu
     * programistycznego w rozwiązaniu może powstać ich więcej.
     */
    private static final BezpiecznikZapisow bezpiecznik = new BezpiecznikZapisow();

    /** Katalog w którym zapisywane są wszystkie pliki. */
    private final File katalog;

    /**
     * Tworzy instancję OperatoraSystemuPlikow i tworzy katalog, w którym operator będzie zapisywał
     * wszystkie pliki.
     *
     * @param sciezkaDoKatalogu sciezka (bezwzględna lub względem katalogu, w którym działa
     *                          program) katalogu, który ma utworzyć OperatorSystemuPlikow i
     *                          zapisywać tam wszystkie mapki. Jeśli katalog o takiej ścieżce już
     *                          istnieje, operator postara się utworzyć katalog o podobnej nazwie,
     *                          ale z dodanym numerycznym sufiksem.
     * @throws WyjatekSystemuPlikow jeśli nie udało się znaleźć niezajętej jeszcze ścieżki lub
     * utworzenie katalogu z jakiegoś powodu się nie powiodło.
     */
    public OperatorSystemuPlikow(String sciezkaDoKatalogu) throws WyjatekSystemuPlikow {
        if (sciezkaDoKatalogu == null) {
            throw new IllegalArgumentException("Ścieżka do katalogu nie może być nullem.");
        }

        File kandydatNaKatalog = new File(sciezkaDoKatalogu);
        if (kandydatNaKatalog.exists()) {
            kandydatNaKatalog = znajdzKatalogZastepczy(kandydatNaKatalog);
            System.err.printf(
                "Ścieżka \"%s\" jest już zajęta. Tworzę katalog o zastępczej ścieżce \"%s\".\n",
                sciezkaDoKatalogu,
                kandydatNaKatalog
            );
        }

        katalog = kandydatNaKatalog;
        bezpiecznik.odnotujNowyKatalog();
        try {
            if (!katalog.mkdirs()) {
                throw new WyjatekSystemuPlikow(
                    "Nie udało się utworzyć katalogu o ścieżce \"%s\".".formatted(katalog)
                );
            }
        }
        catch (SecurityException wyjatek) {
            throw new WyjatekSystemuPlikow(
                zlozKomunikatSecurityException(String.format(
                    "utworzenie katalogu o ścieżce \"%s\"",
                    katalog
                )),
                wyjatek
            );
        }
    }

    private static File znajdzKatalogZastepczy(File katalog) throws WyjatekSystemuPlikow {
        File rodzic = katalog.getParentFile();
        String nazwaKatalogu = katalog.getName();

        int numerProby = 1;
        File katalogZastepczy = zlozSciezkeKatalogu(rodzic, nazwaKatalogu, numerProby);
        try {
            while (katalogZastepczy.exists() && numerProby < MAKS_PROB_KATALOGU) {
                numerProby++;
                katalogZastepczy = zlozSciezkeKatalogu(rodzic, nazwaKatalogu, numerProby);
            }

            if (katalogZastepczy.exists()) {
                throw new WyjatekSystemuPlikow(String.format(
                    "Przekroczono limit %d prób znalezienia unikalnego sufiksu dla " +
                        "katalogu o nazwie \"%s\" w katalogu \"%s\".",
                    MAKS_PROB_KATALOGU,
                    nazwaKatalogu,
                    rodzic
                ));
            }
            return katalogZastepczy;
        }
        catch (SecurityException wyjatek) {
            throw new WyjatekSystemuPlikow(
                zlozKomunikatSecurityException(String.format(
                    "sprawdzenie, czy katalog o ścieżce \"%s\" istnieje",
                    katalogZastepczy
                )),
                wyjatek
            );
        }
    }

    private static File zlozSciezkeKatalogu(File rodzic, String nazwa, int numer) {
        return new File(rodzic, "%s-%d".formatted(nazwa, numer));
    }

    /**
     * Składa komunikat wyjątku do zgłoszenia po złapaniu SecurityException.
     *
     * @param zablokowanaAkcja wyrażenie rzeczownikowe w bierniku opisujące akcję zablokowaną
     *                         przez SecurityManagera.
     * @return komunikat wyjątku.
     */
    private static String zlozKomunikatSecurityException(String zablokowanaAkcja) {
        return String.format(
            "Klasa SecurityManager zablokowała %s. To dziwne, bo SecurityManager od Javy 17 jest " +
                "uznawany za przestarzały i nowy kod nie powinien z niego korzystać.",
            zablokowanaAkcja
        );
    }

    /**
     * Zapisuje podany tekst do nowego pliku o zadanej nazwie (w katalogu utworzonym w konstruktorze
     * tej klasy).
     *
     * @param nazwaPliku nazwa pliku (bez prefiksu z katalogów).
     * @param tekst tekst do zapisania.
     * @throws WyjatekSystemuPlikow jeśli plik o zadanej nazwie w katalogu tego
     * OperatoraSystemuPlikow już istnieje lub wystąpił inny problem z systemem plików.
     */
    public void zapiszDoPliku(String nazwaPliku, String tekst) throws WyjatekSystemuPlikow {
        if (nazwaPliku == null) {
            throw new IllegalArgumentException("Nazwa pliku nie może być nullem.");
        }
        if (tekst == null) {
            throw new IllegalArgumentException("Tekst nie może być nullem.");
        }

        File plik = new File(katalog, nazwaPliku);
        sprawdzZePlikNieIstnieje(plik);

        bezpiecznik.odnotujNowyPlik(tekst.length());
        try (FileWriter pisarz = new FileWriter(plik)) {
            pisarz.write(tekst);
        }
        catch (IOException wyjatek) {
            throw new WyjatekSystemuPlikow(
                "Nie udało się zapisać tekstu do pliku \"%s\".".formatted(plik),
                wyjatek
            );
        }
    }

    private void sprawdzZePlikNieIstnieje(File plik) throws WyjatekSystemuPlikow {
        try {
            if (plik.exists()) {
                throw new WyjatekSystemuPlikow(
                    "Plik o ścieżce \"%s\" już istnieje.".formatted(plik)
                );
            }
        }
        catch (SecurityException wyjatek) {
            throw new WyjatekSystemuPlikow(
                zlozKomunikatSecurityException(String.format(
                    "sprawdzenie, czy plik o ścieżce \"%s\" już istnieje",
                    plik
                )),
                wyjatek
            );
        }
    }
}
