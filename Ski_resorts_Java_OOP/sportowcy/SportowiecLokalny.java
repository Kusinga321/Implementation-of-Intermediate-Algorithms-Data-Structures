package osrodek.sportowcy;

import osrodek.graf.Krawedz;
import osrodek.symulacja.Resort;

public class SportowiecLokalny extends Sportowiec {
    public SportowiecLokalny(int id, int poziom, double prawdLosowania, boolean sledzony, double wDop, double wWyr, double wZnudz, double beta, int pozycja, int czas) {
        super(id, poziom, prawdLosowania, sledzony, wDop, wWyr, wZnudz, beta, pozycja, czas);
    }

    @Override
    public void podejmijDecyzje(int aktualnyCzas, Resort resort) {
        if (super.losujPrawdopodobienstwo() < super.getPrawdLosowania()) {
            Krawedz losowa = losujKrawedz(resort);
            if (losowa != null)
                wykonajKrok(losowa, aktualnyCzas, resort);
            return;
        }

        double najwyzsza = -1.0;
        Krawedz ostatecznyWybor = null;

        for (Krawedz k : resort.getKrawedzieWychodzace(super.getObecnaPozycja())) {
            double atr = k.ocenaJakoKrok(this, resort);
            if (atr > najwyzsza) {
                najwyzsza = atr;
                ostatecznyWybor = k;
            }
        }

        if (ostatecznyWybor != null) {
            wykonajKrok(ostatecznyWybor, aktualnyCzas, resort);
        }
    }
}