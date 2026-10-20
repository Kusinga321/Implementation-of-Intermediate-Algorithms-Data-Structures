package osrodek.kadra.pliki;

/**
 * Sygnalizuje, że operacja GeneratoraMapek nie powiodła się ze względu na problem z związany
 * systemem plików.
 */
public class WyjatekSystemuPlikow extends Exception {
    public WyjatekSystemuPlikow(String wiadomosc) {
        super(wiadomosc);
    }

    public WyjatekSystemuPlikow(String wiadomosc, Throwable przyczyna) {
        super(wiadomosc, przyczyna);
    }
}
