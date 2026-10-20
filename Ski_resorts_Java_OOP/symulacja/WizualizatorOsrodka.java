package osrodek.symulacja;

import osrodek.graf.Trasa;
import osrodek.graf.Wyciag;
import osrodek.graf.Krawedz;
import osrodek.graf.Wezel;
import osrodek.sportowcy.Sportowiec;
import osrodek.zdarzenia.Zdarzenie;
import osrodek.kadra.GeneratorMapek;
import osrodek.kadra.styl.StylKrawedzi;
import osrodek.kadra.styl.StylLinii;
import osrodek.kadra.styl.StylWezla;
import osrodek.kadra.styl.GruboscKonturu;
import osrodek.kadra.pliki.WyjatekSystemuPlikow;

import java.util.*;
import java.util.stream.Collectors;

public class WizualizatorOsrodka {
    private final Resort resort;

    public WizualizatorOsrodka(Resort resort) {
        this.resort = resort;
    }

    public void generuj(String sciezkaKatalogu) throws WyjatekSystemuPlikow {
        GeneratorMapek gen = new GeneratorMapek(sciezkaKatalogu);
        Wezel[] wezly = resort.getWezly();
        Trasa[] trasy = resort.getTrasy();
        Wyciag[] wyciagi = resort.getWyciagi();
        Sportowiec[] sportowcy = resort.getSportowcy();

        // Mapka 1: Parametry tras i wyciągów
        gen.zeruj();
        for (Wezel w : wezly)
            gen.dodajWezel(w.getNumer(), w.getX(), w.getY(), new StylWezla(w.isSkomunikowany() ? GruboscKonturu.POGRUBIONY : GruboscKonturu.ZWYKLY));

        for (Trasa t : trasy) {
            gen.dodajKrawedz(t.getPoczatkowy(), t.getKoncowy(), new StylKrawedzi(StylLinii.CIAGLA),
                    Arrays.asList(String.format("t%d: poziom: %d, czas: %ds", t.getNumer(), t.getTrudnosc(), t.getCzasPrzejazduSek()),
                            String.format(Locale.ENGLISH, "odporność: %.2f, %.5f", t.getAtrakcyjnosc(), t.getOdpornosc())));
        }

        for (Wyciag w : wyciagi) {
            gen.dodajKrawedz(w.getPoczatkowy(), w.getKoncowy(), new StylKrawedzi(StylLinii.PRZERYWANA),
                    Arrays.asList(String.format("w%d: %d os. co %ds", w.getNumer(), w.getMaksymalnaWielkoscGrupy(), w.getOdstepCzasowy()),
                            String.format("czas: %ds", w.getCzasPrzejazduSek())));
        }
        gen.tworzMapke("mapka1-parametry.tex");

        // Mapka 2: Statystyki
        gen.zeruj();
        for (Wezel w : wezly)
            gen.dodajWezel(w.getNumer(), w.getX(), w.getY(), new StylWezla(w.isSkomunikowany() ? GruboscKonturu.POGRUBIONY : GruboscKonturu.ZWYKLY));

        for (Trasa t : trasy) {
            gen.dodajKrawedz(t.getPoczatkowy(), t.getKoncowy(), new StylKrawedzi(StylLinii.CIAGLA),
                    Arrays.asList(String.format(Locale.ENGLISH, "t%d: śnieg: %.2f", t.getNumer(), t.wyrownanieNawierzchni()),
                            String.format("zjazdy: %d", t.getLiczbaPrzejazdow())));
        }

        for (Wyciag w : wyciagi) {
            int procent = w.getProcentZajetosci();
            gen.dodajKrawedz(w.getPoczatkowy(), w.getKoncowy(), new StylKrawedzi(StylLinii.PRZERYWANA),
                    Arrays.asList(String.format("w%d: kol: %d(śr), %d(maks)", w.getNumer(), (int) Math.round(w.getSredniaKolejka(Zdarzenie.CZAS_ZAMKNIECIA_SEK)), w.getMaxKolejka()),
                            String.format("wjazdy: %d / %d (%d%%)", w.getLacznaLiczbaPrzejazdow(), w.getMaksymalnaPojemnoscDnia(), procent)));
        }
        gen.tworzMapke("mapka2-statystyki.tex");

        // Mapka 3: Historia przejazdów sportowca
        for (Sportowiec s : sportowcy) {
            if (s.isSledzony()) {
                gen.zeruj();
                for (Wezel w : wezly)
                    gen.dodajWezel(w.getNumer(), w.getX(), w.getY(), new StylWezla(w.isSkomunikowany() ? GruboscKonturu.POGRUBIONY : GruboscKonturu.ZWYKLY));

                Map<Krawedz, List<Integer>> uzycia = new HashMap<>();
                for (Sportowiec.PrzejazdHistoria ph : s.getHistoria()) {
                    uzycia.putIfAbsent(ph.krawedz(), new ArrayList<>());
                    uzycia.get(ph.krawedz()).add(ph.numer());
                }

                for (Map.Entry<Krawedz, List<Integer>> wpis : uzycia.entrySet()) {
                    Krawedz k = wpis.getKey();
                    List<Integer> numery = wpis.getValue();
                    String prefix = k.getPrefiksMapki();
                    StylLinii styl = k.getStylLiniiMapki();

                    String tekst = prefix + k.getNumer() + "(" + numery.size() + "): " + numery.stream().map(String::valueOf).collect(Collectors.joining(","));
                    gen.dodajKrawedz(k.getPoczatkowy(), k.getKoncowy(), new StylKrawedzi(styl), tekst);
                }
                gen.tworzMapke("mapka3-historia-" + s.getNumerSportowca() + ".tex");
            }
        }
    }
}