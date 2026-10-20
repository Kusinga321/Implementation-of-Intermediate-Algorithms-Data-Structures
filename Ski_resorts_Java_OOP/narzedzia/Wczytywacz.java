package osrodek.narzedzia;

import osrodek.graf.Trasa;
import osrodek.graf.Wezel;
import osrodek.graf.Wyciag;
import osrodek.sportowcy.Sportowiec;
import osrodek.sportowcy.SportowiecKolekcjoner;
import osrodek.sportowcy.SportowiecLokalny;
import osrodek.sportowcy.SportowiecZachlanny;

import java.util.Locale;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class Wczytywacz {
    private Wezel[] wezly;
    private Wyciag[] wyciagi;
    private Trasa[] trasy;
    private Sportowiec[] sportowcy;

    public void wczytajZeStandardowegoWejscia() throws WyjatekBlednychDanych {
        Scanner wejscie = new Scanner(System.in);

        try {
            int liczbaWezlow = Integer.parseInt(pobierzNiepustaLinie(wejscie));
            wezly = new Wezel[liczbaWezlow];
            for (int i = 0; i < liczbaWezlow; i++) {
                Scanner skanerLinii = utworzSkanerLinii(pobierzNiepustaLinie(wejscie));
                wezly[i] = new Wezel(i, skanerLinii.nextInt(), skanerLinii.nextInt(), skanerLinii.nextInt(), skanerLinii.hasNext() && skanerLinii.next().equals("s"));
            }

            int liczbaWyciagow = Integer.parseInt(pobierzNiepustaLinie(wejscie));
            wyciagi = new Wyciag[liczbaWyciagow];
            for (int i = 0; i < liczbaWyciagow; i++) {
                Scanner skanerLinii = utworzSkanerLinii(pobierzNiepustaLinie(wejscie));
                int w1 = skanerLinii.nextInt();
                int w2 = skanerLinii.nextInt();
                int poczatek = (wezly[w1].getWysokosc() > wezly[w2].getWysokosc()) ? w2 : w1;
                int koniec = (poczatek == w1) ? w2 : w1;
                wyciagi[i] = new Wyciag(i, poczatek, koniec, skanerLinii.nextInt(), skanerLinii.nextInt(), skanerLinii.nextInt());
            }

            int liczbaTras = Integer.parseInt(pobierzNiepustaLinie(wejscie));
            trasy = new Trasa[liczbaTras];
            for (int i = 0; i < liczbaTras; i++) {
                Scanner skanerLinii = utworzSkanerLinii(pobierzNiepustaLinie(wejscie));
                int w1 = skanerLinii.nextInt();
                int w2 = skanerLinii.nextInt();
                int poczatek = (wezly[w1].getWysokosc() < wezly[w2].getWysokosc()) ? w2 : w1;
                int koniec = (poczatek == w1) ? w2 : w1;
                trasy[i] = new Trasa(i, poczatek, koniec, skanerLinii.nextInt(), skanerLinii.nextInt(), skanerLinii.nextDouble(), skanerLinii.nextDouble());
            }

            int liczbaGrup = Integer.parseInt(pobierzNiepustaLinie(wejscie));
            int calkowitaLiczbaSportowcow = 0;
            String[] linie1 = new String[liczbaGrup];
            String[] linie2 = new String[liczbaGrup];
            String[] linie3 = new String[liczbaGrup];

            for (int i = 0; i < liczbaGrup; i++) {
                linie1[i] = pobierzNiepustaLinie(wejscie);
                linie2[i] = pobierzNiepustaLinie(wejscie);
                linie3[i] = pobierzNiepustaLinie(wejscie);
                calkowitaLiczbaSportowcow += utworzSkanerLinii(linie1[i]).nextInt();
            }

            sportowcy = new Sportowiec[calkowitaLiczbaSportowcow];
            int aktualnyId = 0;

            for (int i = 0; i < liczbaGrup; i++) {
                Scanner linia1 = utworzSkanerLinii(linie1[i]);
                int liczbaWGrupie = linia1.nextInt();
                int poziom = linia1.nextInt();
                double spontanicznosc = linia1.nextDouble();
                double betaZnudzenia = linia1.nextDouble();
                String rodzaj = linia1.next();
                boolean sledzeni = linia1.hasNext() && linia1.next().equals("s");

                Scanner linia2 = utworzSkanerLinii(linie2[i]);
                double wagaDopasowania = linia2.nextDouble();
                double wagaWyrownania = linia2.nextDouble();
                double wagaZnudzenia = linia2.nextDouble();

                Scanner linia3 = utworzSkanerLinii(linie3[i]);
                int wezelStartowy = linia3.nextInt();

                int aktualnyCzasStartu = NarzedziaCzasu.czasNaSekundy(linia3.next());
                int odstepPomiedzy = (liczbaWGrupie > 1 && linia3.hasNextInt()) ? linia3.nextInt() : 0;

                for (int j = 0; j < liczbaWGrupie; j++) {
                    if (rodzaj.equals("L")) {
                        sportowcy[aktualnyId] = new SportowiecLokalny(aktualnyId, poziom, spontanicznosc, sledzeni, wagaDopasowania, wagaWyrownania, wagaZnudzenia, betaZnudzenia, wezelStartowy, aktualnyCzasStartu);
                    } else if (rodzaj.equals("Z")) {
                        sportowcy[aktualnyId] = new SportowiecZachlanny(aktualnyId, poziom, spontanicznosc, sledzeni, wagaDopasowania, wagaWyrownania, wagaZnudzenia, betaZnudzenia, wezelStartowy, aktualnyCzasStartu);
                    } else if (rodzaj.equals("K")) {
                        sportowcy[aktualnyId] = new SportowiecKolekcjoner(aktualnyId, poziom, spontanicznosc, sledzeni, wagaDopasowania, wagaWyrownania, wagaZnudzenia, betaZnudzenia, wezelStartowy, aktualnyCzasStartu);
                    }
                    aktualnyCzasStartu += odstepPomiedzy;
                    aktualnyId++;
                }
            }
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new WyjatekBlednychDanych("Wystąpił błąd podczas parsowania danych wejściowych. Niezgodny format pliku.", e);
        }
    }

    private String pobierzNiepustaLinie(Scanner skanerWejscia) throws WyjatekBlednychDanych {
        while (skanerWejscia.hasNextLine()) {
            String linia = skanerWejscia.nextLine().trim();
            if (!linia.isEmpty())
                return linia;
        }
        throw new WyjatekBlednychDanych("Oczekiwano więcej danych, ale wejście się skończyło.");
    }

    private Scanner utworzSkanerLinii(String linia) {
        Scanner skaner = new Scanner(linia);
        skaner.useLocale(Locale.ENGLISH);
        return skaner;
    }


    public Wezel[] getWezly() { return wezly.clone(); }
    public Wyciag[] getWyciagi() { return wyciagi.clone(); }
    public Trasa[] getTrasy() { return trasy.clone(); }
    public Sportowiec[] getSportowcy() { return sportowcy.clone(); }
}