# State Machine — Funkcionalne tranzicije stanja

## Problem

Svaki sistem koji ima "životni ciklus" je state machine:
narudžbina (kreirana → plaćena → poslata → isporučena),
ticket (otvoren → u radu → rešen → zatvoren),
korisnik (registrovan → aktivan → suspendovan → obrisan).

Imperativni pristup obično koristi `switch` + `if` kaskadu:

```java
// RUŽNO: svaka tranzicija je if/else, teško za proširivanje
void obradi(Narudzbina n, String akcija) {
    if (n.status == "KREIRANA" && akcija == "plati") { n.status = "PLACENA"; }
    else if (n.status == "PLACENA" && akcija == "posalji") { n.status = "POSLATA"; }
    else if (...) { ... }
    // 20+ linija za svaki novi status
}
```

## FP rešenje

Svaka tranzicija je **čista funkcija**: `(Stanje, Event) → NovoStanje`.
Dozvoljene tranzicije su **mapa**: `(Stanje, Event) → Optional<NovoStanje>`.

```
        plati          posalji        isporuci
KREIRANA ────→ PLACENA ────→ POSLATA ────→ ISPORUCENA
    │                            │
    │ otkazi                     │ vrati
    ▼                            ▼
OTKAZANA                    VRACENA
```

Prednosti:
- **Eksplicitne tranzicije** — sve dozvoljene promene stanja su na jednom mestu
- **Kompajler pomaže** — sealed interface garantuje da su sva stanja pokrivena
- **Immutable** — staro stanje se ne menja, dobijamo novo
- **Testabilno** — svaka tranzicija je čista funkcija
- **Proširivo** — dodavanje novog stanja/eventa ne zahteva menjanje postojećeg koda

## Primeri u kodu

- `OrderStateMachine.java` — narudžbina sa sealed stanjima, mapom tranzicija, i obradom niza akcija

---
