package osrodek.graf;

import osrodek.symulacja.Resort;

import java.util.*;

public final class PrzeszukiwaczGrafu {

    private PrzeszukiwaczGrafu() {}

    public static List<Krawedz> znajdzNajkrotszaSciezke(Resort resort, int start, int cel) {
        if (start == cel) {
            return new ArrayList<>();
        }

        Queue<Integer> kolejka = new LinkedList<>();
        Map<Integer, Krawedz> krawedzDo = new HashMap<>();
        Map<Integer, Integer> poprzednik = new HashMap<>();
        Set<Integer> odwiedzone = new HashSet<>();

        kolejka.add(start);
        odwiedzone.add(start);

        while (!kolejka.isEmpty()) {
            int u = kolejka.poll();
            if (u == cel) {
                break;
            }

            for (Krawedz k : resort.getKrawedzieWychodzace(u)) {
                int v = k.getKoncowy();
                if (!odwiedzone.contains(v)) {
                    odwiedzone.add(v);
                    krawedzDo.put(v, k);
                    poprzednik.put(v, u);
                    kolejka.add(v);
                }
            }
        }

        if (!poprzednik.containsKey(cel) && start != cel) {
            return null; // Nie ma żadnej ścieżki do celu
        }

        LinkedList<Krawedz> sciezka = new LinkedList<>();
        Integer obecny = cel;
        while (obecny != null && obecny != start) {
            sciezka.addFirst(krawedzDo.get(obecny));
            obecny = poprzednik.get(obecny);
        }
        return sciezka;
    }

    public static Map<Integer, Integer> policzOdleglosci(Resort resort, int start) {
        Queue<Integer> kolejka = new LinkedList<>();
        Map<Integer, Integer> odleglosci = new HashMap<>();

        kolejka.add(start);
        odleglosci.put(start, 0);

        while (!kolejka.isEmpty()) {
            int u = kolejka.poll();
            int odleglosc = odleglosci.get(u);
            for (Krawedz k : resort.getKrawedzieWychodzace(u)) {
                int v = k.getKoncowy();
                if (!odleglosci.containsKey(v)) {
                    odleglosci.put(v, odleglosc + 1);
                    kolejka.add(v);
                }
            }
        }
        return odleglosci;
    }
}