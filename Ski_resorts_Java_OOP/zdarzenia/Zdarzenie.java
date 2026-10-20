package osrodek.zdarzenia;

public abstract class Zdarzenie {
    private final int momentWykonaniaSek;

    public static final int CZAS_OTWARCIA_SEK = 32400; // 09:00:00
    public static final int CZAS_ZAMKNIECIA_SEK = 54000; // 15:00:00
    public static final int CZAS_OSTATECZNEGO_ZATRZYMANIA_WYCIAGOW = 57600; // 16:00:00

    public Zdarzenie(int czas) {
        this.momentWykonaniaSek = czas;
    }

    public int getCzas() {
        return momentWykonaniaSek;
    }

    public abstract void wykonajZdarzenie();
    public abstract boolean czyPrzerywaPo15();
}