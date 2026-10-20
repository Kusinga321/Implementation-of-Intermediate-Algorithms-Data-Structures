package osrodek.graf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import osrodek.narzedzia.Wczytywacz;
import osrodek.sportowcy.Sportowiec;
import osrodek.symulacja.Resort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrzeszukiwaczGrafuTest {

    private Resort testowyResort;

    @BeforeEach
    public void przygotujGrafTestowy() {
        Wczytywacz pustyWczytywacz = new Wczytywacz() {
            @Override public Trasa[] getTrasy() { return new Trasa[0]; }
            @Override public Wyciag[] getWyciagi() { return new Wyciag[0]; }
            @Override public Sportowiec[] getSportowcy() { return new Sportowiec[0]; }
            @Override public Wezel[] getWezly() { return new Wezel[0]; }
        };

        testowyResort = new Resort(
                pustyWczytywacz.getWezly(),
                pustyWczytywacz.getTrasy(),
                pustyWczytywacz.getWyciagi(),
                pustyWczytywacz.getSportowcy()
        ) {
            @Override
            public List<Krawedz> getKrawedzieWychodzace(int u) {
                List<Krawedz> krawedzie = new ArrayList<>();
                switch (u) {
                    case 0:
                        krawedzie.add(new Trasa(1, 0, 1, 1, 1, 1.0, 1.0));
                        krawedzie.add(new Trasa(8, 0, 3, 1, 1, 1.0, 1.0)); // Dodatkowa krawędź
                        break;
                    case 1:
                        krawedzie.add(new Trasa(2, 1, 2, 1, 1, 1.0, 1.0));
                        break;
                    case 2:
                        krawedzie.add(new Trasa(3, 2, 4, 1, 1, 1.0, 1.0));
                        break;
                    case 3:
                        krawedzie.add(new Trasa(4, 3, 1, 1, 1, 1.0, 1.0));
                        break;
                    case 4:
                        krawedzie.add(new Trasa(5, 4, 5, 1, 1, 1.0, 1.0));
                        krawedzie.add(new Trasa(7, 4, 0, 1, 1, 1.0, 1.0));
                        break;
                    case 5:
                        krawedzie.add(new Trasa(6, 5, 3, 1, 1, 1.0, 1.0));
                        break;
                }
                return krawedzie;
            }
        };
    }

    @Test
    public void testSciezkiOd0Do4() {
        List<Krawedz> sciezka = PrzeszukiwaczGrafu.znajdzNajkrotszaSciezke(testowyResort, 0, 4);

        assertEquals(3, sciezka.size(), "Odległość powinna wynosić 3 krawędzie.");
        assertEquals(1, sciezka.get(0).getKoncowy(), "Pierwszy krok powinien prowadzić do węzła 1");
        assertEquals(2, sciezka.get(1).getKoncowy(), "Drugi krok powinien prowadzić do węzła 2");
        assertEquals(4, sciezka.get(2).getKoncowy(), "Trzeci krok powinien prowadzić do węzła 4");
    }

    @Test
    public void testBezposredniejSciezkiOd3Do1() {
        List<Krawedz> sciezka = PrzeszukiwaczGrafu.znajdzNajkrotszaSciezke(testowyResort, 3, 1);

        assertEquals(1, sciezka.size(), "Odległość bezpośredniej ścieżki to 1.");
        assertEquals(1, sciezka.get(0).getKoncowy(), "Krok powinien prowadzić do węzła 1");
    }

    @Test
    public void testPustejSciezkiOd2DoSamegoSiebie() {
        List<Krawedz> sciezka = PrzeszukiwaczGrafu.znajdzNajkrotszaSciezke(testowyResort, 2, 2);
        Map<Integer, Integer> odleglosci = PrzeszukiwaczGrafu.policzOdleglosci(testowyResort, 2);

        assertTrue(sciezka.isEmpty(), "Ścieżka prowadząca do tego samego węzła powinna być pusta.");
        assertEquals(0, odleglosci.get(2), "Odległość do samego siebie to równe 0.");
    }

    @Test
    public void testSciezkiZWyboramiOd4Do3() {
        List<Krawedz> sciezka = PrzeszukiwaczGrafu.znajdzNajkrotszaSciezke(testowyResort, 4, 3);

        assertEquals(2, sciezka.size(), "Odległość z 4 do 3 to dokładnie 2 krawędzie.");

        int srodkowyWezel = sciezka.get(0).getKoncowy();
        assertTrue(srodkowyWezel == 5 || srodkowyWezel == 0,
                "Ścieżka powinna przejść przez węzeł 5 lub 0 (ponieważ obie drogi dają odległość 2).");

        assertEquals(3, sciezka.get(1).getKoncowy(), "Drugim i ostatnim węzłem podróży zawsze musi być 3.");
    }
}