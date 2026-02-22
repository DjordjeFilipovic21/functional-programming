# Paradigme Programiranja - Nedelja 1
## Uvod u funkcionalno programiranje u Javi

---

## Sadržaj

1. [Pregled paradigmi programiranja](#1-pregled-paradigmi-programiranja)
2. [Imperativni vs. Funkcionalni stil](#2-imperativni-vs-funkcionalni-stil)
3. [Imutabilnost](#3-imutabilnost)
4. [Lambda izrazi u Javi](#4-lambda-izrazi-u-javi)
5. [Funkcionalni interfejsi](#5-funkcionalni-interfejsi)
6. [Method Reference](#6-method-reference)
7. [Stream API - uvod](#7-stream-api--uvod)
8. [Optional - uvod](#8-optional--uvod)
9. [Primeri koda](#9-primeri-koda)

---

## 1. Pregled paradigmi programiranja

Postoji nekoliko osnovnih paradigmi (stilova) programiranja. Svaka nudi drugačiji način razmišljanja o problemima i njihovim rešenjima:

| Paradigma | Ključna ideja                                            | Primer jezika           |
|-----------|----------------------------------------------------------|-------------------------|
| **Imperativna** | Govorimo računaru *kako* da uradi nešto, korak po korak  | C, starija Java         |
| **Objektno-orijentisana (OOP)** | Modelujemo svet kroz objekte sa state-om i ponašanjem    | Java, C++, Python       |
| **Funkcionalna** | Opisujemo *šta* želimo, kroz kompoziciju čistih funkcija | Haskell, Clojure, Scala |
| **Logička** | Definišemo pravila i prepuštamo sistemu da nađe rešenje  | Prolog                  |

Java podržava i imperativni i funkcionalni stil - možemo ih kombinovati u okviru iste aplikacije.

### Matematički modeli izračunavanja

- **Tjuringova mašina** - osnova imperativnog programiranja. Stanje + instrukcije koje ga menjaju.
- **Lambda račun (λ-calculus)** - osnova funkcionalnog programiranja. Sve je funkcija; izračunavanje = primena funkcija na argumente.

---

## 2. Imperativni vs. Funkcionalni stil

### Imperativni stil
- Eksplicitne petlje (`for`, `while`)
- Mutabilne(promenljive) varijable
- Govorimo *kako*
- Primer: ručno iteriramo, menjamo flag, break-ujemo petlju

```java
// Imperativno: da li lista gradova sadrži "Beograd"?
boolean found = false;
for (String city : cities) {
    if (city.equals("Beograd")) {
        found = true;
        break;
    }
}
System.out.println("Pronađen: " + found);
```

### Funkcionalni / Deklarativni stil
- Koristimo high-level operacije (`filter`, `map`, `reduce`)
- Bez eksplicitne mutacije
- Govorimo *šta* želimo
- Kod prati logiku problema, bliži je business logic-u

```java
// Deklarativno: isto pitanje, bez ijedne mutabilne promenljive
boolean found = cities.stream()
                      .anyMatch(city -> city.equals("Beograd"));
System.out.println("Pronađen: " + found);
```

### Prednosti funkcionalnog stila

- **Manje grešaka** - bez mutabilnih promenljivih koje se mogu pogrešno promeniti / ne promeniti
- **Lakše za čitanje** - kod opisuje *šta* se radi, ne *kako*
- **Lakša paralelizacija** - bez deljenog stanja nema problem sa thread-safety
- **Lakše za testiranje** - čiste funkcije su determinističke i bez sporednih efekata
- **Kompozabilnost** - lako kombinujemo operacije 

---

## 3. Imutabilnost

**Imutabilnost** znači da se vrednost ne može promeniti nakon inicijalizacije. Ovo je jedna od glavnih stavki funkcionalnog programiranja.

```java
// Mutabilno - loše u FP
int sum = 0;
for (int x : numbers) {
    sum += x; // menjamo sum
}

// Imutabilno - bolje
int sum = numbers.stream().mapToInt(Integer::intValue).sum();
```

U Javi, za imutabilnost koristimo:
- `final` ključnu reč za promenljive
- Imutabilne kolekcije (`List.of(...)`, `Collections.unmodifiableList(...)`)
- Rad sa Stream API-jem koji ne menja originalnu kolekciju

---

## 4. Lambda izrazi u Javi

**Lambda izraz** je anonimna funkcija - funkcija bez imena, koja se može proslediti kao argument ili dodeliti promenljivoj.

### Sintaksa

```
(parametri) -> telo
```

| Oblik | Primer |
|-------|--------|
| Jedan parametar, jedan izraz | `x -> x * 2` |
| Više parametara | `(x, y) -> x + y` |
| Bez parametara | `() -> System.out.println("Hello")` |
| Blok naredbi | `(x) -> { int r = x * 2; return r; }` |

### Primeri

```java
// Lambda kao Comparator
Comparator<String> byLength = (a, b) -> a.length() - b.length();

// Lambda kao Runnable
Runnable r = () -> System.out.println("Radi u posebnoj niti!");

// Lambda koja filtrira
Predicate<Integer> isEven = n -> n % 2 == 0;
```

---

## 5. Funkcionalni interfejsi

Lambda izrazi implementiraju **funkcionalne interfejse** - interfejse sa tačno jednom apstraktnom metodom (SAM - *Single Abstract Method*). Anotiraju se sa `@FunctionalInterface`.

### Ugrađeni funkcionalni interfejsi u JDK

| Interfejs | Metoda | Opis |
|-----------|--------|------|
| `Predicate<T>` | `boolean test(T t)` | Provera uslova, vraća true/false |
| `Function<T, R>` | `R apply(T t)` | Transformacija: T → R |
| `Consumer<T>` | `void accept(T t)` | Konzumira vrednost, nema povratne vrednosti |
| `Supplier<T>` | `T get()` | Proizvodi vrednost, bez ulaza |

```java
Predicate<String> isLong   = s -> s.length() > 5;
Function<String, Integer> length = String::length;
Consumer<String> print     = System.out::println;
Supplier<String> greeting  = () -> "hello world!";
```

Ovi interfejsi su **tipovi** lambda izraza koji se koriste kao parametri metoda poput `filter`, `map`, `forEach` itd.

---

## 6. Method Reference

**Method reference** je skraćena sintaksa za lambda izraz koji samo poziva neku već postojeću metodu.

### Oblici

| Tip | Sintaksa | Lambda ekvivalent |
|-----|----------|-------------------|
| Statička metoda | `Klasa::statickaMetoda` | `x -> Klasa.statickaMetoda(x)` |
| Metoda instance | `Klasa::metodaInstance` | `x -> x.metodaInstance()` |
| Metoda konkretnog objekta | `objekat::metoda` | `x -> objekat.metoda(x)` |
| Konstruktor | `Klasa::new` | `x -> new Klasa(x)` |

```java
// Lambda
list.stream().map(s -> s.toUpperCase()).forEach(s -> System.out.println(s));

// Method reference - ekvivalentno, kraće
list.stream().map(String::toUpperCase).forEach(System.out::println);
```

> **Kada koristiti?** Kada lambda izraz samo prosleđuje parametre metodi bez ikakve transformacije - koristite method reference radi čitljivosti.

---

## 7. Stream API - uvod

**Stream** je sekvenca elemenata nad kojom možemo primenjivati operacije funkcionalne prirode. Stream **ne menja** originalnu kolekciju - sve operacije vraćaju novi Stream ili finalnu vrednost.

### Kreiranje Streama

```java
List<String> names = List.of("Ana", "Bojan", "Čedomir");
Stream<String> stream = names.stream();
```

### Tipovi operacija

- **Intermediate (posredne)** - vraćaju novi Stream, lenji su (`filter`, `map`, `sorted`, `distinct`, ...)
- **Terminal (završne)** - pokreću izračunavanje, vraćaju rezultat (`forEach`, `collect`, `count`, `sum`, `reduce`, `findFirst`, ...)

### Osnovna pipeline arhitektura

```java
kolekcija.stream()
         .posrednaOperacija1(...)
         .posrednaOperacija2(...)
         .terminalnaOperacija(...);
```

### Ključne metode

```java
// filter - zadržava elemente koji zadovoljavaju uslov
stream.filter(x -> x > 10)

// map - transformiše svaki element
stream.map(s -> s.toUpperCase())

// forEach - prolazi kroz elemente (terminal)
stream.forEach(System.out::println)

// collect - sakuplja u kolekciju (terminal)
stream.collect(Collectors.toList())

// count, sum, min, max (terminal)
stream.count()
```

---

## 8. Optional - uvod

**Optional<T>** je kontejner koji može, ali ne mora, sadržati vrednost. Koristi se kao alternativa `null` vrednostima.

```java
Optional<String> opt = Optional.of("vrednost");
Optional<String> empty = Optional.empty();

// Provera i ekstrakcija
opt.isPresent();              // true
opt.get();                    // "vrednost"
opt.orElse("podrazumevano");  // vraća vrednost ili default
opt.ifPresent(System.out::println); // radi nešto samo ako postoji vrednost
```

> Optional sprečava `NullPointerException` i čini nameru koda eksplicitnijom.

---

## 9. Primeri koda

- [`ImperativeVsFunctional.java`](src/ImperativeVsFunctional.java) - poređenje imperativnog i funkcionalnog pristupa
- [`LambdaBasics.java`](src/LambdaBasics.java) - osnove lambda izraza i funkcionalnih interfejsa
- [`MethodReferenceExamples.java`](src/MethodReferenceExamples.java) - primeri method reference sintakse
- [`StreamBasics.java`](src/StreamBasics.java) - osnove Stream API-ja sa filter, map, forEach, collect
- [`OptionalExamples.java`](src/OptionalExamples.java) - rad sa Optional klasom

---