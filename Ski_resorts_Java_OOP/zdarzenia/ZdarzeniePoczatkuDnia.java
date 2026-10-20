package osrodek.zdarzenia;
import osrodek.symulacja.Resort;
import osrodek.sportowcy.Sportowiec;

public class ZdarzeniePoczatkuDnia extends Zdarzenie {
    private final Sportowiec sportowiec;
    private final Resort resort;

    public ZdarzeniePoczatkuDnia(int czas, Sportowiec s, Resort r) {
        super(czas);
        this.sportowiec = s;
        this.resort = r;
    }

    @Override
    public boolean czyPrzerywaPo15() {
        return true;
    }

    @Override
    public void wykonajZdarzenie() {
        sportowiec.podejmijDecyzje(super.getCzas(), resort);
    }
}