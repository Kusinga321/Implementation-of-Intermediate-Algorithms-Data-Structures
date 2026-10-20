package osrodek.zdarzenia;
import osrodek.graf.Wyciag;
import osrodek.symulacja.Resort;
import osrodek.sportowcy.Sportowiec;

public class ZdarzenieZabraniaPasazerow extends Zdarzenie {
    private final Wyciag wyciag;
    private final Resort resort;

    public ZdarzenieZabraniaPasazerow(int czas, Wyciag w, Resort r) {
        super(czas);
        this.wyciag = w;
        this.resort = r;
    }

    @Override
    public boolean czyPrzerywaPo15() {
        return true;
    }

    @Override
    public void wykonajZdarzenie() {
        Sportowiec[] pasazerowie = wyciag.pobierzZKolejki(wyciag.getMaksymalnaWielkoscGrupy(), super.getCzas());
        if (pasazerowie != null && pasazerowie.length > 0) {
            int czasDojazdu = super.getCzas() + wyciag.getCzasPrzejazduSek();
            resort.dodajDoKolejki(new ZdarzenieKoncaWjazduWyciagiem(czasDojazdu, pasazerowie, wyciag, resort));
        }

        int czasNastepnego = super.getCzas() + wyciag.getOdstepCzasowy();
        if (czasNastepnego <= CZAS_OSTATECZNEGO_ZATRZYMANIA_WYCIAGOW) {
            resort.dodajDoKolejki(new ZdarzenieZabraniaPasazerow(czasNastepnego, wyciag, resort));
        }
    }
}