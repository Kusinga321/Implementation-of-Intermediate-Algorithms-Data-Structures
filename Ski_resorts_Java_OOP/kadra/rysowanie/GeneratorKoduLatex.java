package osrodek.kadra.rysowanie;

import osrodek.kadra.graf.Krawedz;
import osrodek.kadra.graf.Wezel;
import osrodek.kadra.styl.GruboscKonturu;
import osrodek.kadra.styl.StylLinii;
import osrodek.kadra.tekst.InspektorZnakow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


/** Generuje kod LaTeX mapek. */
public class GeneratorKoduLatex {
    private static final int MARGINES_STRONY_CM = 1;
    private static final int MARGINES_OBRAZKA_CM = 2;

    /** Wielkość wcięcia (liczba spacji) w wynikowym kodzie `.tex`. */
    private static final int WIELKOSC_WCIECIA_W_KODZIE = 4;

    /**
     * Stałe do skalowania współrzędnych.
     *
     * <p>TikZ używa arytmetyki stałopozycyjnej i dość łatwo dostać błąd przepełnienia. Żeby tego
     * uniknąć, dzielimy wszystkie współrzędne przez POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH, a na koniec
     * mnożymy wszystkie współrzędne (argumentem "scale" obrazka w TikZ) razy
     * KONCOWE_MNOZENIE_WSPOLRZEDNYCH. Standardowo
     * POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH == KONCOWE_MNOZENIE_WSPOLRZEDNYCH, ale dopuszczalne jest
     * żeby te stałe były różne – jeśli chcemy rozsunąć/zsunąć węzły bez konieczności modyfikacji
     * wszystkich współrzędnych z osobna.
     *
     * <p>Kontekst:
     * <a href="https://www.reddit.com/r/LaTeX/comments/gcijwi/dimension_too_large_error_with_tikz/">Reddit</a>
     */
    private static final int POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH = 100;
    private static final int KONCOWE_MNOZENIE_WSPOLRZEDNYCH = 100;

    private static final String NAZWA_STYLU_WEZLA = "wezel grafu";
    private static final String NAZWA_STYLU_KRAWEDZI = "krawedz grafu";

    private final InspektorZnakow inspektorZnakow;

    public GeneratorKoduLatex(InspektorZnakow inspektorZnakow) {
        this.inspektorZnakow = inspektorZnakow;
    }

    /**
     * Generuje mapkę (w postaci kodu LaTeX) złożoną z zadanych węzłów i krawędzi.
     *
     * <p>Wynikowy napis (dla spójności) używa znaku nowej linii "\n" niezależnie od systemu
     * operacyjnego, na którym uruchamiamy program. Sprawdziliśmy, że dalsza konwersja do PDF dobrze
     * działa z "\n" nawet na Windowsie (i sam plik .tex też otwiera się poprawnie w edytorach
     * na Windowsie), więc dla uproszczenia używamy tylko "\n" jako znaku nowej linii.
     *
     * @param wezly węzły mapki.
     * @param krawedzie krawędzie mapki wraz z żądanymi kątami ich zagięć. Początki/końce wszystkich
     *                  krawędzi muszą być zawarte w podanym zbiorze węzłów.
     * @return kod LaTeX mapki.
     * @throws IllegalArgumentException jeśli któryś z argumentów jest nullem lub jeśli początek
     * lub koniec którejś krawędzi nie występuje w kolekcji węzły.
     */
    public String generujKodLatexMapki(
        Collection<Wezel> wezly,
        Collection<KrawedzZZagieciem> krawedzie
    ) {
        sprawdzNulleIZawieranie(wezly, krawedzie);

        // W szablonach podawanych do String::format warto unikać LaTeXowych komentarzy (zaczynają
        // się od znaku '%'). Znak '%' jest znakiem specjalnym String::format i wymagałby
        // zabezpieczenia.
        String kodMapki = String.format(
            """
            \\documentclass[10pt]{article}
            %s
            %s
            \\begin{document}
            \\begin{figure}[p]
            \\centering
            %s
            \\end{figure}
            \\end{document}
            """,
            generujKodFormatuPapieru(wezly),
            NIEZMIENNE_USTAWIENIA_POCZATKU_PLIKU,
            generujKodObrazka(wezly, krawedzie)
        );
        // Niektóre funkcje napisowe (niekoniecznie używane obecnie przez GeneratorMapek lub
        // związane z nim klasy) mogą używać "\r\n" jako sekwencji nowej linii, jeśli program działa
        // na Windowsie. Sprawdziliśmy, że Windows dobrze radzi sobie z obsługą Unixowych znaków
        // nowej linii ("\n"). Normalizujemy więc napis, żeby używał tylko Unixowych znaków nowej
        // linii – żeby uniknąć mieszanki dwóch rodzajów znaków/sekwencji nowej linii w wynikowym
        // pliku `.tex`.
        return kodMapki.replaceAll("\\R", "\n");
    }

    private void sprawdzNulleIZawieranie(
        Collection<Wezel> wezly,
        Collection<KrawedzZZagieciem> krawedzie
    ) {
        if (wezly == null) {
            throw new IllegalArgumentException("Kolekcja węzłów nie może być nullem.");
        }
        if (krawedzie == null) {
            throw new IllegalArgumentException(
                "Kolekcja krawędzi z zagięciem nie może być nullem."
            );
        }

        // Sprawdź zawieranie zbiorów węzłów.
        HashSet<Wezel> konceKrawedzi = new HashSet<>();
        for (KrawedzZZagieciem krawedzZZagieciem : krawedzie) {
            Krawedz krawedz = krawedzZZagieciem.krawedz();
            konceKrawedzi.add(krawedz.wezelPoczatkowy());
            konceKrawedzi.add(krawedz.wezelKoncowy());
        }
        konceKrawedzi.removeAll(wezly);

        int iluWezlowBrakuje = konceKrawedzi.size();
        if (iluWezlowBrakuje > 0) {
            throw new IllegalArgumentException(String.format(
                "Następujące węzły (w liczbie %d) występują jako początek lub koniec krawędzi, " +
                    "ale nie są wymienione w kolekcji węzłów: %s.",
                iluWezlowBrakuje,
                konceKrawedzi
            ));
        }
    }

    private String generujKodFormatuPapieru(Collection<Wezel> wezly) {
        int[] maksima = new int[]{0, 0};
        for (Wezel wezel : wezly) {
            int[] wspolrzedne = wezel.punkt().wspolrzedne();
            for (int i = 0; i < maksima.length; i++) {
                maksima[i] = Math.max(maksima[i], wspolrzedne[i]);
            }
        }

        int[] wymiaryStrony = new int[maksima.length];
        double powiekszenie =
            (double) KONCOWE_MNOZENIE_WSPOLRZEDNYCH / POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH;
        for (int i = 0; i < wymiaryStrony.length; i++) {
            double wymiarObrazka = (maksima[i] * powiekszenie) + 2 * MARGINES_OBRAZKA_CM;
            wymiaryStrony[i] = (int) Math.ceil(wymiarObrazka + 2 * MARGINES_STRONY_CM);
        }

        return String.format(
            "\\usepackage[paperwidth=%dcm, paperheight=%dcm, margin=%dcm]{geometry}",
            wymiaryStrony[0],
            wymiaryStrony[1],
            MARGINES_STRONY_CM
        );
    }

    private String generujKodObrazka(
        Collection<Wezel> wezly,
        Collection<KrawedzZZagieciem> krawedzie
    ) {
        String obrazek = (
            generujDomyslnyStylWezlowIKrawedzi() +
                generujKodWezlow(wezly) +
                generujKodKrawedzi(krawedzie)
        );

        return String.format(
            """
            \\begin{tikzpicture}[scale=%d]
            %s
            \\end{tikzpicture}
            """.stripIndent(),
            KONCOWE_MNOZENIE_WSPOLRZEDNYCH,
            // Indent dodaje brakujące "\n" na końcu napisu. Nam nie pasuje, więc usuwamy je
            // przez stripTrailing().
            obrazek.indent(WIELKOSC_WCIECIA_W_KODZIE).stripTrailing()
        );
    }

    private String generujDomyslnyStylWezlowIKrawedzi() {
        return String.format(
            // String::format zamieni "%%" na pojedynczy "%".
            """
            \\tikzset{
                %% Style węzłów i krawędzi.
                %s/.style={circle,draw,minimum size=0.75cm,inner sep=0,line width=0.35mm},
                %s/.style={-{Latex[length=3mm, width=2mm]},sloped,centered,line width=0.35mm,font=\\small}
            }
            """.stripIndent(),
            NAZWA_STYLU_WEZLA,
            NAZWA_STYLU_KRAWEDZI
        );
    }

    private String generujKodWezlow(Collection<Wezel> wezly) {
        StringBuilder kodWezlow = new StringBuilder("% Węzły:\n");
        for (Wezel wezel : wezly) {
            // Skalowanie współrzędnych jest wyjaśnione w opisie stałej
            // POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH.
            String kodWezla = "\\node[%s] (%d) at (%d/%d, %d/%d) {%d};\n".formatted(
                generujStylWezla(wezel),
                wezel.numer(),
                wezel.punkt().x(),
                POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH,
                wezel.punkt().y(),
                POCZATKOWE_DZIELENIE_WSPOLRZEDNYCH,
                wezel.numer()
            );
            kodWezlow.append(kodWezla);
        }
        return kodWezlow.toString();
    }

    private String generujStylWezla(Wezel wezel) {
        StringBuilder styl = new StringBuilder(NAZWA_STYLU_WEZLA);

        // Korzystamy z wyrażenia switch (mimo że zwykły if by wystarczył), żeby kompilator zgłosił
        // błąd, jeśli pojawi się nowa wartość enuma, a my zapomnimy jej tutaj obsłużyć.
        String dopisek = switch (wezel.styl().gruboscKonturu()) {
            case GruboscKonturu.ZWYKLY -> "";
            case GruboscKonturu.POGRUBIONY -> ",line width=0.7mm";
        };
        styl.append(dopisek);

        return styl.toString();
    }

    private String generujKodKrawedzi(Collection<KrawedzZZagieciem> krawedzieZZagieciem) {
        StringBuilder kodKrawedzi = new StringBuilder()
            .append("% Krawedzie:\n")
            .append("\\path[draw]\n");

        String szablonKrawedzi = "(%d) edge[%s] node {%s} (%d)\n";
        for (var krawedzZZagieciem : krawedzieZZagieciem) {
            Krawedz krawedz = krawedzZZagieciem.krawedz();
            String tekstKrawedzi = generujTekstKrawedzi(krawedz, 1);
            kodKrawedzi.append(szablonKrawedzi.formatted(
                krawedz.wezelPoczatkowy().numer(),
                generujStylKrawedzi(krawedzZZagieciem),
                tekstKrawedzi.isEmpty() ? "" : ("\n" + tekstKrawedzi + "\n"),
                krawedz.wezelKoncowy().numer()
            ));
        }
        kodKrawedzi.append(";\n");

        return kodKrawedzi.toString();
    }

    private String generujStylKrawedzi(KrawedzZZagieciem krawedzZZagieciem) {
        StringBuilder styl = new StringBuilder(NAZWA_STYLU_KRAWEDZI);
        int zagiecie = krawedzZZagieciem.zagiecie();
        if (zagiecie != 0) {
            styl.append(", bend left=%d".formatted(zagiecie));
        }

        // Korzystamy z wyrażenia switch (mimo że zwykły if by wystarczył), żeby kompilator zgłosił
        // błąd, jeśli pojawi się nowa wartość enuma, a my zapomnimy ją tutaj obsłużyć.
        String dopisekStyluLinii = switch (krawedzZZagieciem.krawedz().styl().stylLinii()) {
            case StylLinii.CIAGLA -> "";
            case StylLinii.PRZERYWANA -> ", densely dashed";
        };
        styl.append(dopisekStyluLinii);

        return styl.toString();
    }

    private String generujTekstKrawedzi(Krawedz krawedz, int bazowaLiczbaWciec) {
        if (krawedz.linieTekstu().isEmpty()) {
            return "";
        }
        ArrayList<String> linieTex = new ArrayList<>();
        for (String linia : krawedz.linieTekstu()) {
            String bezpiecznaLinia = inspektorZnakow.zabezpieczZnakiSpecjalne(linia);
            linieTex.add("\\contour{white}{%s}".formatted(bezpiecznaLinia));
        }
        // Podwójny odwrócony ukośnik wprowadza nowy wiersz w tabeli w LaTeX'u.
        String linieTabeli = String.join(" \\\\\n", linieTex);
        String tabela = String.format(
            """
            \\begin{tabular}{l}
            %s
            \\end{tabular}
            """,
            linieTabeli.indent(WIELKOSC_WCIECIA_W_KODZIE).stripTrailing()
        );
        return tabela.indent(bazowaLiczbaWciec * WIELKOSC_WCIECIA_W_KODZIE).stripTrailing();
    }

    private static final String NIEZMIENNE_USTAWIENIA_POCZATKU_PLIKU = """
        % Usuń numerację stron.
        \\pagestyle{empty}
        
        \\usepackage{tikz}
        % Import do ustalania końcówek strzałek.
        \\usetikzlibrary{arrows.meta}
        
        % Zmniejsz interlinię.
        \\linespread{0.9}
        
        % Importy do białych konturów napisów.
        \\usepackage[T1]{fontenc}
        \\usepackage{pslatex}
        \\usepackage{contour}
        \\usepackage{color}
        \\contourlength{1pt} % Grubość konturu
        \\contournumber{50}  % Liczba powtórzeń
        
        % Ustaw czcionkę bezszeryfową.
        \\renewcommand{\\familydefault}{\\sfdefault}
        """.stripIndent();
}
