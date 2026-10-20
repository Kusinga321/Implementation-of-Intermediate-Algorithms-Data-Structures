package osrodek.sportowcy;

import osrodek.graf.Trasa;
import osrodek.graf.PrzeszukiwaczGrafu;
import osrodek.symulacja.Resort;

import java.util.*;

public class SportowiecKolekcjoner extends SportowiecZPlanem {
    public SportowiecKolekcjoner(int id, int poziom, double prawdLosowania, boolean sledzony, double wDop, double wWyr, double wZnudz, double beta, int pozycja, int czas) {
        super(id, poziom, prawdLosowania, sledzony, wDop, wWyr, wZnudz, beta, pozycja, czas);
    }

    @Override
    protected Trasa wybierzTraseCel(Resort resort) {
        Trasa[] dostepneTrasy = resort.getTrasy();

        int minZjazdow = Integer.MAX_VALUE;
        for (Trasa t : dostepneTrasy) {
            int zjazdy = pobierzLiczbeZjazdowTrasa(t);
            if (zjazdy < minZjazdow)
                minZjazdow = zjazdy;
        }

        List<Trasa> kandydaci = new ArrayList<>();
        for (Trasa t : dostepneTrasy) {
            if (pobierzLiczbeZjazdowTrasa(t) == minZjazdow)
                kandydaci.add(t);
        }

        Map<Integer, Integer> odleglosci = PrzeszukiwaczGrafu.policzOdleglosci(resort, super.getObecnaPozycja());

        Trasa najlepsza = null;
        int minDystans = Integer.MAX_VALUE;
        double maxAtr = -1.0;

        for (Trasa t : kandydaci) {
            int dystans = odleglosci.getOrDefault(t.getPoczatkowy(), Integer.MAX_VALUE);
            if (dystans < minDystans) {
                minDystans = dystans;
                najlepsza = t;
                maxAtr = lacznaAtrakcyjnoscTrasy(t);
            } else if (dystans == minDystans) {
                double atr = lacznaAtrakcyjnoscTrasy(t);
                if (atr > maxAtr) {
                    maxAtr = atr;
                    najlepsza = t;
                }
            }
        }
        return najlepsza;
    }
}