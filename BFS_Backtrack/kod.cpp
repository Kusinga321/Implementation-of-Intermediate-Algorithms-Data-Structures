#include <bits/stdc++.h>

using namespace std;

/**
 * StanID reprezentuje unikalny hash stanu układu szklanek.
 * Wykorzystujemy system pozycyjny o zmiennej podstawie, gdzie 
 * każda szklanka to kolejna cyfra liczby.
 */
typedef unsigned long long StanID;

/**
 * Oblicza mnożniki dla systemu pozycyjnego.
 * mnozniki[i] pozwala na bezpośredni dostęp do i-tej szklanki w hashu StanID.
 */
vector<StanID> obliczMnozniki(int n, const vector<int>& pojemnosc) {
    vector<StanID> mnozniki(n);
    StanID obecnyMnoznik = 1;
    for (int i = 0; i < n; ++i) {
        mnozniki[i] = obecnyMnoznik;
        obecnyMnoznik *= (StanID)(pojemnosc[i] + 1);
    }
    return mnozniki;
}

/**
 * Konwertuje wektor stanów na unikalny identyfikator numeryczny (bijekcja).
 */
StanID wektorNaID(int n, const vector<int>& stan, const vector<StanID>& mnozniki) {
    StanID id = 0;
    for (int i = 0; i < n; ++i) 
        id += (StanID)stan[i] * mnozniki[i];
    return id;
}

/**
 * Dekoduje StanID z powrotem do reprezentacji wektorowej.
 * Wykorzystuje surowe wskaźniki dla optymalizacji wydajności wewnątrz pętli BFS.
 */
inline void idNaWektor(StanID id, int n, const vector<int>& pojemnosc, vector<int>& wynik) {
    const int* pPoj = pojemnosc.data();
    int* pWynik = wynik.data();
    for (int i = 0; i < n; ++i) {
        StanID podstawa = pPoj[i] + 1;
        pWynik[i] = (int)(id % podstawa);
        id /= podstawa;
    }
}

/**
 * Implementacja BFS oparta na wektorze (O(1) dostęp do stanu).
 * Metoda preferowana ze względu na szybkość, wymagająca ciągłego bloku pamięci.
 */
int bfs_vector(int n, const vector<int>& pojemnosc, StanID startID, StanID celID, const vector<StanID>& mnozniki, StanID maksStanow) {
    vector<int> dystans; 
    dystans.resize(maksStanow, -1);      

    queue<StanID> kolejka;
    kolejka.push(startID);
    dystans[startID] = 0;

    vector<int> wartosc(n, 0);

    while (!kolejka.empty()) {
        StanID obecneID = kolejka.front();
        kolejka.pop();

        int obecnyDystans = dystans[obecneID];
        int nastepnyDystans = obecnyDystans + 1;

        if (obecneID == celID) return obecnyDystans;
        
        idNaWektor(obecneID, n, pojemnosc, wartosc);

        for (int i = 0; i < n; ++i) {
            int woda_i = wartosc[i];

            // Akcja: Napełnianie szklanki i
            if (woda_i < pojemnosc[i]) {
                StanID nastepneID = obecneID + (StanID)(pojemnosc[i] - woda_i) * mnozniki[i];
                if (dystans[nastepneID] == -1) {
                    dystans[nastepneID] = nastepnyDystans;
                    kolejka.push(nastepneID);
                    if (nastepneID == celID) return nastepnyDystans;
                }
            }

            // Akcja: Opróżnianie szklanki i
            if (woda_i > 0) {
                StanID nastepneID = obecneID - (StanID)woda_i * mnozniki[i];
                if (dystans[nastepneID] == -1) {
                    dystans[nastepneID] = nastepnyDystans;
                    kolejka.push(nastepneID);
                    if (nastepneID == celID) return nastepnyDystans;
                }
            }

            // Akcja: Przelewanie ze szklanki i do szklanki j
            if (woda_i > 0) {
                for (int j = 0; j < n; ++j) {
                    if (i == j) continue;
                    int woda_j = wartosc[j];
                    if (woda_j < pojemnosc[j]) {
                        int ilosc = min(woda_i, pojemnosc[j] - woda_j);
                        if (ilosc > 0) {
                            StanID nastepneID = obecneID - (StanID)ilosc * mnozniki[i] 
                                                         + (StanID)ilosc * mnozniki[j];
                            if (dystans[nastepneID] == -1) {
                                dystans[nastepneID] = nastepnyDystans;
                                kolejka.push(nastepneID);
                                if (nastepneID == celID) return nastepnyDystans;
                            }
                        }
                    }
                }
            }
        }
    }
    return -1;
}

/**
 * Implementacja BFS oparta na mapie haszującej.
 * Używana jako fallback, gdy przestrzeń stanów jest zbyt duża lub brakuje pamięci.
 */
int bfs_map(int n, const vector<int>& pojemnosc, StanID startID, StanID celID, const vector<StanID>& mnozniki) {
    unordered_map<StanID, int> dystans;
    dystans.reserve(100000); 

    queue<StanID> kolejka;
    kolejka.push(startID);
    dystans[startID] = 0;

    vector<int> wartosc(n, 0);

    while (!kolejka.empty()) {
        StanID obecneID = kolejka.front();
        kolejka.pop();

        int obecnyDystans = dystans[obecneID];
        int nastepnyDystans = obecnyDystans + 1;

        if (obecneID == celID) return obecnyDystans;
        
        idNaWektor(obecneID, n, pojemnosc, wartosc);

        for (int i = 0; i < n; ++i) {
            int woda_i = wartosc[i];

            auto sprawdz_i_dodaj = [&](StanID nastepneID) -> bool {
                if (dystans.find(nastepneID) == dystans.end()) {
                    dystans[nastepneID] = nastepnyDystans;
                    kolejka.push(nastepneID);
                    return nastepneID == celID;
                }
                return false;
            };

            if (woda_i < pojemnosc[i]) {
                StanID nastepneID = obecneID + (StanID)(pojemnosc[i] - woda_i) * mnozniki[i];
                if (sprawdz_i_dodaj(nastepneID)) return nastepnyDystans;
            }

            if (woda_i > 0) {
                StanID nastepneID = obecneID - (StanID)woda_i * mnozniki[i];
                if (sprawdz_i_dodaj(nastepneID)) return nastepnyDystans;
            }

            if (woda_i > 0) {
                for (int j = 0; j < n; ++j) {
                    if (i == j) continue;
                    int woda_j = wartosc[j];
                    if (woda_j < pojemnosc[j]) {
                        int ilosc = min(woda_i, pojemnosc[j] - woda_j);
                        if (ilosc > 0) {
                            StanID nastepneID = obecneID - (StanID)ilosc * mnozniki[i] 
                                                         + (StanID)ilosc * mnozniki[j];
                            if (sprawdz_i_dodaj(nastepneID)) return nastepnyDystans;
                        }
                    }
                }
            }
        }
    }
    return -1;
}

/**
 * Narzędzia matematyczne: Największy Wspólny Dzielnik i optymalizacja stanów.
 */
int gcd(int a, int b) {
    if (a * b == 0) return max(a, b);
    while (a != b) { if (a < b) swap(a, b); a -= b; }
    return a;
}

int global_gcd(const vector<int> &x) {
    int res = x[0];
    for (size_t i = 1; i < x.size(); i++) res = gcd(x[i], res);
    return res;
}

/**
 * Sprawdza, czy cel jest osiągalny matematycznie (warunek konieczny NWD).
 */
bool gcd_optimization(const vector<int> &x, const vector<int> &y) {
    int common = global_gcd(x);
    for (auto t : y) if (t % common != 0) return false;
    return true;
}

/**
 * Optymalizacja dla przypadków, gdzie każdy cel jest albo 0, albo pełną szklanką.
 */
int triv_case_optimization(const int &n, const vector<int> &x, const vector<int> &y) {
    for (int i = 0; i < n; i++) 
        if (y[i] != x[i] && y[i] != 0) return 0;
    
    int ans = 0;
    for (auto t : y) if (t > 0) ans++;
    return ans;
}

/**
 * Główna logika sterująca wyborem algorytmu BFS w zależności od dostępnych zasobów.
 */
int solve(const int &n, const vector<int> &x, const vector<int> &y) {
    if (!gcd_optimization(x, y)) return -1;
    int triv = triv_case_optimization(n, x, y);
    if (triv > 0) return triv;

    vector<StanID> mnozniki = obliczMnozniki(n, x);
    StanID startID = 0;
    StanID celID = wektorNaID(n, y, mnozniki);

    if (startID == celID) return 0;

    // Obliczanie teoretycznej przestrzeni stanów dla alokacji wektora
    StanID maksStanow = mnozniki.back() * (x.back() + 1);
    const StanID SAFE_LIMIT = 60000000; // Próg bezpiecznej alokacji (~240MB)

    if (maksStanow > SAFE_LIMIT) {
        return bfs_map(n, x, startID, celID, mnozniki);
    } else {
        try {
            return bfs_vector(n, x, startID, celID, mnozniki, maksStanow);
        } catch (const std::bad_alloc&) {
            return bfs_map(n, x, startID, celID, mnozniki);
        }
    }
}

int main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);

    int n;
    vector<int> x, y;
    if (!(cin >> n)) return 0;

    x.resize(n); y.resize(n);
    for (int i = 0; i < n; i++) cin >> x[i] >> y[i];

    cout << solve(n, x, y) << '\n';

    return 0;
}