package osrodek.zdarzenia;
import osrodek.graf.Wyciag;
import osrodek.symulacja.Resort;
import osrodek.sportowcy.Sportowiec;

public class ZdarzenieKoncaWjazduWyciagiem extends Zdarzenie {
    private final Sportowiec[] pasazerowie;
    private final Wyciag wyciag;
    private final Resort resort;

    public ZdarzenieKoncaWjazduWyciagiem(int czas, Sportowiec[] pasazerowie, Wyciag wyciag, Resort resort) {
        super(czas);
        this.pasazerowie = pasazerowie;
        this.wyciag = wyciag;
        this.resort = resort;
    }

    @Override
    public boolean czyPrzerywaPo15() {
        return false;
    }

    @Override
    public void wykonajZdarzenie() {
        wyciag.wyladujPasazerow(pasazerowie, super.getCzas(), resort);
    }
}