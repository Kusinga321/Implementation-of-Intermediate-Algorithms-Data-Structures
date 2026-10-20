DEFAULT REL

section .data
newline db 10 ; Znak nowej linii

section .bss
text_start resq 1 ; Adres bazy bufora wejściowego zmapowanego przez sys_mmap
line_table resq 95 ; Tablica wskaźników do wierszy tekstu (max 95 znaków ASCII)
line_count resq 1 ; Zliczona liczba poprawnie wyodrębnionych wierszy reguł
total_bytes resq 1 ; Całkowity rozmiar tekstu wczytanego ze standardowego wejścia
capacity resq 1 ; Bieżący rozmiar przestrzeni wirtualnej zaalokowanej na wejście
rule_dict resq 127 ; Tablica wskaźników na łańcuchy zastąpień (indeksowana ASCII)
buf_A resq 1 ; Wskaźnik na bieżący bufor roboczy zawierający tekst fraktala
buf_B resq 1 ; Wskaźnik na bufor docelowy przyjmujący wynik po podmianach
buf_A_cap resq 1 ; Całkowita przydzielona pojemność pamięci dla bufora A
buf_B_cap resq 1 ; Całkowita przydzielona pojemność pamięci dla bufora B
buf_A_len resq 1 ; Rozmiar bieżącego tekstu w buforze źródłowym (w bajtach)
buf_B_len resq 1 ; Liczba bajtów dotychczas zapisana do bufora docelowego
iterations resq 1 ; Ostatecznie wyliczona liczba iteracji algorytmu (parametr n)


section .text
global _start

; ==============================================================================
; WALIDACJA WEJŚCIA I KONWERSJA ARGUMENTU (ATOI)
; Opis:
; Sprawdza poprawność uruchomienia programu i zamienia argument tekstowy (ASCII)
; na wartość całkowitoliczbową, uwzględniając zabezpieczenia przed przepełnieniem.
;
; Użycie rejestrów:
; rdi - liczba argumentów (argc) zdejmywana ze stosu
; rsi - wskaźnik na kolejne znaki argumentu wejściowego (argv[1])
; r11 - akumulator przechowujący ostateczną wartość iteracji (n)
; rcx - kod ASCII aktualnie przetwarzanego znaku (później jego wartość)
; rax - rejestr pomocniczy do weryfikacji progu przepełnienia rejestru
; ==============================================================================
_start:
cmp qword [rsp], 2 ; Sprawdzenie, czy podano dokładnie jeden argument programu
jne blad_mmap
mov rsi, [rsp + 16] ; Pobranie wskaźnika na początek łańcucha znaków dla 'n'
xor r11, r11 ; Inicjalizacja akumulatora liczby iteracji zerem
cmp byte [rsi], 0 ; Weryfikacja, czy przekazany łańcuch argumentu nie jest pusty
je blad_mmap

atoi:
movzx rcx, byte [rsi] ; Pobranie bieżącego znaku z łańcucha argumentu wejściowego
cmp rcx, 0 ; Sprawdzenie osiągnięcia końca łańcucha
je koniec_atoi
cmp rcx, 48 ; Walidacja dolnej granicy dopuszczalnych znaków (znak '0')
jl blad_mmap
cmp rcx, 57 ; Walidacja górnej granicy dopuszczalnych znaków (znak '9')
jg blad_mmap

algorytm:
mov rax, 429496729 ; Próg bezpieczeństwa przed operacją mnożenia przez 10 (2^32 - 1)
cmp r11, rax
ja blad_mmap
jb safe_mul ; Przeskok do bezpiecznej operacji jeśli jesteśmy poniżej progu

mov rax, rcx ; Przypadek graniczny: sprawdzenie samej cyfry dodawanej
sub rax, 48 ; Konwersja kodu ASCII na surową wartość liczbową
cmp rax, 5
ja blad_mmap ; Zakończenie błędem przy przekroczeniu górnej granicy int32

safe_mul:
imul r11, 10
sub rcx, 48 ; Wydobycie faktycznej wartości z kodu ASCII znaku
add r11, rcx
inc rsi
jmp atoi

koniec_atoi:
mov [iterations], r11 ; Trwałe zapamiętanie obliczonej docelowej liczby iteracji


; ==============================================================================
; ALOKACJA PAMIĘCI I WCZYTYWANIE DANYCH WEJŚCIOWYCH (SYS_READ)
; Opis:
; Przygotowuje dynamicznie rozszerzany bufor pamięci i wczytuje do niego dane
; ze standardowego wejścia aż do napotkania EOF (End Of File).
;
; Użycie rejestrów:
; r12 - licznik całkowitej liczby dotychczas wczytanych bajtów tekstu
; rcx - baza do obliczania docelowego adresu zapisu (baza + r12)
; rdx - wyliczany rozmiar pozostałego wolnego miejsca w zaalokowanym buforze
; ==============================================================================
mov rax, 9 ; Przygotowanie numeru wywołania systemowego dla sys_mmap
xor rdi, rdi ; Pozostawienie jądru systemu decyzji o adresie startowym bloku
mov rsi, 1048576 ; Rezerwacja początkowego bloku pamięci wielkości 1 Megabajta
mov [capacity], rsi ; Odnotowanie wielkości pierwszej alokacji w strukturach
mov rdx, 3 ; Ustalenie flag PROT_READ | PROT_WRITE (odczyt i modyfikacja)
mov r10, 34 ; Ustalenie flag MAP_PRIVATE | MAP_ANONYMOUS (pamięć anonimowa)
mov r8, -1 ; Wymuszona flaga braku powiązania z plikiem (fd zignorowane)
xor r9, r9 ; Offset w pliku (wymagany zerowy przy mapowaniu anonimowym)
syscall ; Żądanie przydziału pamięci dla początkowego tekstu wejściowego

cmp rax, 0 ; Weryfikacja powodzenia procesu mapowania pamięci przez system
jl blad_mmap

mov [text_start], rax ; Zapamiętanie globalnego wskaźnika na początek wczytywanych danych
mov rcx, [text_start] ; Ustalenie początkowej pozycji zapisu na bazę zaalokowanej przestrzeni
mov r12, 0 ; Ustawienie licznika dotychczas wczytanych bajtów na zero

_wczytywanie:
add r12, 4096
cmp r12, [capacity] ; Zbadanie czy 'wirtualny' dopis przebija rozmiar całej alokacji
jge powieksz_bufor ; Uruchomienie procedury rozszerzającej przy braku przestrzeni
sub r12, 4096

mov rax, 0 ; Numer wywołania sys_read (pobieranie informacji z deskryptora)
mov rdi, 0 ; Wskazanie deskryptora numer 0 reprezentującego standardowe wejście
mov rdx, [capacity] ; Skopiowanie górnego limitu pojemności pamięci dla obliczeń
sub rdx, r12 ; Wyznaczenie precyzyjnie pozostałej luki przestrzeni na nowe dane
mov rsi, [text_start] ; Powrót do twardego początku przydzielonego bufora bazowego
add rsi, r12 ; Precyzyjne wskazanie adresu, od którego jądro ma umieścić znaki
syscall ; Wstrzymanie procesu w oczekiwaniu na nadejście fragmentu tekstu

cmp rax, 0 ; Weryfikacja wielkości strumienia danych zwróconego z wywołania
je koniec_wczytywania ; Rozpoznanie EOF i naturalne zakończenie strumienia wejściowego
jl blad ; Awaryjne przerwanie programu z powodu błędu wejścia/wyjścia

add r12, rax ; Aktualizacja liczby wczytanych znaków o nową paczkę
jmp _wczytywanie


; ==============================================================================
; RELOKACJA BUFORA WEJŚCIOWEGO (SYS_MREMAP)
; Opis:
; Wykonywane, gdy bufor wejściowy ulegnie przepełnieniu. Podwaja dotychczasową
; pojemność bufora za pomocą mechanizmu MREMAP_MAYMOVE (jak wektor w C++).
; ==============================================================================
powieksz_bufor:
mov rax, 25 ; Zlecenie jądru systemu operacji na mapowaniu pamięci (sys_mremap)
mov rdi, [text_start] ; Podanie dotychczasowej bazy relokowanego bloku pamięci
mov rsi, [capacity] ; Wskazanie systemowi aktualnego zapotrzebowania w bajtach
mov rdx, [capacity] ; Bazowy rozmiar do wyliczenia poszerzonego limitu
shl rdx, 1 ; Dwukrotne zwiększenie żądanego rozmiaru bufora (x2)
mov r10, 1 ; Zezwolenie jądru na swobodne przeniesienie bloku pod nowy adres
syscall ; Transakcja żądająca nowego, dłuższego ciągłego bloku pamięci

cmp rax, 0 ; Sprawdzenie statusu zakończenia operacji reallokacji pamięci
jl blad

mov [text_start], rax ; Nadpisanie globalnej bazy prawdopodobnie nowym wskaźnikiem
mov rcx, [text_start] ; Uaktualnienie lokalnego rejestru wspierającego proces odczytu
sub r12, 4096 ; Korekta o "próbną" wartość dodaną tuż przed wejściem w reallokację
add rcx, r12 ; Przesunięcie adresu o faktyczną liczbę wczytanych wcześniej bajtów
shl qword [capacity], 1 ; Prawidłowe zaksięgowanie dwukrotnie większej nowej pojemności
jmp _wczytywanie


; ==============================================================================
; PARSOWANIE WEJŚCIA I SEGMENTACJA NA WIERSZE
; Opis:
; Zamienia znaki nowej linii ('\n') na '\0' oraz buduje
; tablicę wskaźników do początków poszczególnych wierszy tekstu.
;
; Użycie rejestrów:
; rsi - ruchomy wskaźnik do przeglądania poszczególnych znaków
; r12 - wskaźnik pełniący rolę ogranicznika bufora (baza + len)
; rbx - dynamiczny wskaźnik do wypełniania tablicy wskaźników linii
; r13 - inkrementowany licznik znalezionych odrębnych wierszy
; ==============================================================================
koniec_wczytywania:
mov [total_bytes], r12
cmp r12, 0 ; Kontrola, czy użytkownik nie przesłał zupełnie pustego pliku
je wypisz_nowa_linie ; Domyślne wyjście z samym znakiem nowej linii dla pustych danych

mov rsi, [text_start] ; Podjęcie bazowego adresu do ostatecznej weryfikacji zawartości
add rsi, r12 ; Przeskok na adres bezpośrednio za ostatnim odebranym bajtem
dec rsi ; Skorygowanie adresu, by trafiał równo w ostatni odebrany znak
cmp byte [rsi], 10 ; Kontrola obecności znaku nowej linii na końcu
jne blad

mov rsi, [text_start] ; Powrót na absolutny początek by rozpocząć pełne parsowanie
mov r12, [text_start]
add r12, [total_bytes] ; Wyznaczenie definitywnej granicy bezpieczeństwa czytania pliku
mov r13, 0 ; Zresetowanie licznika liczby wierszy reguł

lea rbx, [rel line_table] ; Pobranie wskaźnika zapisu do pierwszego rzędu struktury linii
mov [rbx], rsi
add rbx, 8
inc r13

parsuj_bufor:
cmp rsi, r12 ; Porównanie bieżącej pozycji względem ustalonego limitu całego pliku
jge dalsza_czesc
mov al, [rsi]
cmp al, 10 ; Sprawdzenie czy przetworzono separator linii tekstu (kod ASCII 10)
jne idz_dalej ; Ignorowanie normalnych znaków drukownych

mov byte [rsi], 0 ; Podmiana separatora LF na bajt zerowy
lea rdx, [rsi + 1] ; Kalkulacja adresu początku pierwszego znaku z kolejnego wiersza
cmp rdx, r12 ; Zabezpieczenie przed zachowaniem ostatniego LF jako nowej, pustej linii
jge idz_dalej

cmp r13, 95 ; Weryfikacja czy użytkownik nie zdefiniował zbyt wielu reguł wymian
jge blad
mov [rbx], rdx ; Zachowanie początku kolejnej reguły do bezpiecznego zbioru referencji
add rbx, 8
inc r13 ; Aktualizacja licznika liczby załadowanych definicji wierszy

idz_dalej:
inc rsi ; Przesunięcie na następny badany bajt źródłowy wejścia
jmp parsuj_bufor


; ==============================================================================
; WALIDACJA ZNAKÓW I BUDOWA SŁOWNIKA ZASTĄPIEŃ (RULE DICTIONARY)
; Opis:
; Sprawdza legalność znaków oraz zapisuje reguły (litera -> słowo)
; do tablicy rule_dict, Upewniając się, że litery się nie dublują.
; ==============================================================================
dalsza_czesc:
mov [line_count], r13
mov rsi, [line_table]

waliduj_aksjomat:
movzx rax, byte [rsi]
cmp rax, 0 ; Wykrycie wcześniej wygenerowanego zakończenia wiersza
je koniec_walidacji_aksjomatu
cmp rax, 33 ; Walidacja znaków
jl blad
cmp rax, 126
jg blad
inc rsi
jmp waliduj_aksjomat

koniec_walidacji_aksjomatu:
mov rcx, 1

tworz_tablice_zastepowan:
cmp rcx, [line_count] ; Porównanie bieżącego licznika iteracji z liczbą reguł
jge tworzenie_fraktala
lea rsi, [rel line_table]
mov rsi, [rsi + rcx * 8]

movzx rax, byte [rsi] ; Wczytanie pojedynczej litery będąca znakiem zastępowanym
lea rdx, [rsi + 1] ; Ekstrakcja łańcucha znaków zamieniających klucz

cmp rax, 33 ; Sprawdzenie poprawności znaku
jl blad
cmp rax, 126
jg blad

lea r8, [rel rule_dict] ; Zapisuję poczatek słownika zastapien
mov r9, [r8 + rax * 8] ; Wyciągnięcie ewentualnej dotychczasowej zawartości komórki dla tego klucza
cmp r9, 0
jne blad ; Awaria gdy procesor wykrył kolizję zastapień

mov rdi, rdx
waliduj_slowo_w:
movzx r9, byte [rdi]
cmp r9, 0 ; Sprawdzenie końca słowa ('\0')
je slowo_w_poprawne
cmp r9, 33 ; Dolna granica oczekiwanych znaków ASCII
jl blad
cmp r9, 126 ; Górna granica oczekiwanych znaków ASCII
jg blad
inc rdi
jmp waliduj_slowo_w

slowo_w_poprawne:
mov [r8 + rax * 8], rdx ; Zapisanie adresu reguły w słowniku
inc rcx
jmp tworz_tablice_zastepowan


; ==============================================================================
; ALOKACJA GŁÓWNYCH BUFORÓW (A I B) ORAZ ICH INICJALIZACJA
; Opis:
; Wydziela pamięć (sys_mmap) na oba bufory, pomiędzy którymi będziemy cyklicznie
; przepisywać rozrastający się tekst fraktala.
; ==============================================================================
tworzenie_fraktala:
mov rax, 9 ; sys_mmap - alokacja pierwszego bufora (buf_A)
xor rdi, rdi
mov rsi, 1048576 ; Początkowy rozmiar bufora: 1 MB (oczekuję że to optymalna wartość)
mov [buf_A_cap], rsi
mov rdx, 3 ; PROT_READ | PROT_WRITE
mov r10, 34 ; MAP_PRIVATE | MAP_ANONYMOUS
mov r8, -1
xor r9, r9
syscall

cmp rax, 0
jl blad ; Błąd alokacji
mov [buf_A], rax

mov rax, 9 ; sys_mmap - alokacja drugiego bufora (buf_B)
xor rdi, rdi
mov rsi, 1048576
mov [buf_B_cap], rsi
mov rdx, 3
mov r10, 34
mov r8, -1
xor r9, r9
syscall

cmp rax, 0
jl blad ; Błąd alokacji
mov [buf_B], rax

mov rsi, [line_table]
mov rdi, [buf_A]
xor rcx, rcx ; Wyzerowanie iteratora służacego do kopiowania tekstu poczatkowego do buf_A

kopiowanie_do_bufA:
cmp byte [rsi + rcx], 0 ; Sprawdzenie końca linii
je koniec_kopiowania
mov al, byte [rsi + rcx]
mov byte [rdi + rcx], al ; Kopiowanie znaku do buf_A
inc rcx
jmp kopiowanie_do_bufA

koniec_kopiowania:
mov [buf_A_len], rcx ; Zapisanie początkowej długości słowa
cmp qword [iterations], 0
je koniec_petli ; Skok do wypisania, jeśli n = 0
xor r13, r13


; ==============================================================================
; GŁÓWNA PĘTLA ITERACYJNA ALGORYTMU
; Opis:
; W każdej iteracji algorytm przegląda znaki w 'buf_A'. Jeśli znak posiada
; regułę zastąpienia, jego wartość rozwijana jest w 'buf_B'. W przeciwnym razie
; znak jest przepisywany bez zmian. Po przejściu bufora, pointery 'A' i 'B' są
; zamieniane miejscami.
;
; Użycie rejestrów:
; rsi - wskaźnik czytający z bieżącego bufora źródłowego (buf_A)
; rdi - wskaźnik zapisujący do bieżącego bufora docelowego (buf_B)
; rdx - iterator przeglądający kolejne pozycje znaków w buf_A
; r8 - iterator określający aktualną pozycję dopisywania do buf_B
; r13 - główny licznik iteracji całej operacji podmian (n)
; ==============================================================================
iteracja_zastepowania:
mov rsi, [buf_A] ; Przygotowanie rejestrów
mov rdi, [buf_B]
xor rdx, rdx
xor r8, r8
cmp qword [buf_A_len], 0 ; Jeśli długość buforu A to 0 to kończ
je koniec_pojedynczej_iteracji

iteracja_po_bufA:
movzx rax, byte [rsi + rdx] ; Pobranie znaku z bufora A
lea r9, [rel rule_dict]
mov r9, qword [r9 + rax * 8] ; Szukanie reguły dla znaku w słowniku
cmp r9, 0
je dodaj_znak ; Brak reguły -> przepisz bez zmian
jne dodaj_zastepnik ; Jest reguła -> wstaw zamiennik

nastepny_znak:
inc rdx
cmp rdx, [buf_A_len] ; Sprawdzenie czy osiągnięto koniec buf_A
jae koniec_pojedynczej_iteracji
jmp iteracja_po_bufA

dodaj_znak: ; Dodaje znak
cmp r8, [buf_B_cap]
jge powieksz_buf_B_znak ; Reallokacja w przypadku braku miejsca (przepisuje dane z buforu B do 2 razy większego miejsca w pamięci)
mov byte [rdi + r8], al
inc r8
jmp nastepny_znak

dodaj_zastepnik: ; Dodaje regułę zastępujaca znak
cmp r8, [buf_B_cap]
jge powieksz_buf_B_zastepnik
cmp byte [r9], 0 ; Sprawdzenie końca reguły
je nastepny_znak
mov al, byte [r9]
mov byte [rdi + r8], al ; Skopiowanie kolejnego znaku z reguły
inc r8
inc r9
jmp dodaj_zastepnik


; ==============================================================================
; REALLOKACJE BUFORA 'B' (SYS_MREMAP)
; Opis:
; Odpowiedzialne za dynamiczne zarządzanie i poszerzanie pamięci dla fraktala
; rozbudowującego się. Bezpiecznie przechowują kontekst (stos) przed wołaniem
; jądra.
; ==============================================================================
powieksz_buf_B_znak: ; Osobne zwiększanie gdy znak nie ma reguły zastępowania
push rax ; Zabezpieczenie rejestrów przed wywołaniem systemowym
push rsi
push rdx

mov rax, 25 ; sys_mremap
mov rdi, [buf_B]
mov rsi, [buf_B_cap]
mov rdx, [buf_B_cap]
shl rdx, 1 ; Podwojenie dotychczasowego rozmiaru
mov r10, 1 ; MREMAP_MAYMOVE
syscall

cmp rax, 0
jl blad
shl qword [buf_B_cap], 1 ; Zapisanie nowej pojemności
mov r14, [buf_B_cap]
mov rdi, rax
mov [buf_B], rax

pop rdx ; Odtworzenie stanu rejestrów
pop rsi
pop rax
jmp dodaj_znak

powieksz_buf_B_zastepnik: ; Oraz osobne zwiększanie gdy znak ma regułę zastępowania
push rsi ; Zabezpieczenie rejestrów przed wywołaniem systemowym
push rdx

mov rax, 25 ; sys_mremap
mov rdi, [buf_B]
mov rsi, [buf_B_cap]
mov rdx, [buf_B_cap]
shl rdx, 1
mov r10, 1
syscall

cmp rax, 0
jl blad
shl qword [buf_B_cap], 1 ; Zapisanie nowej pojemności
mov r14, [buf_B_cap]
mov rdi, rax
mov [buf_B], rax

pop rdx ; Odtworzenie stanu rejestrów
pop rsi
jmp dodaj_zastepnik


; ==============================================================================
; FINALIZACJA POJEDYNCZEJ ITERACJI (ZAMIANA BUFORÓW)
; Opis:
; Wskaźniki bufora docelowego i źródłowego są podmieniane tak, by w kolejnym
; cyklu buf_B stał się buf_A.
; ==============================================================================
koniec_pojedynczej_iteracji:
mov [buf_A_len], r8

mov r10, [buf_A] ; Zamiana wskaźników
mov r11, [buf_B]
mov [buf_A], r11
mov [buf_B], r10

mov r10, [buf_A_cap] ; Zamiana pojemności buforów
mov r11, [buf_B_cap]
mov [buf_A_cap], r11
mov [buf_B_cap], r10

inc r13 ; Zwiększenie licznika wykonanych iteracji
cmp r13, [iterations]
je koniec_petli
jmp iteracja_zastepowania


; ==============================================================================
; WYPISANIE WYNIKÓW (SYS_WRITE)
; Opis:
; Iteracyjnie zrzuca zawartość z buf_A na standardowe wyjście, a na sam koniec
; dodaje znak nowej linii ('\n'), nawet jeśli bufor jest pusty.
; ==============================================================================
koniec_petli:
mov r14, [buf_A]
mov r15, [buf_A_len]
cmp r15, 0 ; Gdy wynik pusty to wypisz pusta linię
je wypisz_nowa_linie

wypisz_petla:
mov rax, 1 ; sys_write
mov rdi, 1 ; stdout
mov rsi, r14
mov rdx, r15 ; Wypisanie pozostałych bajtów
syscall

cmp rax, 0
jl blad

add r14, rax ; Przesunięcie wskaźnika po wypisaniu partii
sub r15, rax ; Pomniejszenie liczby bajtów do wypisania
cmp r15, 0
jg wypisz_petla

wypisz_nowa_linie:
mov rax, 1 ; sys_write
mov rdi, 1
lea rsi, [rel newline] ; Wrzucam adres do zadeklarowanego końca linii
mov rdx, 1 ; 1 bajt
syscall
jmp zakoncz_sukces


; ==============================================================================
; SPRZĄTANIE PAMIĘCI (SYS_MUNMAP) I ZAKOŃCZENIE
; Opis:
; Weryfikuje które bufory zostały zaalokowane, zwalnia je, a następnie zwraca
; odpowiedni kod exit.
; ==============================================================================
blad_mmap: ; Używam tego gdy nie ma co zwalniać i mam wyrzucić bład
mov rdi, 1 ; Kod błędu: 1
jmp exit_program

blad: ; Używam tego gdy jest co zwalniać i mam wyrzucić bład
mov rdi, 1
jmp sprzatnij_i_wyjdz

zakoncz_sukces: ; Używam tego gdy jest co zwalniać i nie ma wyrzucać błędu
mov rdi, 0 ; Kod sukcesu: 0
jmp sprzatnij_i_wyjdz

sprzatnij_i_wyjdz:
push rdi ; Zachowanie kodu wyjścia (przed wywołaniami systemowymi)

sprawdz_text:
cmp qword [text_start], 0 ; Jeśli zaalokowałem text_start to zwalniam
je sprawdz_buf_A
mov rax, 11 ; sys_munmap
mov rdi, [text_start]
mov rsi, [capacity]
syscall

sprawdz_buf_A:
cmp qword [buf_A], 0 ; Jeśli zaalokowałem buf_A to zwalniam
je sprawdz_buf_B
mov rax, 11
mov rdi, [buf_A]
mov rsi, [buf_A_cap]
syscall

sprawdz_buf_B:
cmp qword [buf_B], 0 ; Jeśli zaalokowałem buf_B to zwalniam
je wykonaj_exit
mov rax, 11
mov rdi, [buf_B]
mov rsi, [buf_B_cap]
syscall

wykonaj_exit:
pop rdi ; Przywrócenie ustawionego wcześniej kodu wyjścia

exit_program:
mov rax, 60 ; sys_exit
syscall