# Currying i parcijalna primena

## Šta je currying?

**Currying** = transformacija funkcije koja prima N argumenata u lanac funkcija
koje svaka prima po 1 argument.

```
f(a, b, c)  →  f(a)(b)(c)

// Matematički:
f: (A × B × C) → R    →    f: A → (B → (C → R))
```

Naziv dolazi od matematičara Haskell-a Curry-ja (po njemu je imenovan i jezik Haskell).

## Šta je parcijalna primena?

**Parcijalna primena** = fiksiranje nekih argumenata funkcije, dobijanje nove funkcije
koja prima preostale argumente.

```
f(a, b, c)  →  g = f(a, ___, ___)  →  g(b, c)
```

Razlika:
- **Currying**: uvek 1 argument po koraku, uvek transformacija cele funkcije
- **Parcijalna primena**: fiksiramo koliko god argumenata želimo

## Zašto je ovo korisno?

### 1. Konfigurabilne funkcije

```java
// Bez currying-a: ponavljamo konfiguraciju
filter(lista, s -> s.startsWith("A"));
filter(lista, s -> s.startsWith("B"));

// Sa currying-om: jednom konfigurišemo, koristimo više puta
Function<String, Predicate<String>> startsWith = prefix -> s -> s.startsWith(prefix);

filter(lista, startsWith.apply("A"));
filter(lista, startsWith.apply("B"));
```

### 2. Pipeline konfiguracija

```java
// Parcijalno primenjene transformacije
Function<Double, UnaryOperator<Double>> multiply = factor -> x -> x * factor;
Function<Double, UnaryOperator<Double>> add      = offset -> x -> x + offset;

// Kreiramo specifične transformacije
UnaryOperator<Double> celsiusToFahrenheit = multiply.apply(9.0/5).andThen(add.apply(32.0));
```

### 3. Factory funkcije

```java
// Curried factory: konfiguriši tip → konfiguriši ime → kreiraj objekat
Function<String, Function<Integer, Student>> studentFactory =
    smer -> poeni -> new Student("?", poeni, smer);

// Specijalizovane fabrike
Function<Integer, Student> itStudent   = studentFactory.apply("IT");
Function<Integer, Student> mathStudent = studentFactory.apply("Matematika");
```

## Currying u Javi

Java nema native podršku za currying (za razliku od Haskell-a gde su SVE funkcije curried).
Ali možemo ga implementirati koristeći `Function<A, Function<B, R>>`:

```java
// Regularna BiFunction
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

// Curried verzija
Function<Integer, Function<Integer, Integer>> addCurried = a -> b -> a + b;

// Parcijalna primena
Function<Integer, Integer> add5 = addCurried.apply(5);
add5.apply(3);  // 8
add5.apply(10); // 15
```

## Primeri u kodu

- `CurryingExamples.java` — currying, parcijalna primena, praktični primeri

---
