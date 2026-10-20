package osrodek.kadra.styl;

/**
 * Określa styl krawędzi.
 * <p>
 * Obecnie klasa ma tylko jeden atrybut, więc osobna klasa jest trochę na wyrost. Natomiast w
 * przyszłości (nie w tym zadaniu) mogłoby się pojawić tutaj więcej aspektów stylu. Dzięki osobnej
 * klasie moglibyśmy dodawać nowe aspekty stylu bez modyfikacji interfejsu (w znaczeniu "sposób
 * korzystania z klasy" a nie Javowego interfejsu) klasy GeneratorMapek.
 * <p>
 * Notabene, ta klasa mogłaby być rekordem
 * (<a href="https://docs.oracle.com/en/java/javase/21/language/records.html">dokumentacja</a>),
 * ale dla uproszczenia (nie omawialiśmy rekordów na laboratorium) tworzymy zwykłą klasę.
 */
public class StylKrawedzi {
    private final StylLinii stylLinii;

    public StylKrawedzi(StylLinii stylLinii) {
        if (stylLinii == null) {
            throw new IllegalArgumentException("Styl linii nie może być nullem.");
        }
        this.stylLinii = stylLinii;
    }

    public StylLinii stylLinii() {
        return stylLinii;
    }

    @Override
    public String toString() {
        return "StylKrawedzi{stylLinii=%s}".formatted(stylLinii);
    }
}
