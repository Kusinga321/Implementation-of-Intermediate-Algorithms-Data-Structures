.PHONY: all clean

all: librstack.so

# Tworzenie biblioteki dzielonej z pliku obiektowego
librstack.so: rstack.o
	gcc -shared -o librstack.so rstack.o

# Kompilacja kodu w C do pliku obiektowego 
rstack.o: rstack.c
	gcc -Wall -Wextra -fPIC -c rstack.c -o rstack.o

# Sprzątanie po kompilacji
clean:
	rm -f rstack.o librstack.so