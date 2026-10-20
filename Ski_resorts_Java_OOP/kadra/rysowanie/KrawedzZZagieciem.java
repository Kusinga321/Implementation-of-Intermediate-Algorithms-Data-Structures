package osrodek.kadra.rysowanie;

import osrodek.kadra.graf.Krawedz;

/**
 * Reprezentuje krawędź wraz z kątem jej odchylenia od prostej łączącej końce krawędzi.
 *
 * @param krawedz krawędź grafu.
 * @param zagiecie odchylenie krawędzi (w wierzchołku początkowym) w lewo względem prostej łączącej
 *                 końce krawędzi (w stopniach). Należy do przedziału [-180, 180]. Odpowiada
 *                 "bend left" z biblioteki Tikz w Latex'u.
 *                 Kontekst:
 *                 <a href="https://tikz.dev/library-edges#pgf./tikz/bend:left">bend left</a>
 */
public record KrawedzZZagieciem(Krawedz krawedz, int zagiecie) {
    private static final int MIN_ZAGIECIE = -180;
    private static final int MAX_ZAGIECIE = 180;

    public KrawedzZZagieciem {
        // Konstruktor rekordu w skróconej formie. Kompilator na końcu konstruktora sam dodaje
        // odpowiednie przypisania na atrybuty.

        if (krawedz == null) {
            throw new IllegalArgumentException("Krawędź nie może być nullem.");
        }

        if (zagiecie < MIN_ZAGIECIE || MAX_ZAGIECIE < zagiecie) {
            throw new IllegalArgumentException(String.format(
                "Zagiecie %d nie należy do przedziału [%d, %d].",
                zagiecie,
                MIN_ZAGIECIE,
                MAX_ZAGIECIE
            ));
        }
    }
}
