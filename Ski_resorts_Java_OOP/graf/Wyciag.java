package osrodek.graf;

import osrodek.sportowcy.Sportowiec;
import java.util.LinkedList;
import java.util.Queue;
import osrodek.symulacja.Resort;
import osrodek.zdarzenia.Zdarzenie;
import osrodek.kadra.styl.StylLinii;

public class Wyciag extends Krawedz {
    private final int odstepCzasowy;
    private final int maksymalnaWielkoscGrupy;
    private final int czasPrzejazdu;
    private int lacznaLiczbaPrzejazdow;
    private final Queue<Sportowiec> kolejkaOsob = new LinkedList<>();

    private int maxKolejka = 0;
    private long sumaDlugosciRazyCzas = 0;
    private int czasOstatniejZmianyKolejki = Zdarzenie.CZAS_OTWARCIA_SEK;

    public Wyciag(int nrWyciagu, int nrPoczatkowego, int nrKoncowego, int odstepCzasowy, int maxGrupa, int czasPrzejazdu) {
        super(nrPoczatkowego, nrKoncowego, nrWyciagu);
        this.odstepCzasowy = odstepCzasowy;
        this.maksymalnaWielkoscGrupy = maxGrupa;
        this.czasPrzejazdu = czasPrzejazdu;
        this.lacznaLiczbaPrzejazdow = 0;
    }

    private void aktualizujStatystykiKolejki(int aktualnyCzas) {
        int czasTrwania = aktualnyCzas - czasOstatniejZmianyKolejki;
        if (czasTrwania > 0) {
            sumaDlugosciRazyCzas += (long) kolejkaOsob.size() * czasTrwania;
        }
        czasOstatniejZmianyKolejki = aktualnyCzas;
        if (kolejkaOsob.size() > maxKolejka)
            maxKolejka = kolejkaOsob.size();
    }

    public void dodajDoKolejki(Sportowiec s, int aktualnyCzas) {
        aktualizujStatystykiKolejki(aktualnyCzas);
        kolejkaOsob.add(s);
        aktualizujStatystykiKolejki(aktualnyCzas);
    }

    public Sportowiec[] pobierzZKolejki(int liczbaOsob, int aktualnyCzas) {
        aktualizujStatystykiKolejki(aktualnyCzas);
        int doPobrania = Math.min(liczbaOsob, kolejkaOsob.size());
        Sportowiec[] grupa = new Sportowiec[doPobrania];
        for(int i = 0; i < doPobrania; i++)
            grupa[i] = kolejkaOsob.poll();

        aktualizujStatystykiKolejki(aktualnyCzas);
        return grupa;
    }

    public void wyladujPasazerow(Sportowiec[] pasazerowie, int aktualnyCzas, Resort resort) {
        int liczbaWyladowanych = 0;
        for (Sportowiec s : pasazerowie) {
            if (s != null) {
                liczbaWyladowanych++;
                s.zakonczPrzejazd(this);
                s.dodajDoHistorii(this);
                if (aktualnyCzas < Zdarzenie.CZAS_ZAMKNIECIA_SEK) {
                    s.podejmijDecyzje(aktualnyCzas, resort);
                }
            }
        }
        this.dodajPrzejazdy(liczbaWyladowanych);
    }

    public void dodajPrzejazdy(int liczbaOsob) {
        this.lacznaLiczbaPrzejazdow += liczbaOsob;
    }

    public int getMaxKolejka() {
        return maxKolejka;
    }

    public int getProcentZajetosci() {
        return (int) Math.round((double) getLacznaLiczbaPrzejazdow() / getMaksymalnaPojemnoscDnia() * 100);
    }

    public double getSredniaKolejka(int czasKonca) {
        aktualizujStatystykiKolejki(czasKonca);
        return (double) sumaDlugosciRazyCzas / (czasKonca - Zdarzenie.CZAS_OTWARCIA_SEK);
    }

    public int getMaksymalnaPojemnoscDnia() {
        int czasDzialania = Zdarzenie.CZAS_ZAMKNIECIA_SEK - Zdarzenie.CZAS_OTWARCIA_SEK;
        return (czasDzialania / odstepCzasowy) * maksymalnaWielkoscGrupy;
    }

    public int getLacznaLiczbaPrzejazdow() {
        return lacznaLiczbaPrzejazdow;
    }

    public int getMaksymalnaWielkoscGrupy() {
        return maksymalnaWielkoscGrupy;
    }

    public int getOdstepCzasowy() {
        return odstepCzasowy;
    }

    public int getCzasPrzejazduSek() {
        return czasPrzejazdu;
    }

    @Override
    public void przyjmijSportowca(Sportowiec s, int aktualnyCzas, Resort resort) {
        this.dodajDoKolejki(s, aktualnyCzas);
    }

    @Override
    public String getPrefiksMapki() {
        return "w";
    }

    @Override
    public StylLinii getStylLiniiMapki() {
        return StylLinii.PRZERYWANA;
    }

    @Override
    public double ocenaJakoCel(Sportowiec s) {
        return -1.0;
    }

    @Override
    public double ocenaJakoKrok(Sportowiec s, Resort resort) {
        double maxAtr = -1.0;
        for (Krawedz k : resort.getKrawedzieWychodzace(this.getKoncowy())) {
            double atr = k.ocenaJakoCel(s);
            if (atr > maxAtr) {
                maxAtr = atr;
            }
        }
        return maxAtr;
    }

    @Override
    public void odnotujPrzejazd(Sportowiec s) {
    }
}