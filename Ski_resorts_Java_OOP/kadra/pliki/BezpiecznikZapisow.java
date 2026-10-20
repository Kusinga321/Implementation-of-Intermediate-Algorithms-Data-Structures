package osrodek.kadra.pliki;

/**
 * Pilnuje, żeby na skutek błędu w kodzie symulacji nie zapisać zbyt dużo obiektów na dysku.
 *
 * <p>Jeśli wykryje, że obecne wykonanie programu zapisało już wyjątkowo dużo
 * katalogów/plików/znaków na dysku, zgłasza wyjątek. Ma to zabezpieczać przed błędami
 * programistycznymi w rodzaju wpadnięcia w pętlę nieskończoną (co mogłyby się zakończyć zajęciem
 * całego wolnego miejsca na dysku).
 */
public class BezpiecznikZapisow {
    /**
     * Limity liczby zapisanych/utworzonych na dysku katalogów, plików, znaków, których poprawne
     * rozwiązanie nie powinno nigdy przekroczyć.
     *
     * <p>Limity są ustawione ustawione z dużym zapasem, żeby praktycznie na pewno nie przeszkodziły
     * w poprawnym wykonaniu programu. Natomiast są na tyle rozsądne, że powinny wyłapać w porę
     * błąd wpadnięcia w pętlę nieskończoną.
     */
    private static final long LIMIT_KATALOGOW = 1_000L;
    private static final long LIMIT_PLIKOW = 100_000L;
    private static final long LIMIT_ZNAKOW = 1_000_000_000L;

    private static final String SUFIKS_KOMUNIKATU = (
        "To bardzo dużo. Bardzo możliwe, że symulacja wpadła w pętlę nieskończoną. Przerywam " +
            "działanie programu, żeby zapobiec zajęciu całego miejsca na dysku."
    );

    /** Liczba katalogów/plików/znaków zapisanych do tej pory na dysku. */
    private long ileKatalogow;
    private long ilePlikow;
    private long ileZnakow;

    public BezpiecznikZapisow() {
        ileKatalogow = 0;
        ilePlikow = 0;
        ileZnakow = 0;
    }

    public void odnotujNowyKatalog() {
        ileKatalogow++;
        if (ileKatalogow > LIMIT_KATALOGOW) {
            throw new WyjatekPodejrzanieDuzoZapisow(String.format(
                "Utworzenie kolejnego katalogu przekroczy limit liczby katalogów (%d). %s",
                LIMIT_KATALOGOW,
                SUFIKS_KOMUNIKATU
            ));
        }
    }

    public void odnotujNowyPlik(long ileZnakowPliku) {
        ilePlikow++;
        if (ilePlikow > LIMIT_PLIKOW) {
            throw new WyjatekPodejrzanieDuzoZapisow(String.format(
                "Utworzenie kolejnego pliku przekroczy limit liczby plików (%d). %s",
                LIMIT_PLIKOW,
                SUFIKS_KOMUNIKATU
            ));
        }

        ileZnakow += ileZnakowPliku;
        if (ileZnakow > LIMIT_ZNAKOW) {
            throw new WyjatekPodejrzanieDuzoZapisow(String.format(
                "Utworzenie kolejnego pliku przekroczy limit liczby znaków (%d). %s",
                LIMIT_ZNAKOW,
                SUFIKS_KOMUNIKATU
            ));
        }
    }
}
