package osrodek.symulacja;

import osrodek.graf.Trasa;
import osrodek.graf.Wyciag;
import osrodek.graf.Krawedz;
import osrodek.graf.Wezel;
import osrodek.sportowcy.Sportowiec;
import osrodek.zdarzenia.Zdarzenie;
import osrodek.zdarzenia.ZdarzeniePoczatkuDnia;
import osrodek.zdarzenia.ZdarzenieZabraniaPasazerow;

import java.util.*;
import java.util.Arrays;

public class Resort {
    private Wezel[] wezly;
    private Trasa[] trasy;
    private Wyciag[] wyciagi;
    private Sportowiec[] sportowcy;
    private PriorityQueue<OpakowaneZdarzenie> kolejka;
    private long licznikZdarzen = 0;
    private final Map<Integer, List<Krawedz>> listaSasiedztwa = new HashMap<>();

    private record OpakowaneZdarzenie(Zdarzenie zdarzenie, long numerDodania) implements Comparable<OpakowaneZdarzenie> {
        @Override
        public int compareTo(OpakowaneZdarzenie inne) {
            int porownanieCzasu = Integer.compare(this.zdarzenie.getCzas(), inne.zdarzenie.getCzas());
            if (porownanieCzasu != 0) return porownanieCzasu;
            return Long.compare(this.numerDodania, inne.numerDodania);
        }
    }


    public Resort(Wezel[] wezly, Trasa[] trasy, Wyciag[] wyciagi, Sportowiec[] sportowcy) {
        this.wezly = wezly.clone();
        this.trasy = trasy.clone();
        this.wyciagi = wyciagi.clone();
        this.sportowcy = sportowcy.clone();
        this.kolejka = new PriorityQueue<>();

        // Budowanie listy sąsiedztwa
        for (Wezel w : wezly) {
            listaSasiedztwa.put(w.getNumer(), new ArrayList<>());
        }
        for (Trasa t : trasy) {
            listaSasiedztwa.get(t.getPoczatkowy()).add(t);
        }
        for (Wyciag w : wyciagi) {
            listaSasiedztwa.get(w.getPoczatkowy()).add(w);
        }

        for (List<Krawedz> sasiedzi : listaSasiedztwa.values()) {
            sasiedzi.sort(Comparator.comparingInt(Krawedz::getNumer));
        }
    }

    public void rozpocznijSymulacje() {
        for (Sportowiec s : sportowcy) {
            kolejka.add(new OpakowaneZdarzenie(new ZdarzeniePoczatkuDnia(s.getCzasPrzybycia(), s, this), licznikZdarzen++));
        }
        for (Wyciag w : wyciagi) {
            kolejka.add(new OpakowaneZdarzenie(new ZdarzenieZabraniaPasazerow(Zdarzenie.CZAS_OTWARCIA_SEK, w, this), licznikZdarzen++));
        }

        while (!kolejka.isEmpty()) {
            Zdarzenie z = kolejka.poll().zdarzenie();
            if (z.getCzas() >= Zdarzenie.CZAS_ZAMKNIECIA_SEK && z.czyPrzerywaPo15())
                continue;
            z.wykonajZdarzenie();
        }
    }

    public List<Krawedz> getKrawedzieWychodzace(int u) {
        return Collections.unmodifiableList(listaSasiedztwa.getOrDefault(u, Collections.emptyList()));
    }

    public void dodajDoKolejki(Zdarzenie z) {
        kolejka.add(new OpakowaneZdarzenie(z, licznikZdarzen++));
    }

    public Trasa[] getTrasy() {
        return Arrays.copyOf(trasy, trasy.length);
    }

    public Wezel[] getWezly() {
        return Arrays.copyOf(wezly, wezly.length);
    }

    public Wyciag[] getWyciagi() {
        return Arrays.copyOf(wyciagi, wyciagi.length);
    }

    public Sportowiec[] getSportowcy() {
        return Arrays.copyOf(sportowcy, sportowcy.length);
    }
}