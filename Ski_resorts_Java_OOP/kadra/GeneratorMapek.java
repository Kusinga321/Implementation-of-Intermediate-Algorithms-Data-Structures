package osrodek.kadra;

import osrodek.kadra.pliki.WyjatekSystemuPlikow;
import osrodek.kadra.pliki.OperatorSystemuPlikow;
import osrodek.kadra.graf.Krawedz;
import osrodek.kadra.graf.Wezel;
import osrodek.kadra.rysowanie.GeneratorKoduLatex;
import osrodek.kadra.rysowanie.KrawedzZZagieciem;
import osrodek.kadra.rysowanie.ZaginaczKrawedzi;
import osrodek.kadra.styl.StylKrawedzi;
import osrodek.kadra.styl.StylWezla;
import osrodek.kadra.tekst.Zecer;
import osrodek.kadra.tekst.InspektorZnakow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Generator mapek w deklaratywnym formacie .tex.
 *
 * <p>Sposób użycia:
 * 1. Wywołujemy konstruktor generatora.
 * 2. Dodajemy węzły i krawędzie grafu mapki, korzystając z metod `dodajWezel` i `dodajKrawedz`.
 * 3. Generujemy plik .tex mapki metodą `tworzMapke`.
 * 4. Żeby zacząć konstruować nową mapkę, wywołujemy metodę `zeruj` i powtarzamy kroki 2-3.
 *
 * <p>Z pliku .tex mapki można wygenerować obrazek PDF, korzystając z następującego polecenia:
 * $ pdflatex mapka.tex
 */
public class GeneratorMapek {
    private static final int MAKSYMALNA_LICZBA_LINII = 2;

    private static final int LIMIT_DLUGOSCI_LINII = 30;

    private static final double MINIMALNA_ZALECANA_DLUGOSC_KRAWEDZI = 6.0;

    private final InspektorZnakow inspektorZnakow;

    private final Zecer zecer;

    private final GeneratorKoduLatex generatorKoduLatex;

    private final OperatorSystemuPlikow operatorSystemuPlikow;

    private LinkedHashMap<Integer, Wezel> wezly;

    private ArrayList<Krawedz> krawedzie;

    /**
     * Tworzy obiekt GeneratorMapki i tworzy na dysku nowy katalog na mapki.
     *
     * @param sciezkaKatalogu ścieżka katalogu, który GeneratorMapek ma utworzyć do przechowywania
     *                        mapek (w razie potrzeby tworzy też brakujące katalogi nadrzędne).
     *                        Metoda tworzMapke będzie zapisywać mapki właśnie do tego
     *                        katalogu. Jeśli sciezkaKatalogu jest już zajęta (tj. pod tą ścieżką
     *                        jest już jakiś plik lub katalog), GeneratorMapek utworzy nowy katalog
     *                        o podobnej nazwie (z liczbowym sufiksem) i to do niego będzie
     *                        zapisywał mapki.
     * @throws WyjatekSystemuPlikow jeśli nie udało się utworzyć katalogu o zadanej ścieżce (ew. z
     * liczbowym sufiksem) – np. jeśli użytkownik nie ma uprawnień do utworzenia katalogu w zadanej
     * lokalizacji.
     */
    public GeneratorMapek(String sciezkaKatalogu) throws WyjatekSystemuPlikow {
        inspektorZnakow = new InspektorZnakow();
        zecer = new Zecer(MAKSYMALNA_LICZBA_LINII, LIMIT_DLUGOSCI_LINII);
        generatorKoduLatex = new GeneratorKoduLatex(inspektorZnakow);
        operatorSystemuPlikow = new OperatorSystemuPlikow(sciezkaKatalogu);

        zeruj();
    }

    /**
     * Usuwa wszystkie węzły i krawędzie z generatora mapki.
     *
     * <p>Użyj po wygenerowaniu mapki, jeśli chcesz zacząć konstruować następną.
     */
    public void zeruj() {
        wezly = new LinkedHashMap<>();
        krawedzie = new ArrayList<>();
    }

    /**
     * Dodaje nowy węzeł do grafu mapki.
     *
     * <p>Uwaga: węzeł oznacza tutaj węzeł grafu mapki, który koncepcyjnie odpowiada węzłom ośrodka
     * narciarskiego, ale z punktu widzenia Javy nie ma bezpośredniego związku z klasą Wezeł (lub
     * podobną) z Państwa rozwiązań. GeneratorMapek nic nie wie (i nie zakłada) o klasach z Państwa
     * rozwiązań.
     *
     * @param numer numer węzła grafu mapki. Jednoznacznie identyfikuje węzeł (każdy węzeł musi mieć
     *              inny numer). Wyświetla się w węźle na mapce. Za jego pomocą definiujemy końce
     *              krawędzi mapki.
     * @param x     współrzędna x położenia węzła na mapce. Wymaganie: x >= 0.
     * @param y     współrzędna y położenia węzła na mapce. Wymaganie: y >= 0.
     * @param styl  graficzny styl węzła na mapce. Nie może być nullem.
     *
     * @throws IllegalArgumentException jeśli wezeł o zadanym numerze już istnieje lub inne
     * parametry nie spełniają warunków opisanych wyżej.
     */
    public void dodajWezel(int numer, int x, int y, StylWezla styl) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Ujemna współrzędna: (%d, %d)".formatted(x, y));
        }
        if (wezly.get(numer) != null) {
            throw new IllegalArgumentException("Węzeł o numerze %d już istnieje.".formatted(numer));
        }
        if (styl == null) {
            throw new IllegalArgumentException("Styl węzła nie może być nullem.");
        }
        wezly.put(numer, new Wezel(numer, x, y, styl));
    }

    /**
     * Dodaje nową krawędź do grafu mapki.
     *
     * <p>Zakłada, że użytkownik sam podzielił tekst krawędzi na linie. Jeśli linia jest dłuższa
     * niż 30 znaków, generator przycina ją do pierwszych 30 znaków i dopisuje wielokropek (żeby
     * zasygnalizować przycięcie).
     *
     * <p>Zaleca się, żeby krawędzie miały długość przynajmniej 6.0, inaczej tekst krawędzi może
     * nachodzić na węzły, w których zaczepiona jest krawędź.
     *
     * <p>Między każdą parą węzłów może być co najwyżej 5 krawędzi.
     *
     * @param numerPoczatkowegoWezla numer początkowego węzła. Węzeł o tym numerze musi być
     *                               wcześniej dodany do grafu mapki metodą dodajWezel.
     *                               Wymaganie: numerPoczatkowegoWezla != numerKoncowegoWezla.
     * @param numerKoncowegoWezla    numer końcowego węzła. Węzeł o tym numerze musi być wcześniej
     *                               dodany do grafu mapki metodą dodajWezel.
     *                               Wymaganie: numerPoczatkowegoWezla != numerKoncowegoWezla.
     * @param styl                   graficzny styl krawędzi na mapce. Nie może być nullem.
     * @param linie                  lista 0-2 linii tekstu, które mają wyświetlić się na
     *                               krawędzi. Nie może być nullem. Napisy na liście linii mogą
     *                               zawierać tylko litery alfabetu polskiego, cyfry, spacje i
     *                               następujące znaki specjalne: `!()/+-–—*=:;,.[]<>?"'%{}_^\
     *                               Napisy na liście linii nie mogą zawierać znaku nowej linii.
     * @throws IllegalArgumentException jeśli któryś z parametrów nie spełnia wymagań.
     */
    public void dodajKrawedz(
        int numerPoczatkowegoWezla,
        int numerKoncowegoWezla,
        StylKrawedzi styl,
        List<String> linie
    ) {
        ArrayList<Wezel> konce = sprawdzWspolneParametryKrawedzi(
            numerPoczatkowegoWezla,
            numerKoncowegoWezla,
            styl
        );
        if (linie == null) {
            throw new IllegalArgumentException("Lista linii nie może być nullem.");
        }
        if (linie.size() > MAKSYMALNA_LICZBA_LINII) {
            throw new IllegalArgumentException(String.format(
                "Podano %d linii, a można co najwyżej %d.",
                linie.size(),
                MAKSYMALNA_LICZBA_LINII
            ));
        }
        for (String linia : linie) {
            sprawdzNiedozwoloneZnaki(linia);
        }

        List<String> gotoweLinie = zecer.przytnijLinie(linie);

        dodajKrawedzZPrzygotowanymTekstem(konce.get(0), konce.get(1), styl, gotoweLinie);
    }

    /**
     * Dodaje nową krawędź do grafu mapki.
     *
     * <p>Działa analogicznie do metody o tej samej nazwie opisanej wyżej. Jedyną różnicą jest
     * obsługa tekstu krawędzi. Obecna metoda sama dzieli tekst na (co najwyżej dwie) linie. Limit
     * długość linii to 30 znaków. Podział na linie nie łamie słów, natomiast mogą zdarzyć się
     * nienaturalne podziały (np. przecinek lub spacja na początku nowej linii). Jeśli tekst nie
     * mieści się na krawędzi, metoda przycina go do odpowiedniej długości i dopisuje wielokropek
     * (żeby zasygnalizować przycięcie). Podobnie jak w metodzie z listą linii tekst nie może
     * zawierać znaku nowej linii (zestaw dozwolonych znaków jest identyczny jak w liście linii).
     */
    public void dodajKrawedz(
        int numerPoczatkowegoWezla,
        int numerKoncowegoWezla,
        StylKrawedzi styl,
        String tekst
    ) {
        ArrayList<Wezel> konce = sprawdzWspolneParametryKrawedzi(
            numerPoczatkowegoWezla,
            numerKoncowegoWezla,
            styl
        );
        if (tekst == null) {
            throw new IllegalArgumentException("Tekst krawędzi nie może być nullem.");
        }
        sprawdzNiedozwoloneZnaki(tekst);

        List<String> linie = zecer.ulozTekst(tekst);

        dodajKrawedzZPrzygotowanymTekstem(konce.get(0), konce.get(1), styl, linie);
    }

    /**
     * Generuje kod .tex mapki i zapisuje go jako plik na dysku.
     *
     * @param nazwaPliku nazwa pliku mapki, który generator ma utworzyć (w katalogu, który generator
     *                   utworzył w konstruktorze).
     * @throws WyjatekSystemuPlikow jeśli plik o tej nazwie już istnieje lub wystąpił inny błąd
     * związany z systemem plików.
     * @throws IllegalArgumentException jeśli podana nazwaPliku jest nullem.
     */
    public void tworzMapke(String nazwaPliku) throws WyjatekSystemuPlikow {
        if (nazwaPliku == null) {
            throw new IllegalArgumentException("Nazwa pliku nie może być nullem.");
        }

        ArrayList<KrawedzZZagieciem> krawedzieZZagieciem =
            ZaginaczKrawedzi.pozaginajKrawedzie(krawedzie);

        String opisMapki = generatorKoduLatex.generujKodLatexMapki(
            wezly.values(), krawedzieZZagieciem
        );

        operatorSystemuPlikow.zapiszDoPliku(nazwaPliku, opisMapki);
    }

    /* =============== Niżej są już tylko metody prywatne. =============== */

    private ArrayList<Wezel> sprawdzWspolneParametryKrawedzi(
        int numerPoczatkowegoWezla,
        int numerKoncowegoWezla,
        StylKrawedzi styl
    ) {
        if (numerPoczatkowegoWezla == numerKoncowegoWezla) {
            throw new IllegalArgumentException(
                "Początek krawędzi musi być w innym węźle niż koniec."
            );
        }
        if (styl == null) {
            throw new IllegalArgumentException("Styl krawędzi nie może być nullem.");
        }

        ArrayList<Wezel> konce = znajdzWezly(numerPoczatkowegoWezla, numerKoncowegoWezla);

        double dlugosc = konce.get(0).punkt().odleglosc(konce.get(1).punkt());
        if (dlugosc < MINIMALNA_ZALECANA_DLUGOSC_KRAWEDZI) {
            System.err.printf(
                "Uwaga: Krawędź między od węzła %d do węzła %d jest krótsza niż zalecane %.2f " +
                "(ma długość %.2f). Dłuższy tekst może nie wyświetlać się poprawnie.%n",
                konce.get(0).numer(),
                konce.get(1).numer(),
                MINIMALNA_ZALECANA_DLUGOSC_KRAWEDZI,
                dlugosc
            );
        }

        return konce;
    }

    private ArrayList<Wezel> znajdzWezly(int... numeryWezlow) {
        ArrayList<Wezel> szukaneWezly = new ArrayList<>();
        for (int numer : numeryWezlow) {
            Wezel wezel = wezly.get(numer);
            if (wezel == null) {
                throw new IllegalArgumentException(
                    "Węzeł o numerze %d nie istnieje.".formatted(numer)
                );
            }
            szukaneWezly.add(wezel);
        }
        return szukaneWezly;
    }

    private void sprawdzNiedozwoloneZnaki(String napis) {
        if (napis.matches("\\R")) {
            throw new IllegalArgumentException("Napis nie może zawierać znaku nowej linii.");
        }
        // Zgłasza wyjątek, jeśli coś się nie zgadza.
        inspektorZnakow.sprawdzZeWszystkieZnakiDozwolone(napis);
    }

    private void dodajKrawedzZPrzygotowanymTekstem(
        Wezel wezelPoczatkowy,
        Wezel wezelKoncowy,
        StylKrawedzi styl,
        List<String> linie
    ) {
        Krawedz krawedz = new Krawedz(krawedzie.size(), wezelPoczatkowy, wezelKoncowy, styl, linie);
        krawedzie.add(krawedz);
    }
}
