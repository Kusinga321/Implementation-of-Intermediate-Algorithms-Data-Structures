package osrodek.kadra.pliki;

/**
 * Ostrzega o podejrzanie dużej liczbie zapisów na dysk.
 *
 * <p>Taka sytuacja oznacza, że symulacja prawdopodobnie wpadła w pętlę nieskończoną, która zapisuje
 * coraz więcej plików lub katalogów na dysku.
 */
public class WyjatekPodejrzanieDuzoZapisow extends RuntimeException {
    public WyjatekPodejrzanieDuzoZapisow(String message) {
        super(message);
    }
}
