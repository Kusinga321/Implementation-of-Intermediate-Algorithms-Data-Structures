package osrodek.graf;

import org.junit.jupiter.api.Test;
import osrodek.sportowcy.Sportowiec;
import osrodek.sportowcy.SportowiecLokalny;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WyciagTest {

    private Sportowiec stworzSportowca(int id) {
        return new SportowiecLokalny(id, 5, 0.5, false, 0.3, 0.3, 0.4, 0.5, 0, 32400);
    }

    @Test
    public void testPelnyOdjazd() {
        Wyciag wyciag = new Wyciag(1, 0, 1, 10, 3, 100);

        wyciag.dodajDoKolejki(stworzSportowca(1), 0);
        wyciag.dodajDoKolejki(stworzSportowca(2), 1);
        wyciag.dodajDoKolejki(stworzSportowca(3), 2);
        wyciag.dodajDoKolejki(stworzSportowca(4), 3);

        Sportowiec[] zabrani = wyciag.pobierzZKolejki(wyciag.getMaksymalnaWielkoscGrupy(), 10);
        wyciag.dodajPrzejazdy(zabrani.length);

        assertEquals(3, zabrani.length, "Wagonik powinien zabrać maksymalnie 3 osoby.");
        assertEquals(3, wyciag.getLacznaLiczbaPrzejazdow(), "Licznik przejazdów wyciągu powinien wynosić 3.");
    }

    @Test
    public void testCzesciowyOdjazd() {
        Wyciag wyciag = new Wyciag(2, 0, 1, 10, 3, 100);

        wyciag.dodajDoKolejki(stworzSportowca(1), 0);
        wyciag.dodajDoKolejki(stworzSportowca(2), 1);

        Sportowiec[] zabrani = wyciag.pobierzZKolejki(wyciag.getMaksymalnaWielkoscGrupy(), 10);
        wyciag.dodajPrzejazdy(zabrani.length);

        assertEquals(2, zabrani.length, "Wagonik powinien zabrać dokładnie 2 oczekujące osoby.");
        assertEquals(2, wyciag.getLacznaLiczbaPrzejazdow(), "Licznik przejazdów wyciągu powinien wynosić 2.");
    }

    @Test
    public void testMaksymalnaDlugoscKolejki() {
        Wyciag wyciag = new Wyciag(3, 0, 1, 10, 3, 100);

        wyciag.dodajDoKolejki(stworzSportowca(1), 0);
        wyciag.dodajDoKolejki(stworzSportowca(2), 1);
        wyciag.dodajDoKolejki(stworzSportowca(3), 2);
        wyciag.dodajDoKolejki(stworzSportowca(4), 3);

        wyciag.pobierzZKolejki(wyciag.getMaksymalnaWielkoscGrupy(), 10);

        wyciag.dodajDoKolejki(stworzSportowca(5), 15);
        assertEquals(4, wyciag.getMaxKolejka(), "Maksymalna historia długości kolejki powinna zapamiętać wartość 4.");
    }
}