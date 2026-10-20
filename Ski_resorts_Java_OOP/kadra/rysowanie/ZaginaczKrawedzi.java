package osrodek.kadra.rysowanie;

import osrodek.kadra.graf.Krawedz;
import osrodek.kadra.graf.NieuporzadkowanaParaWezlow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Wyznacza odpowiednie wielkości zagięć krawędzi.
 *
 * <p>Zaginanie jest konieczne, jeśli między daną parą węzłów jest więcej niż jedna krawędź.
 * W przeciwnym razie wszystkie krawędzie łączące dane dwa węzły by się pokrywały.
 */
public class ZaginaczKrawedzi {
    /**
     * Schemat wartości zagięć w zależności od liczby krawędzi między dwoma węzłami (wyznaczony
     * empirycznie). Klasa nie pozwala na więcej niż MAKS_KRAWEDZI_W_GRUPIE krawędzi między dwoma
     * węzłami.
     */
    private static final List<List<Integer>> ZAGIECIA = List.of(
        List.of(0),
        List.of(13, -13),
        List.of(28, 0, -28),
        List.of(40, 13, -13, -40),
        List.of(65, 28, 0, -28, -65)
    );
    private static final int MAKS_KRAWEDZI_W_GRUPIE = ZAGIECIA.size();

    /**
     * Wyznacza odpowiednią (czytelną) wielkość zagięcia każdej krawędzi.
     *
     * @param krawedzie lista krawędzi do pozaginania.
     * @return lista krawędzi z wyznaczonymi zagięciami (być może w innej kolejności niż wejściowa
     * lista krawędzi).
     */
    public static ArrayList<KrawedzZZagieciem> pozaginajKrawedzie(Collection<Krawedz> krawedzie) {
        if (krawedzie == null) {
            throw new IllegalArgumentException("Lista krawędzi nie może być nullem.");
        }

        TreeMap<NieuporzadkowanaParaWezlow, ArrayList<Krawedz>> grupyKrawedzi =
            grupujPoParachWezlow(krawedzie);

        ArrayList<KrawedzZZagieciem> krawedzieZZagieciem = new ArrayList<>();
        for (var grupa : grupyKrawedzi.values()) {
            krawedzieZZagieciem.addAll(pozaginajKrawedzieZGrupy(grupa));
        }
        return krawedzieZZagieciem;
    }

    private static TreeMap<NieuporzadkowanaParaWezlow, ArrayList<Krawedz>>
    grupujPoParachWezlow(Collection<Krawedz> krawedzie) {
        TreeMap<NieuporzadkowanaParaWezlow, ArrayList<Krawedz>> grupy = new TreeMap<>();
        for (Krawedz krawedz : krawedzie) {
            NieuporzadkowanaParaWezlow paraWezlow = krawedz.nieuporzadkowanaParaWezlow();
            ArrayList<Krawedz> grupa = grupy.get(paraWezlow);
            if (grupa == null) {
                grupa = new ArrayList<>();
                grupy.put(paraWezlow, grupa);
            }
            grupa.add(krawedz);
        }
        return grupy;
    }

    private static ArrayList<KrawedzZZagieciem> pozaginajKrawedzieZGrupy(
        List<Krawedz> grupaKrawedzi
    ) {
        if (grupaKrawedzi.size() > MAKS_KRAWEDZI_W_GRUPIE) {
            Krawedz krawedz = grupaKrawedzi.get(0);
            throw new IllegalStateException(String.format(
                "Dodano %d krawędzi między węzłem %d a węzłem %d, a można co najwyżej %d.",
                grupaKrawedzi.size(),
                krawedz.wezelPoczatkowy().numer(),
                krawedz.wezelKoncowy().numer(),
                MAKS_KRAWEDZI_W_GRUPIE
            ));
        }

        ArrayList<Krawedz> krawedzie = new ArrayList<>(grupaKrawedzi);
        // Sortujemy tak, żeby krawędzie biegnące od węzła A do B były po jednej stronie tablicy,
        // a krawędzie biegnące od B do A po drugiej stronie tablicy (co przełoży się potem na
        // wizualną bliskość w ramach każdej z grup). Sortowanie też po numerze krawędzi
        // zapewnia dodatkowo deterministyczną kolejność wszystkich krawędzi.
        krawedzie.sort(
            Comparator.comparing(Krawedz::uporzadkowanaParaWezlow).thenComparingInt(Krawedz::numer)
        );

        ArrayList<Integer> wstepneZagiecia = new ArrayList<>(ZAGIECIA.get(krawedzie.size() - 1));

        ArrayList<KrawedzZZagieciem> krawedzieZZagieciem = new ArrayList<>();
        for (int i = 0; i < krawedzie.size(); i++) {
            // Wzorzec ZAGIECIA zakłada, że wszystkie krawędzie prowadzą od węzła o mniejszym
            // numerze do węzła o większym numerze. Dla krawędzi biegnących w drugą stronę musimy
            // zmienić znak.
            Krawedz krawedz = krawedzie.get(i);
            int zagiecie = wstepneZagiecia.get(i);
            if (krawedz.wezelPoczatkowy().numer() > krawedz.wezelKoncowy().numer()) {
                zagiecie *= -1;
            }
            krawedzieZZagieciem.add(new KrawedzZZagieciem(krawedz, zagiecie));
        }
        return krawedzieZZagieciem;
    }
}
