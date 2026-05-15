Stock Market Simulator
Opis projektu

Projekt przedstawia prosty symulator giełdowy napisany w języku Java. Program umożliwia przeglądanie danych giełdowych różnych spółek, kupowanie i sprzedawanie akcji oraz śledzenie wartości portfela inwestora. Aplikacja działa w konsoli i została stworzona w celu pokazania wykorzystania elementów programowania funkcyjnego w języku Java.

Funkcjonalności
wyświetlanie dostępnych spółek
przeglądanie historycznych cen akcji
filtrowanie danych po nazwie spółki
filtrowanie danych po zakresie dat
kupowanie akcji
sprzedawanie akcji
wyświetlanie historii transakcji
obliczanie wartości portfela
obliczanie zysku lub straty inwestora

Projekt zawiera następujące elementy:

niemutowalność danych
rekordy (record)
sealed interface
Optional
Stream API
funkcje wyższego rzędu (Predicate, Function)
kompozycję funkcji (andThen)
operacje map, filter, reduce, toList
Technologie
Java
Stream API
Optional
Record
Sealed Interface

Uruchomienie projektu:
W terminalu nalezy wpisac komendę:

javac Main.java

Potem:

java Main

Budowa projektu:
Wszystko znajduje sie w jednym pliku Main.java
W pliku znajdują się rekordy przechowujące dane giełdowe, obsługa portfela inwestora oraz metody odpowiedzialne za filtrowanie danych i wykonywanie transakcji.
