package osrodek.sportowcy;

import osrodek.graf.Trasa;
import osrodek.symulacja.Resort;

public class SportowiecZachlanny extends SportowiecZPlanem {
    public SportowiecZachlanny(int id, int poziom, double prawdLosowania, boolean sledzony, double wDop, double wWyr, double wZnudz, double beta, int pozycja, int czas) {
        super(id, poziom, prawdLosowania, sledzony, wDop, wWyr, wZnudz, beta, pozycja, czas);
    }

    @Override
    protected Trasa wybierzTraseCel(Resort resort) {
        Trasa[] dostepneTrasy = resort.getTrasy();
        Trasa najlepsza = null;
        double maxAtr = -1.0;
        for (Trasa t : dostepneTrasy) {
            double atr = lacznaAtrakcyjnoscTrasy(t);
            if (atr > maxAtr) {
                maxAtr = atr;
                najlepsza = t;
            }
        }
        return najlepsza;
    }
}