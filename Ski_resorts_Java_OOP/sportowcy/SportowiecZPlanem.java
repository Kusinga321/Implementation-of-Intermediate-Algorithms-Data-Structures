package osrodek.sportowcy;

import osrodek.graf.Krawedz;
import osrodek.graf.Trasa;
import osrodek.graf.PrzeszukiwaczGrafu;
import osrodek.symulacja.Resort;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class SportowiecZPlanem extends Sportowiec {
    private final Queue<Krawedz> obecnyPlan = new LinkedList<>();

    public SportowiecZPlanem(int id, int poziom, double prawdLosowania, boolean sledzony, double wDop, double wWyr, double wZnudz, double beta, int pozycja, int czas) {
        super(id, poziom, prawdLosowania, sledzony, wDop, wWyr, wZnudz, beta, pozycja, czas);
    }

    protected abstract Trasa wybierzTraseCel(Resort resort);

    @Override
    public void podejmijDecyzje(int aktualnyCzas, Resort resort) {
        if (obecnyPlan.isEmpty()) {
            if (super.losujPrawdopodobienstwo() < super.getPrawdLosowania()) {
                Krawedz wylosowana = losujKrawedz(resort);
                if (wylosowana != null) {
                    wykonajKrok(wylosowana, aktualnyCzas, resort);
                }
                return;
            }
            Trasa cel = wybierzTraseCel(resort);
            if (cel != null) {
                List<Krawedz> sciezka = PrzeszukiwaczGrafu.znajdzNajkrotszaSciezke(resort, this.getObecnaPozycja(), cel.getPoczatkowy());
                if (sciezka != null) {
                    obecnyPlan.addAll(sciezka);
                    obecnyPlan.add(cel);
                } else {
                    Krawedz zastepcza = losujKrawedz(resort);
                    if (zastepcza != null) wykonajKrok(zastepcza, aktualnyCzas, resort);
                    return;
                }
            }
        }
        Krawedz nastepna = obecnyPlan.poll();
        if (nastepna != null)
            wykonajKrok(nastepna, aktualnyCzas, resort);
    }
}