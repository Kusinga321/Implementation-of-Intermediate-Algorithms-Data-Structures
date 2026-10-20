package osrodek.zdarzenia;
import osrodek.graf.Trasa;
import osrodek.symulacja.Resort;
import osrodek.sportowcy.Sportowiec;

public class ZdarzenieKoncaZjazduTrasa extends Zdarzenie {
    private final Sportowiec sportowiec;
    private final Trasa trasa;
    private final Resort resort;

    public ZdarzenieKoncaZjazduTrasa(int czas, Sportowiec s, Trasa t, Resort r) {
        super(czas);
        this.sportowiec = s;
        this.trasa = t;
        this.resort = r;
    }

    @Override
    public boolean czyPrzerywaPo15() {
        return false;
    }

    @Override
    public void wykonajZdarzenie() {
        sportowiec.zakonczPrzejazd(trasa);
        if (super.getCzas() < CZAS_ZAMKNIECIA_SEK) {
            sportowiec.podejmijDecyzje(super.getCzas(), resort);
        }
    }
}