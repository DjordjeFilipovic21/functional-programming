# Paradigme Programiranja - Nedelja 2
## Funkcionalni interfejsi, forEach, ponovno korišćenje lambda izraza, collect i join

---

## Sadržaj

1. [Predicate, Consumer, Function, Supplier — detaljnije](#1-predicate-consumer-function-supplier--detaljnije)
2. [Povezanost funkcionalnih interfejsa sa Stream operacijama](#2-povezanost-funkcionalnih-interfejsa-sa-stream-operacijama)
3. [forEach — detaljno objašnjenje](#3-foreach--detaljno-objašnjenje)
4. [Ponovno korišćenje lambda izraza](#4-ponovno-korišćenje-lambda-izraza)
5. [Leksički opseg i closure-i](#5-leksički-opseg-i-closure-i)
6. [collect i Collectors klasa](#6-collect-i-collectors-klasa)
7. [Spajanje elemenata — join](#7-spajanje-elemenata--join)
8. [reduce — svođenje kolekcije na jednu vrednost](#8-reduce--svođenje-kolekcije-na-jednu-vrednost)
9. [Dodatne operacije: skip, limit, dropWhile, takeWhile](#9-dodatne-operacije-skip-limit-dropwhile-takewhile)
10. [Zadaci za vežbu](#10-zadaci-za-vežbu)
11. [Primeri koda](#11-primeri-koda)

---

## 1. Predicate, Consumer, Function, Supplier — detaljnije

Ovi interfejsi čine osnovu funkcionalne Jave. Svaki od njih ima jednu apstraktnu metodu (SAM) i predstavlja **tip** lambda izraza.

### Predicate\<T\> — "Da li važi?"

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}
```

- **Ulaz:** jedan argument tipa `T`
- **Izlaz:** `boolean`
- **Koristi se u:** `filter()`, `removeIf()`, `anyMatch()`, `allMatch()`, `noneMatch()`
- **Kompozicija:** `and()`, `or()`, `negate()`

```java
Predicate<Integer> jeParno = n -> n % 2 == 0;
Predicate<Integer> jeVeceOd10 = n -> n > 10;

// Kompozicija — parno I veće od 10
Predicate<Integer> parnoVeceOd10 = jeParno.and(jeVeceOd10);
parnoVeceOd10.test(12); // true
parnoVeceOd10.test(8);  // false

// Negacija
Predicate<Integer> jeNeparno = jeParno.negate();
```

### Consumer\<T\> — "Uradi nešto sa ovim"

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
```

- **Ulaz:** jedan argument tipa `T`
- **Izlaz:** ništa (`void`)
- **Koristi se u:** `forEach()`
- **Kompozicija:** `andThen()` — ulančavanje

```java
Consumer<String> stampaj = s -> System.out.println(s);
Consumer<String> stampajVeliko = s -> System.out.println(s.toUpperCase());

// andThen — izvršava oba redom
Consumer<String> oba = stampaj.andThen(stampajVeliko);
oba.accept("java"); // štampa "java" pa "JAVA"
```

### Function\<T, R\> — "Transformiši ovo u nešto drugo"

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```

- **Ulaz:** argument tipa `T`
- **Izlaz:** rezultat tipa `R`
- **Koristi se u:** `map()`, `flatMap()`
- **Kompozicija:** `andThen()`, `compose()`

```java
Function<String, Integer> duzina = String::length;
Function<Integer, Integer> kvadrat = n -> n * n;

// andThen — prvo duzina, pa kvadrat
Function<String, Integer> duzinaNaKvadrat = duzina.andThen(kvadrat);
duzinaNaKvadrat.apply("Java"); // 16

// compose — obrnuti redosled: prvo kvadrat pa...
// (ne radi jer tipovi ne odgovaraju u ovom primeru)
// Ali generalno: f.compose(g) = f(g(x))
```

### Supplier\<T\> — "Proizvedi nešto"

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

- **Ulaz:** ništa
- **Izlaz:** vrednost tipa `T`
- **Koristi se u:** `orElseGet()`, `Stream.generate()`, lenjo kreiranje objekata

```java
Supplier<Double> randomBroj = Math::random;
Supplier<List<String>> praznaLista = ArrayList::new;

// Lenjo izračunavanje — poziva se tek kad zatreba
Optional.empty().orElseGet(() -> "podrazumevana vrednost");
```

### Specijalizovani interfejsi

| Interfejs | Opis |
|-----------|------|
| `BiFunction<T, U, R>` | Dva ulaza → jedan izlaz |
| `BiConsumer<T, U>` | Dva ulaza → void |
| `BiPredicate<T, U>` | Dva ulaza → boolean |
| `UnaryOperator<T>` | Function gde je T == R |
| `BinaryOperator<T>` | BiFunction gde su T, U, R isti tip |

---

## 2. Povezanost funkcionalnih interfejsa sa Stream operacijama

Ovo je ključna veza — **svaka** Stream operacija prima **tačno određen** funkcionalni interfejs kao argument:

| Stream metoda | Prima | Funkcionalni interfejs | Potpis |
|---------------|-------|----------------------|--------|
| `filter()` | Predicate\<T\> | `T → boolean` | `boolean test(T t)` |
| `map()` | Function\<T, R\> | `T → R` | `R apply(T t)` |
| `flatMap()` | Function\<T, Stream\<R\>\> | `T → Stream<R>` | `Stream<R> apply(T t)` |
| `forEach()` | Consumer\<T\> | `T → void` | `void accept(T t)` |
| `reduce()` | BinaryOperator\<T\> | `(T, T) → T` | `T apply(T t1, T t2)` |
| `sorted()` | Comparator\<T\> | `(T, T) → int` | `int compare(T a, T b)` |
| `anyMatch()` | Predicate\<T\> | `T → boolean` | `boolean test(T t)` |
| `allMatch()` | Predicate\<T\> | `T → boolean` | `boolean test(T t)` |
| `noneMatch()` | Predicate\<T\> | `T → boolean` | `boolean test(T t)` |

Kada pišemo `list.stream().filter(x -> x > 5)`, lambda izraz `x -> x > 5` je zapravo **implementacija** metode `test` iz `Predicate<Integer>` interfejsa.

```java
// Ovo:
list.stream().filter(x -> x > 5);

// Je skraćeni oblik ovoga:
Predicate<Integer> veceOd5 = new Predicate<Integer>() {
    @Override
    public boolean test(Integer x) {
        return x > 5;
    }
};
list.stream().filter(veceOd5);
```

---

## 3. forEach — detaljno objašnjenje

### Šta je forEach?

`forEach` je terminalna operacija koja prima **Consumer\<T\>** i izvršava ga za svaki element.

```java
// Definicija u Iterable interfejsu:
default void forEach(Consumer<? super T> action) { ... }
```

### forEach na kolekciji vs. na Stream-u

```java
List<String> imena = List.of("Ana", "Bojan", "Čedomir");

// 1. Direktno na listi (Iterable.forEach) — interna iteracija
imena.forEach(ime -> System.out.println(ime));

// 2. Na stream-u (Stream.forEach) — nakon pipeline-a
imena.stream()
     .filter(ime -> ime.length() > 3)
     .forEach(ime -> System.out.println(ime));
```

### Evolucija od petlje do forEach

```java
// 1. Klasična for petlja — eksterna iteracija
for (int i = 0; i < imena.size(); i++) {
    System.out.println(imena.get(i));
}

// 2. Enhanced for — i dalje eksterna
for (String ime : imena) {
    System.out.println(ime);
}

// 3. forEach sa anonimnom klasom — interna, ali ružna
imena.forEach(new Consumer<String>() {
    public void accept(String ime) {
        System.out.println(ime);
    }
});

// 4. forEach sa lambda — interna, elegantna
imena.forEach(ime -> System.out.println(ime));

// 5. forEach sa method reference — najkraća
imena.forEach(System.out::println);
```

### Ograničenja forEach

- **Nema break/continue** — ne možemo prekinuti forEach na pola
- **Ne sme da menja kolekciju** tokom iteracije
- Za slučajeve kad treba `break`, koristiti `findFirst()`, `takeWhile()`, `anyMatch()`

---

## 4. Ponovno korišćenje lambda izraza

Lambda izrazi su kratki, ali lako se dupliraju. Princip **DRY** (Don't Repeat Yourself) važi i ovde.

### Problem — dupliranje

```java
// LOŠE: isti lambda izraz ponovljen 3 puta!
long countFriends = friends.stream()
    .filter(name -> name.startsWith("N")).count();
long countEditors = editors.stream()
    .filter(name -> name.startsWith("N")).count();
long countComrades = comrades.stream()
    .filter(name -> name.startsWith("N")).count();
```

### Rešenje — skladištenje u promenljivu

```java
// DOBRO: lambda izraz sačuvan u Predicate promenljivoj
final Predicate<String> startsWithN = name -> name.startsWith("N");

long countFriends  = friends.stream().filter(startsWithN).count();
long countEditors  = editors.stream().filter(startsWithN).count();
long countComrades = comrades.stream().filter(startsWithN).count();
```

Lambda izraz je objekat — može se dodeliti promenljivoj, proslediti kao argument, i vratiti kao rezultat.

---

## 5. Leksički opseg i closure-i

### Problem — nov oblik dupliranja

```java
// Opet dupliranje — razlikuje se samo slovo!
final Predicate<String> startsWithN = name -> name.startsWith("N");
final Predicate<String> startsWithB = name -> name.startsWith("B");
```

### Rešenje 1 — statička metoda (Higher-Order Function)

```java
public static Predicate<String> checkIfStartsWith(String letter) {
    return name -> name.startsWith(letter);
}

// Upotreba:
friends.stream().filter(checkIfStartsWith("N")).count();
friends.stream().filter(checkIfStartsWith("B")).count();
```

Funkcija `checkIfStartsWith` **vraća** drugu funkciju (Predicate). To je **funkcija višeg reda** (higher-order function).

### Rešenje 2 — Function umesto statičke metode

```java
final Function<String, Predicate<String>> startsWithLetter =
    letter -> name -> name.startsWith(letter);

// Upotreba:
friends.stream().filter(startsWithLetter.apply("N")).count();
friends.stream().filter(startsWithLetter.apply("B")).count();
```

### Šta je closure?

**Closure** je lambda izraz koji "zatvara" (closes over) promenljive iz svog okružujućeg opsega:

```java
String prefix = "Zdravo, ";   // promenljiva iz spoljnog opsega
Consumer<String> pozdravi = name -> System.out.println(prefix + name);
//                                                      ^^^^^^
//                           'prefix' nije parametar lambde,
//                            ali se koristi — to je closure!
```

**Pravilo:** Promenljive iz spoljnog opsega moraju biti `final` ili **efektivno final** (ne smeju se menjati nakon inicijalizacije).

---

## 6. collect i Collectors klasa

`collect()` je terminalna operacija za **skupljanje** elemenata Stream-a u konačnu strukturu.

### Osnovni kolektori

```java
import java.util.stream.Collectors;

List<String> imena = List.of("Ana", "Bojan", "Čedomir", "Ana");

// U listu
List<String> lista = imena.stream()
    .filter(i -> i.length() > 3)
    .collect(Collectors.toList());

// U nepromenljivu listu (Java 16+)
List<String> nepromenljiva = imena.stream()
    .filter(i -> i.length() > 3)
    .toList();

// U Set (bez duplikata)
Set<String> set = imena.stream()
    .collect(Collectors.toSet());

// U mapu
Map<String, Integer> mapa = imena.stream()
    .distinct()
    .collect(Collectors.toMap(
        ime -> ime,            // key
        ime -> ime.length()    // value
    ));
```

### Grupisanje — groupingBy

```java
// Grupisanje po dužini imena
Map<Integer, List<String>> poDuzini = imena.stream()
    .collect(Collectors.groupingBy(String::length));
// {3=[Ana, Ana], 5=[Bojan], 7=[Čedomir]}
```

### Particionisanje — partitioningBy

```java
// Podela na dva dela: true i false
Map<Boolean, List<String>> podela = imena.stream()
    .collect(Collectors.partitioningBy(i -> i.length() > 3));
// {false=[Ana, Ana], true=[Bojan, Čedomir]}
```

---

## 7. Spajanje elemenata — join

### String.join — najjednostavniji način

```java
List<String> gradovi = List.of("Beograd", "Niš", "Novi Sad");

String rezultat = String.join(", ", gradovi);
// "Beograd, Niš, Novi Sad"
```

### Collectors.joining — sa transformacijom

```java
// joining sa separatorom
String spojena = gradovi.stream()
    .collect(Collectors.joining(", "));
// "Beograd, Niš, Novi Sad"

// joining sa separatorom, prefiksom i sufiksom
String formatirana = gradovi.stream()
    .map(String::toUpperCase)
    .collect(Collectors.joining(", ", "[", "]"));
// "[BEOGRAD, NIŠ, NOVI SAD]"
```

### Zašto ne koristiti ručno spajanje?

```java
// LOŠE — zarez na kraju!
for (String grad : gradovi) {
    System.out.print(grad + ", ");
}
// Beograd, Niš, Novi Sad,  ← višak zareza

// DOBRO
System.out.println(String.join(", ", gradovi));
// Beograd, Niš, Novi Sad   ← ispravno
```

---

## 8. reduce — svođenje kolekcije na jednu vrednost

`reduce()` kombinuje elemente Stream-a u jednu vrednost koristeći **BinaryOperator**.

```java
// Suma brojeva
List<Integer> brojevi = List.of(1, 2, 3, 4, 5);
Optional<Integer> suma = brojevi.stream()
    .reduce((a, b) -> a + b);
// Optional[15]

// Sa početnom vrednošću (identity)
int suma2 = brojevi.stream()
    .reduce(0, (a, b) -> a + b);
// 15 — vraća int, ne Optional

// Najduže ime
List<String> imena = List.of("Ana", "Bojan", "Čedomir");
Optional<String> najduze = imena.stream()
    .reduce((a, b) -> a.length() >= b.length() ? a : b);
// Optional[Čedomir]
```

### reduce vs. specijalizovane metode

```java
// Umesto reduce za sumu:
int suma = brojevi.stream().mapToInt(Integer::intValue).sum();

// Umesto reduce za max:
Optional<String> najduze = imena.stream()
    .max(Comparator.comparingInt(String::length));
```

---

## 9. Dodatne operacije: skip, limit, dropWhile, takeWhile

```java
List<String> imena = List.of("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");

// skip(n) — preskoči prvih n elemenata
imena.stream().skip(4).forEach(System.out::println);
// Sara, Scott

// limit(n) — uzmi samo prvih n elemenata (ekvivalent break-a)
imena.stream().limit(3).forEach(System.out::println);
// Brian, Nate, Neal

// dropWhile — preskoči dok je uslov tačan
imena.stream()
    .dropWhile(name -> name.length() > 4)
    .forEach(System.out::println);
// Nate, Neal, Raju, Sara, Scott (Brian preskočen)

// takeWhile — uzimaj dok je uslov tačan (ekvivalent break-a)
imena.stream()
    .takeWhile(name -> name.length() > 4)
    .forEach(System.out::println);
// Brian (samo on zadovoljava uslov pre prekida)
```

> **Napomena:** `dropWhile` i `takeWhile` rade kao vrata — jednom kad se otvore/zatvore, ostaju u tom stanju. Za filtriranje *svakog* elementa koristiti `filter`.

---

## 10. Zadaci za vežbu

Zadaci su u fajlu [`PracticeTasksForStudents.java`](PracticeTasksForStudents.java). Sadrže TODO komentare gde studenti treba da implementiraju rešenja.

---

## 11. Primeri koda

- [`FunctionalInterfacesDeepDive.java`](FunctionalInterfacesDeepDive.java) — Predicate, Consumer, Function, Supplier u akciji
- [`ForEachExplained.java`](ForEachExplained.java) — forEach detaljno, tip argumenta, evolucija od petlje
- [`LambdaReuse.java`](LambdaReuse.java) — ponovno korišćenje lambda izraza, closure-i, leksički opseg
- [`CollectAndJoin.java`](CollectAndJoin.java) — collect, Collectors, join, reduce
- [`PracticeTasksForStudents.java`](PracticeTasksForStudents.java) — zadaci za vežbu na času

---
