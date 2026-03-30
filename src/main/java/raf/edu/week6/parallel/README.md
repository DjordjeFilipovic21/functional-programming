# Parallel Streams — Paralelizacija u FP

## Ideja

Jedan od najvećih aduta funkcionalnog pristupa: **isti kod, paralelno izvršavanje**.
Pošto su stream operacije bez sporednih efekata (čiste funkcije), JVM može bezbedno
podeliti posao na više thread-ova.

```java
// Sekvencijalno
lista.stream().filter(...).map(...).collect(...)

// Paralelno — ISTA logika, samo druga fabrika
lista.parallelStream().filter(...).map(...).collect(...)
```

## Kako radi?

Parallel stream koristi **Fork/Join** framework:

```
        [1, 2, 3, 4, 5, 6, 7, 8]
                    │
              ┌─────┴─────┐
         [1,2,3,4]    [5,6,7,8]        ← FORK (podeli)
           │              │
       ┌───┴───┐      ┌───┴───┐
     [1,2]   [3,4]  [5,6]   [7,8]     ← FORK
       │       │      │       │
      map     map    map     map       ← obradi svaki deo
       │       │      │       │
     [r1,r2] [r3,r4] [r5,r6] [r7,r8]
       └───┬───┘      └───┬───┘
         merge           merge         ← JOIN (spoji)
              └─────┬─────┘
                  merge                ← JOIN
                    │
            [r1,r2,...,r8]
```

## Kada koristiti?

| Koristiti | Ne koristiti |
|-----------|-------------|
| Velika kolekcija (>10.000 elemenata) | Mala kolekcija (<1.000) |
| CPU-intenzivne operacije (parsiranje, računanje) | I/O operacije (HTTP, fajl, DB) |
| Nezavisni elementi (nema deljenog stanja) | Deljeno mutable stanje |
| `ArrayList`, nizovi (dobra lokalnost) | `LinkedList` (loša podela) |
| Operacije gde redosled nije bitan | Redosled mora biti očuvan |

## Opasnosti

### 1. Shared mutable state — klasičan bug

```java
// OPASNO: race condition!
List<Integer> rezultati = new ArrayList<>();
stream.parallel().forEach(rezultati::add);  // ← CRASH ili gubimo elemente

// ISPRAVNO: koristiti collect
List<Integer> rezultati = stream.parallel().collect(Collectors.toList());
```

### 2. Overhead za male kolekcije

Kreiranje thread-ova i sinhronizacija koštaju. Za male kolekcije,
sekvencijalni stream je brži.

### 3. Redosled

`forEach` na paralelnom streamu NE garantuje redosled.
Koristiti `forEachOrdered` ako je redosled bitan (ali gubi se paralelizam).

## reduce i combiner

Treći argument `reduce` metode — **combiner** — postoji upravo za paralelne streamove.
Kada se stream podeli, svaki deo ima svoj parcijalni rezultat.
Combiner ih spaja:

```java
// Sekvencijalno: combiner se nikad ne poziva
int suma = stream.reduce(0, Integer::sum, Integer::sum);
//                        ^identity  ^accumulator  ^combiner

// Paralelno: accumulator radi unutar svakog dela,
//            combiner spaja parcijalne rezultate
```

## Primeri u kodu

- `ParallelStreams.java` — benchmark, pitfalls, reduce sa combiner-om

---
