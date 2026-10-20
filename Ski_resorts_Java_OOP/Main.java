package osrodek;

import osrodek.narzedzia.Wczytywacz;
import osrodek.narzedzia.WyjatekBlednychDanych;
import osrodek.kadra.pliki.WyjatekSystemuPlikow;
import osrodek.symulacja.RaportStatystyk;
import osrodek.symulacja.Resort;
import osrodek.symulacja.WizualizatorOsrodka;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Musisz podać ścieżkę do zapisu mapek jako argument wywołania (args[0])!");
            return;
        }

        try {
            Wczytywacz wczytywacz = new Wczytywacz();
            wczytywacz.wczytajZeStandardowegoWejscia();

            Resort resort = new Resort(
                    wczytywacz.getWezly(),
                    wczytywacz.getTrasy(),
                    wczytywacz.getWyciagi(),
                    wczytywacz.getSportowcy()
            );

            resort.rozpocznijSymulacje();

            RaportStatystyk raport = new RaportStatystyk(resort);
            raport.wypiszRaport();

            WizualizatorOsrodka wizualizator = new WizualizatorOsrodka(resort);
            wizualizator.generuj(args[0]);

        } catch (WyjatekSystemuPlikow e) {
            System.err.println("Błąd zapisu plików. Upewnij się, że podałeś poprawną ścieżkę do katalogu i posiadasz w nim odpowiednie uprawnienia do zapisu.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Wystąpił nieoczekiwany błąd wewnętrzny. Prosimy o zgłoszenie tego błędu zespołowi deweloperskiemu, załączając poniższy ślad stosu (stack trace).");
            e.printStackTrace();
        }
    }
}