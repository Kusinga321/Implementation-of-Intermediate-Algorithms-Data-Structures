package osrodek.sportowcy;

import osrodek.graf.Krawedz;
import osrodek.graf.Trasa;
import osrodek.symulacja.Resort;

import java.util.*;

public abstract class Sportowiec {
    public record PrzejazdHistoria(Krawedz krawedz, int numer) {}

    private static final Random GENERATOR = new Random();

    private final int numerSportowca;
    private final int poziomZaawansowania;
    private final double prawdLosowania;
    private final boolean czySledzony;
    private final double wagaDopasowania;
    private final double wagaWyrownania;
    private final double wagaZnudzenia;
    private final double betaZnudzenia;
    private int obecnaPozycja;
    private final int czasPrzybycia;
    private final Map<Trasa, Integer> liczbaZjazdowCache = new HashMap<>();
    private int lacznaLiczbaZjazdow = 0;
    private final Map<Trasa, Double> ostatnieZnudzenie = new HashMap<>();
    private final Map<Trasa, Integer> indeksOstatniegoZjazdu = new HashMap<>();
    private final List<PrzejazdHistoria> historia = new ArrayList<>();
    private int licznikPrzejazdowGlobalny = 0;

    public Sportowiec(int id, int poziom, double prawdLosowania, boolean sledzony,
                      double wDop, double wWyr, double wZnudz, double beta,
                      int pozycja, int czas) {
        this.numerSportowca = id;
        this.poziomZaawansowania = poziom;
        this.prawdLosowania = prawdLosowania;
        this.czySledzony = sledzony;
        this.wagaDopasowania = wDop;
        this.wagaWyrownania = wWyr;
        this.wagaZnudzenia = wZnudz;
        this.betaZnudzenia = beta;
        this.obecnaPozycja = pozycja;
        this.czasPrzybycia = czas;
    }

    public double pobierzObecneZnudzenie(Trasa t) {
        double z_k = ostatnieZnudzenie.getOrDefault(t, 0.0);
        int k = indeksOstatniegoZjazdu.getOrDefault(t, 0);
        int i = lacznaLiczbaZjazdow - k;
        return z_k * Math.pow(1.0 - betaZnudzenia, i);
    }

    public double lacznaAtrakcyjnoscTrasy(Trasa t) {
        return wagaDopasowania * t.dopasowaniePoziomu(poziomZaawansowania)
                + wagaWyrownania * t.wyrownanieNawierzchni()
                + wagaZnudzenia * (1.0 - pobierzObecneZnudzenie(t));
    }

    public void dodajDoHistorii(Krawedz k) {
        licznikPrzejazdowGlobalny++;
        historia.add(new PrzejazdHistoria(k, licznikPrzejazdowGlobalny));

        k.odnotujPrzejazd(this);
    }

    public void zaktualizujZnudzeniePoPrzejezdzie(Trasa t) {
        double obecneZ = pobierzObecneZnudzenie(t);
        double noweZ = betaZnudzenia * 1.0 + (1.0 - betaZnudzenia) * obecneZ;
        lacznaLiczbaZjazdow++;
        ostatnieZnudzenie.put(t, noweZ);
        indeksOstatniegoZjazdu.put(t, lacznaLiczbaZjazdow);

        liczbaZjazdowCache.put(t, liczbaZjazdowCache.getOrDefault(t, 0) + 1);
    }

    protected void wykonajKrok(Krawedz wybor, int aktualnyCzas, Resort resort) {
        wybor.przyjmijSportowca(this, aktualnyCzas, resort);
    }

    protected Krawedz losujKrawedz(Resort resort) {
        List<Krawedz> opcje = resort.getKrawedzieWychodzace(obecnaPozycja);
        if (opcje.isEmpty()) {
            return null;
        }
        return opcje.get(GENERATOR.nextInt(opcje.size()));
    }

    public abstract void podejmijDecyzje(int aktualnyCzas, Resort resort);

    public int getNumerSportowca() {
        return numerSportowca;
    }

    public boolean isSledzony() {
        return czySledzony;
    }

    public int getObecnaPozycja() {
        return obecnaPozycja;
    }

    public void zakonczPrzejazd(Krawedz krawedz) {
        this.obecnaPozycja = krawedz.getKoncowy();
    }
    public int getCzasPrzybycia() {
        return czasPrzybycia;
    }

    protected double getPrawdLosowania() {
        return prawdLosowania;
    }


    protected double losujPrawdopodobienstwo() {
        return GENERATOR.nextDouble();
    }


    public List<PrzejazdHistoria> getHistoria() {
        return java.util.Collections.unmodifiableList(historia);
    }

    public int pobierzLiczbeZjazdowTrasa(Trasa t) {
        return liczbaZjazdowCache.getOrDefault(t, 0);
    }
}