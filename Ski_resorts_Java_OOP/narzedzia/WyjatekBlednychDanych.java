package osrodek.narzedzia;

/**
 * Wyjątek dziedzinowy sygnalizujący błędy podczas parsowania danych wejściowych symulacji.
 */
public class WyjatekBlednychDanych extends Exception {

    public WyjatekBlednychDanych(String message) {
        super(message);
    }

    public WyjatekBlednychDanych(String message, Throwable cause) {
        super(message, cause);
    }
}