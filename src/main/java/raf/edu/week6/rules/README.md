# Rule Engine — Composable poslovni uslovi

## Problem

Poslovni sistemi su puni uslova: ko može da pristupi čemu, koliki je popust,
koji nivo rizika ima transakcija, koji email template se šalje.

Imperativni pristup: ugnežđeni if/else blokovi koji rastu eksponencijalno:

```java
double popust(Kupac k, Narudzbina n) {
    if (k.jeVIP() && n.vrednost() > 10000) return 0.20;
    else if (k.jeVIP()) return 0.10;
    else if (n.vrednost() > 10000) return 0.05;
    else if (k.godine() > 65) return 0.08;
    else return 0;
}
// Dodavanje novog pravila = menjanje cele metode
```

## FP rešenje: Specification pattern

Svaki uslov je `Predicate<T>`, svako pravilo je par `(uslov, akcija)`.
Pravila se komponuju sa `and`, `or`, `negate`.

```java
record Rule<T, R>(Predicate<T> uslov, Function<T, R> akcija, String opis) {}

// Pravila su PODACI, ne kod:
List<Rule<Kupac, Double>> pravila = List.of(
    rule(vip.and(velikiIznos), k -> 0.20, "VIP + velika narudžbina"),
    rule(vip,                  k -> 0.10, "VIP popust"),
    rule(velikiIznos,          k -> 0.05, "Popust za veliku narudžbinu"),
    rule(penzioner,            k -> 0.08, "Penzionerski popust")
);

// First-match:
double popust = pravila.stream()
    .filter(r -> r.uslov().test(kupac))
    .findFirst()
    .map(r -> r.akcija().apply(kupac))
    .orElse(0.0);
```

Prednosti:
- **Pravila su podaci** — mogu se učitati iz baze, konfiga, A/B testa
- **Composable** — `and`, `or`, `negate` za složene uslove bez if/else
- **Proširivo** — novo pravilo = novi element liste, ne menjamo stari kod
- **Testabilno** — svako pravilo se testira nezavisno
- **Transparentno** — svako pravilo ima opis, lako za debugging

## Primeri u kodu

- `RuleEngine.java` — pricing rules, access control, i risk scoring sa composable Predicate-ima

---
