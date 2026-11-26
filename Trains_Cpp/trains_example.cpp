/**
 * Kurs programowania w C++, 2026
 * Przykład do zadania nr 3 ,,Pociągi''
 * Plik:        trains_example.cpp
 * Autor:       Jakub Piotrowicz
 * Utworzono:   27.10.2026
 */

#include "trains.h"

#ifdef NDEBUG
    #undef NDEBUG
#endif

#include <cassert>
#include <cstddef>
#include <iostream>
#include <utility>

using std::cout;

namespace {
    void series_examples() {
        constexpr WagonSeries compact{WagonType::SECOND_CLASS, 60};
        static_assert(compact.seats() == 60);
        static_assert(compact != STANDARD_SECOND_CLASS);

        // Niejawnie konwertujemy typ wagonu na serię standardową.
        constexpr WagonSeries sleeper = WagonType::SLEEPER;
        static_assert(sleeper == STANDARD_SLEEPER);

        cout << compact << '\n' << STANDARD_DINING << "\n\n";
    }

    void wagon_examples() {
        assert(Wagon::in_service() == 0);
        assert(Wagon::ever_built() == 0);

        Wagon w1{STANDARD_FIRST_CLASS};
        assert(w1.number() == 1);
        assert(w1.series() == STANDARD_FIRST_CLASS);

        // Kopiowanie buduje nowy wagon tej samej serii.
        Wagon w2{w1};
        assert(w2.number() == 2);
        assert(w2.series() == w1.series());
        assert(Wagon::in_service() == 2);
        assert(Wagon::ever_built() == 2);

        // Przenoszenie zachowuje numer, a obiekt źródłowy staje się pusty.
        Wagon w3{std::move(w2)};
        assert(w3.number() == 2);
        assert(!w2);
        assert(Wagon::in_service() == 2);
        assert(Wagon::ever_built() == 2);

        // Przypisanie kopiujące to przebudowa – wagon zachowuje swój numer.
        Wagon w4{WagonType::SLEEPER};
        assert(w4.number() == 3);
        w4 = w1;
        assert(w4.number() == 3);
        assert(w4.series() == STANDARD_FIRST_CLASS);

        // Przypisanie kopiujące do pustego obiektu buduje nowy wagon.
        w2 = w1;
        assert(w2.number() == 4);
        assert(Wagon::in_service() == 4);

        // Przypisanie przenoszące – wagon nr 3 przestaje istnieć.
        w4 = std::move(w3);
        assert(w4.number() == 2);
        assert(!w3);
        assert(Wagon::in_service() == 3);

        cout << w1 << '\n' << w4 << '\n' << w3 << "\n\n";
    }

    void temporary_examples() {
        // Wszystkie wagony z poprzedniej funkcji już nie istnieją.
        assert(Wagon::in_service() == 0);

        // Obiekt tymczasowy istnieje do końca pełnego wyrażenia.
        assert(Wagon{STANDARD_DINING}.number() == 5 &&
               Wagon::in_service() == 1);
        assert(Wagon::in_service() == 0);

        {
            // Referencja przedłuża życie obiektu tymczasowego.
            Wagon const& wagon = Wagon{STANDARD_DINING};
            assert(wagon.number() == 6);
            assert(Wagon::in_service() == 1);
        }
        assert(Wagon::in_service() == 0);
    }

    void locomotive_examples() {
        Locomotive eu07{"EU07-005", 3};
        assert(eu07.designation() == "EU07-005");

        // Lokomotyw nie można kopiować, ale można je przenosić.
        Locomotive moved{std::move(eu07)};
        assert(moved);
        assert(!eu07);

        Locomotive none;
        assert(!none);

        cout << moved << '\n' << none << "\n\n";
    }

    void train_examples() {
        Wagon first{STANDARD_FIRST_CLASS};
        Wagon second{STANDARD_SECOND_CLASS};
        assert(first.number() == 7);
        assert(second.number() == 8);

        // Lista inicjująca przechowuje kopie l-wartości (wagony nr 9 i 10).
        // Jej elementy są stałe, więc konstruktor składu może je jedynie
        // skopiować (wagony nr 11 i 12). Wagony nr 9 i 10 przestają istnieć
        // wraz z listą, po zakończeniu tworzenia składu.
        Train train{Locomotive{"EU07-005", 3}, {first, second}};
        assert(train.size() == 2);
        assert(train[0].number() == 11);
        assert(train[1].number() == 12);
        assert(Wagon::ever_built() == 12);
        assert(Wagon::in_service() == 4);

        // Obiekty tymczasowe trafiają na listę bez kopiowania (wagony nr 13
        // i 14), ale konstruktor składu i tak musi je skopiować. Lokomotywa
        // SM42 uciągnie tylko jeden wagon, więc kopia drugiego nie powstaje.
        Train shunter{Locomotive{"SM42-100", 1},
                      {Wagon{STANDARD_SLEEPER}, Wagon{STANDARD_DINING}}};
        assert(shunter.size() == 1);
        assert(shunter[0].number() == 15);
        assert(Wagon::ever_built() == 15);

        // Operacja std::move przenosi wagon na listę (obiekt third staje się pusty), ale
        // konstruktor składu i tak musi go skopiować. W składzie jest więc nowy
        // wagon nr 17, a przeniesiony wagon nr 16 przestaje istnieć razem z listą.
        Wagon third{STANDARD_DINING};
        assert(third.number() == 16);
        Train dining{Locomotive{"SM42-101", 1}, {std::move(third)}};
        assert(!third);
        assert(dining[0].number() == 17);
        assert(Wagon::ever_built() == 17);
        assert(Wagon::in_service() == 6);

        // Doczepienie l-wartości buduje nowy wagon.
        train += first;
        assert(train.size() == 3);
        assert(train[2].number() == 18);

        // Skład jest pełny, więc wagon nie jest ani doczepiany, ani przenoszony.
        train += std::move(second);
        assert(train.size() == 3);
        assert(second);
        assert(second.number() == 8);

        assert(train.capacity() == 3);
        assert(train.seats() == 54 + 80 + 54);
        assert(train.locomotive().designation() == "EU07-005");
        cout << train << '\n';

        // Składów nie można kopiować, ale można je przenosić.
        Train moved{std::move(train)};
        assert(moved);
        assert(moved.size() == 3);
        assert(!train);
        assert(train.size() == 0);
        cout << train << "\n\n";
    }

    void depot_examples() {
        // Zajezdnia główna powstaje przy pierwszym wywołaniu main_depot().
        std::size_t const built = Wagon::ever_built();
        Depot& central = main_depot();
        assert(Wagon::ever_built() == built + 8);
        assert(&main_depot() == &central);
        assert(Wagon::ever_built() == built + 8);
        assert(central.locomotives() == 2);
        assert(central.wagons() == 8);
        assert(central.wagons(STANDARD_SECOND_CLASS) == 4);

        // Dyżurny utworzony bez parametru obsługuje zajezdnię główną.
        Dispatcher dispatcher;
        assert(&dispatcher.yard() == &central);

        Train express = dispatcher.assemble(WagonType::SECOND_CLASS, 3);
        assert(express.locomotive().designation() == "EU07-001");
        assert(express.size() == 3);

        // Wagony zostały przeniesione z zajezdni – zachowały swoje numery.
        assert(express[0].number() == built + 2);
        assert(express[2].number() == built + 4);
        assert(central.locomotives() == 1);
        assert(central.wagons() == 5);
        assert(central.wagons(STANDARD_SECOND_CLASS) == 1);

        express += Wagon{STANDARD_DINING};
        cout << express << '\n';

        // Dyżurny może obsługiwać także inną zajezdnię.
        Depot local;
        local += Locomotive{"SM42-100", 2};
        local += Wagon{STANDARD_SLEEPER};
        Dispatcher local_dispatcher{local};
        Train night = local_dispatcher.assemble(STANDARD_SLEEPER, 5);
        assert(night.size() == 1);
        assert(local.locomotives() == 0);
        assert(local.wagons() == 0);

        // W zajezdni nie ma już lokomotywy, więc skład jest pusty.
        Train nothing = local_dispatcher.assemble(STANDARD_SLEEPER, 1);
        assert(!nothing);

        cout << night << '\n' << nothing << '\n';

        // Zajezdnia przyjmuje tylko r-wartości (local += wagon dla l-wartości
        // się nie kompiluje) i wydaje tabor, przenosząc go.
        local += Wagon{STANDARD_DINING};
        local += Locomotive{"SM42-101", 1};
        Wagon released = local.release_wagon(STANDARD_DINING);
        assert(released);
        assert(!local.release_wagon(STANDARD_DINING));
        assert(local.release_locomotive().designation() == "SM42-101");
        assert(!local.release_locomotive());
    }
} // anonimowa przestrzeń nazw

int main() {
    series_examples();
    wagon_examples();
    temporary_examples();
    locomotive_examples();
    train_examples();
    depot_examples();
}
