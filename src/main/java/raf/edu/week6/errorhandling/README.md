# Obrada grešaka u FP

## Problem: checked exceptions u lambdama

Java lambda izrazi **ne mogu** baciti checked exception osim ako funkcionalni interfejs
to eksplicitno dozvoljava. `Function`, `Predicate`, `Consumer` — nijedan ne deklariše `throws`.

```java
// NE KOMPAJLIRA: IOException is checked
lista.stream()
    .map(path -> Files.readString(path))  // ← kompajler greška!
    .collect(Collectors.toList());
```

Ovo je fundamentalni sukob između Javinog sistema checked exception-a i FP pristupa.

## Rešenje 1: Wrapper pattern — omotaj exception

Najprostije rešenje: funkcija koja hvata checked exception i baca unchecked.

```java
@FunctionalInterface
interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
}

static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> fn) {
    return t -> {
        try { return fn.apply(t); }
        catch (Exception e) { throw new RuntimeException(e); }
    };
}

// Sada radi:
lista.stream()
    .map(unchecked(path -> Files.readString(path)))
    .collect(Collectors.toList());
```

## Rešenje 2: Either pattern — eksplicitna greška u tipu

Umesto da bacamo exception, vraćamo tip koji SADRŽI ili uspeh ili grešku:

```
Either<Error, Value>
  ├── Left(error)    — neuspeh
  └── Right(value)   — uspeh
```

Prednosti:
- **Kompajler** nas tera da obradimo grešku — ne može se "zaboraviti"
- **Nema exception-a** — tok programa je uvek predvidiv
- **Composable** — map/flatMap na Either rade samo na Right strani

## Rešenje 3: Optional za "može biti prazno"

Kada nam nije bitan razlog greške (samo da li postoji vrednost):

```java
stream.map(s -> parseIntSafe(s))      // String → Optional<Integer>
      .flatMap(Optional::stream)       // odbaci prazne
      .collect(Collectors.toList());
```

## Kada koristiti šta?

| Situacija | Pristup |
|-----------|---------|
| Greška je neočekivana (bug) | Baci RuntimeException |
| Checked exception u lambdi | Wrapper (`unchecked()`) |
| Operacija može ali ne mora uspeti | `Optional` |
| Treba znati ZAŠTO je neuspeh | `Either<Error, T>` |
| Više nezavisnih validacija | `Validation` (nedelja 5) |

## Primeri u kodu

- `ErrorHandling.java` — wrapper pattern, Either tip, praktični primeri

---
