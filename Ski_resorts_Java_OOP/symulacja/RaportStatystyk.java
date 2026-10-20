package osrodek.symulacja;

import osrodek.graf.Trasa;
import osrodek.graf.Wyciag;
import osrodek.zdarzenia.Zdarzenie;

import java.util.Locale;

/**
 * Odpowiada za wypisywanie tekstowych statystyk podsumowujących dzień na standardowe wyjście.
 */
public final class RaportStatystyk {
    private final Resort resort;

    public RaportStatystyk(Resort resort) {
        this.resort = resort;
    }

    public void wypiszRaport() {
        System.out.println("Statystyki Tras: ");
        for (Trasa t : resort.getTrasy()) {
            System.out.printf(Locale.ENGLISH, "Trasa %d: zjazdy: %d, wyrównanie: %.2f\n",
                    t.getNumer(),
                    t.getLiczbaPrzejazdow(),
                    t.wyrownanieNawierzchni()
            );
        }

        System.out.println("\nStatystyki wyciągów: ");
        for (Wyciag w : resort.getWyciagi()) {
            System.out.printf("Wyciąg %d: kol: %d(śr), %d(maks), wjazdy: %d / %d (%d%%)\n",
                    w.getNumer(),
                    (int) Math.round(w.getSredniaKolejka(Zdarzenie.CZAS_ZAMKNIECIA_SEK)),
                    w.getMaxKolejka(),
                    w.getLacznaLiczbaPrzejazdow(),
                    w.getMaksymalnaPojemnoscDnia(),
                    w.getProcentZajetosci()
            );
        }
    }
}