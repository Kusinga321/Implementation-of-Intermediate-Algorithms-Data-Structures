package osrodek.graf;

import osrodek.sportowcy.Sportowiec;
import osrodek.symulacja.Resort;
import osrodek.zdarzenia.ZdarzenieKoncaZjazduTrasa;
import osrodek.kadra.styl.StylLinii;

public class Trasa extends Krawedz {
    private final int trudnosc;
    private final int czasPrzejazduSek;
    private final double atrakcyjnosc;
    private final double odpornosc;
    private int liczbaPrzejazdow;

    private static final int ROZNICA_TRUDNOSCI_MAX = 5;
    private static final double WAGA_SPADKU_DOPASOWANIA = 5.0;
    private static final double WAGA_WZROSTU_DOPASOWANIA = 7.0;
    private static final double MINIMALNE_DOPASOWANIE = 0.2;

    public Trasa(int nrTrasy, int nrPoczatkowego, int nrKoncowego, int trudnosc, int czasPrzejazduSek, double atrakcyjnosc, double odpornosc) {
        super(nrPoczatkowego, nrKoncowego, nrTrasy);
        this.trudnosc = trudnosc;
        this.czasPrzejazduSek = czasPrzejazduSek;
        this.atrakcyjnosc = atrakcyjnosc;
        this.odpornosc = odpornosc;
        this.liczbaPrzejazdow = 0;
    }

    public double dopasowaniePoziomu(int poziomZaawansowania) {
        if (trudnosc >= poziomZaawansowania + ROZNICA_TRUDNOSCI_MAX) {
            return 0;
        } else if (poziomZaawansowania + ROZNICA_TRUDNOSCI_MAX >= trudnosc && trudnosc >= poziomZaawansowania) {
            return 1.0 - (trudnosc - poziomZaawansowania) / WAGA_SPADKU_DOPASOWANIA;
        } else {
            return Math.max(MINIMALNE_DOPASOWANIE, 1.0 + (trudnosc - poziomZaawansowania) / WAGA_WZROSTU_DOPASOWANIA);
        }
    }

    public double wyrownanieNawierzchni() {
        return atrakcyjnosc + (1.0 - atrakcyjnosc) * Math.pow(odpornosc, liczbaPrzejazdow);
    }

    public int getTrudnosc() {
        return trudnosc;
    }

    public int getCzasPrzejazduSek() {
        return czasPrzejazduSek;
    }
    public double getAtrakcyjnosc() {
        return atrakcyjnosc;
    }
    public double getOdpornosc() {
        return odpornosc;
    }
    public int getLiczbaPrzejazdow() {
        return liczbaPrzejazdow;
    }
    public void dodajPrzejazd() {
        this.liczbaPrzejazdow++;
    }


    @Override
    public void przyjmijSportowca(Sportowiec s, int aktualnyCzas, Resort resort) {
        this.dodajPrzejazd();
        s.dodajDoHistorii(this);
        int czasKonca = aktualnyCzas + this.getCzasPrzejazduSek();
        resort.dodajDoKolejki(new ZdarzenieKoncaZjazduTrasa(czasKonca, s, this, resort));
    }

    @Override
    public String getPrefiksMapki() {
        return "t";
    }

    @Override
    public StylLinii getStylLiniiMapki() {
        return StylLinii.CIAGLA;
    }

    @Override
    public double ocenaJakoCel(Sportowiec s) {
        return s.lacznaAtrakcyjnoscTrasy(this);
    }

    @Override
    public double ocenaJakoKrok(Sportowiec s, Resort resort) {
        return this.ocenaJakoCel(s);
    }

    @Override
    public void odnotujPrzejazd(Sportowiec s) {
        s.zaktualizujZnudzeniePoPrzejezdzie(this);
    }
}