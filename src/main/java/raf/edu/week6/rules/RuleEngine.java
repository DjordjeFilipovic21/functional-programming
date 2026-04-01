package raf.edu.week6.rules;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Nedelja 6 — Rule Engine: composable poslovni uslovi
 *
 * Demonstrira:
 *   1. Rule = Predicate + Function + opis
 *   2. Predicate composition: and, or, negate
 *   3. First-match, all-match, scoring strategije
 *   4. Tri praktična primera: pricing, access control, risk scoring
 */
public class RuleEngine {

    // =========================================================================
    // Generički Rule tip
    // =========================================================================

    /**
     * Pravilo: ako uslov važi za T, primeni akciju i vrati R.
     * Opis služi za logging/debugging — znamo ZAŠTO je nešto primenjeno.
     */
    record Rule<T, R>(Predicate<T> uslov, Function<T, R> akcija, String opis) {

        /** Primeni pravilo ako uslov važi, inače Optional.empty() */
        Optional<R> primeni(T input) {
            if (uslov.test(input)) return Optional.of(akcija.apply(input));
            return Optional.empty();
        }

        /** Debug ispis: da li pravilo važi za dati input */
        String evaluiraj(T input) {
            boolean vazi = uslov.test(input);
            return String.format("  [%s] %s", vazi ? "DA" : "NE", opis);
        }
    }

    /** Pomoćna fabrika za čitljiviji kod */
    static <T, R> Rule<T, R> rule(Predicate<T> uslov, Function<T, R> akcija, String opis) {
        return new Rule<>(uslov, akcija, opis);
    }

    /** Konstantna akcija — ignoriše input */
    static <T, R> Rule<T, R> rule(Predicate<T> uslov, R vrednost, String opis) {
        return new Rule<>(uslov, t -> vrednost, opis);
    }

    // =========================================================================
    // Strategije evaluacije
    // =========================================================================

    /** First-match: primeni PRVO pravilo koje važi */
    static <T, R> Optional<R> firstMatch(List<Rule<T, R>> pravila, T input) {
        return pravila.stream()
                .map(r -> r.primeni(input))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /** First-match sa default vrednošću */
    static <T, R> R firstMatchOrDefault(List<Rule<T, R>> pravila, T input, R defaultVal) {
        return firstMatch(pravila, input).orElse(defaultVal);
    }

    /** All-match: primeni SVA pravila koja važe, vrati listu rezultata */
    static <T, R> List<R> allMatch(List<Rule<T, R>> pravila, T input) {
        return pravila.stream()
                .map(r -> r.primeni(input))
                .flatMap(Optional::stream)
                .toList();
    }

    // =========================================================================
    // Primer 1: Pricing — popusti
    // =========================================================================

    record Kupac(String ime, boolean vip, int godine, double ukupnoKupljeno) {}
    record Narudzbina(Kupac kupac, double vrednost, int brojStavki) {}

    static void pricingPrimer() {
        System.out.println("=== Primer 1: Pricing Rules ===\n");

        // Atomski uslovi — čisti Predicate-i, reusable
        Predicate<Narudzbina> jeVIP         = n -> n.kupac().vip();
        Predicate<Narudzbina> velikiIznos   = n -> n.vrednost() > 10000;
        Predicate<Narudzbina> lojalanKupac  = n -> n.kupac().ukupnoKupljeno() > 50000;
        Predicate<Narudzbina> penzioner     = n -> n.kupac().godine() >= 65;
        Predicate<Narudzbina> veleprodaja   = n -> n.brojStavki() > 20;

        // Pravila — REDOSLED JE BITAN (first-match)
        // Specifičnija pravila idu prva!
        List<Rule<Narudzbina, Double>> popustPravila = List.of(
                rule(jeVIP.and(velikiIznos),          0.25, "VIP + velika narudžbina: 25%"),
                rule(jeVIP.and(lojalanKupac),          0.20, "VIP + lojalan kupac: 20%"),
                rule(jeVIP,                            0.15, "VIP: 15%"),
                rule(velikiIznos.and(veleprodaja),     0.18, "Velika narudžbina + veleprodaja: 18%"),
                rule(velikiIznos,                      0.10, "Velika narudžbina: 10%"),
                rule(penzioner,                        0.08, "Penzionerski popust: 8%"),
                rule(veleprodaja,                      0.05, "Veleprodajni popust: 5%")
        );

        // Test kupci
        List<Narudzbina> narudzbine = List.of(
                new Narudzbina(new Kupac("Ana", true, 35, 80000), 15000, 5),
                new Narudzbina(new Kupac("Bojan", false, 28, 5000), 12000, 3),
                new Narudzbina(new Kupac("Čedomir", false, 70, 20000), 3000, 2),
                new Narudzbina(new Kupac("Dragana", false, 40, 1000), 500, 1),
                new Narudzbina(new Kupac("Eva", true, 30, 10000), 8000, 25)
        );

        narudzbine.forEach(n -> {
            double popust = firstMatchOrDefault(popustPravila, n, 0.0);
            double ustedja = n.vrednost() * popust;

            System.out.printf("  %s: %.2f RSD → popust %.0f%% (%.2f RSD)%n",
                    n.kupac().ime(), n.vrednost(), popust * 100, ustedja);

            // Debug: zašto baš ovaj popust?
            popustPravila.stream()
                    .filter(r -> r.uslov().test(n))
                    .findFirst()
                    .ifPresent(r -> System.out.println("    Primenjeno: " + r.opis()));
        });
    }

    // =========================================================================
    // Primer 2: Access Control
    // =========================================================================

    record Korisnik(String ime, Set<String> uloge, boolean aktivan, int loginPokusaji) {}

    static void accessControlPrimer() {
        System.out.println("\n=== Primer 2: Access Control ===\n");

        // Uslovi
        Predicate<Korisnik> jeAktivan       = Korisnik::aktivan;
        Predicate<Korisnik> jeAdmin         = k -> k.uloge().contains("ADMIN");
        Predicate<Korisnik> jeModerator     = k -> k.uloge().contains("MODERATOR");
        Predicate<Korisnik> jeEditor        = k -> k.uloge().contains("EDITOR");
        Predicate<Korisnik> nijeZakljucan   = k -> k.loginPokusaji() < 5;

        // Kombinovani uslovi — čitaju se kao rečenice
        Predicate<Korisnik> mozeDaBrise     = jeAktivan.and(jeAdmin).and(nijeZakljucan);
        Predicate<Korisnik> mozeDaUredjuje  = jeAktivan.and(jeAdmin.or(jeEditor)).and(nijeZakljucan);
        Predicate<Korisnik> mozeDaMogerira  = jeAktivan.and(jeAdmin.or(jeModerator)).and(nijeZakljucan);
        Predicate<Korisnik> mozeDaCita      = jeAktivan;

        // Pravila pristupa
        record Pristup(String akcija, boolean dozvoljeno) {}

        Function<Korisnik, List<Pristup>> proveriPristup = korisnik -> {
            return List.of(
                    new Pristup("READ",     mozeDaCita.test(korisnik)),
                    new Pristup("EDIT",     mozeDaUredjuje.test(korisnik)),
                    new Pristup("MODERATE", mozeDaMogerira.test(korisnik)),
                    new Pristup("DELETE",   mozeDaBrise.test(korisnik))
            );
        };

        List<Korisnik> korisnici = List.of(
                new Korisnik("Ana",    Set.of("ADMIN"),     true,  0),
                new Korisnik("Bojan",  Set.of("EDITOR"),    true,  0),
                new Korisnik("Cara",   Set.of("ADMIN"),     true,  6), // zaključan!
                new Korisnik("Dragan", Set.of("MODERATOR"), false, 0), // neaktivan!
                new Korisnik("Eva",    Set.of(),            true,  0)  // nema uloge
        );

        korisnici.forEach(k -> {
            System.out.printf("  %s (uloge: %s, aktivan: %s, pokušaji: %d):%n",
                    k.ime(), k.uloge(), k.aktivan(), k.loginPokusaji());

            proveriPristup.apply(k).forEach(p ->
                    System.out.printf("    %-10s %s%n", p.akcija(), p.dozvoljeno() ? "DA" : "NE"));
            System.out.println();
        });
    }

    // =========================================================================
    // Primer 3: Risk Scoring — all-match (akumulacija bodova)
    // =========================================================================

    record Transakcija(double iznos, String zemlja, boolean noviKupac, int satUDanu) {}

    static void riskScoringPrimer() {
        System.out.println("=== Primer 3: Risk Scoring ===\n");

        // Svako pravilo dodaje bodove rizika
        Predicate<Transakcija> velikIznos   = t -> t.iznos() > 50000;
        Predicate<Transakcija> ogromIznos   = t -> t.iznos() > 200000;
        Predicate<Transakcija> rizicnaZemlja = t -> Set.of("XX", "YY", "ZZ").contains(t.zemlja());
        Predicate<Transakcija> noviKupac    = Transakcija::noviKupac;
        Predicate<Transakcija> nocnaTransa  = t -> t.satUDanu() >= 0 && t.satUDanu() < 6;

        List<Rule<Transakcija, Integer>> rizikPravila = List.of(
                rule(velikIznos,                  20, "Iznos > 50K: +20"),
                rule(ogromIznos,                  30, "Iznos > 200K: +30"),
                rule(rizicnaZemlja,               40, "Rizična zemlja: +40"),
                rule(noviKupac,                   15, "Novi kupac: +15"),
                rule(nocnaTransa,                 10, "Noćna transakcija: +10"),
                rule(noviKupac.and(velikIznos),   25, "Novi kupac + veliki iznos: +25")
        );

        List<Transakcija> transakcije = List.of(
                new Transakcija(10000,  "RS", false, 14),
                new Transakcija(75000,  "RS", true,  3),
                new Transakcija(250000, "XX", true,  22),
                new Transakcija(5000,   "RS", false, 10)
        );

        transakcije.forEach(t -> {
            // All-match: sakupi SVE bodove koji važe
            List<Integer> bodovi = allMatch(rizikPravila, t);
            int ukupno = bodovi.stream().mapToInt(Integer::intValue).sum();
            String nivo = ukupno >= 80 ? "VISOK" : ukupno >= 30 ? "SREDNJI" : "NIZAK";

            System.out.printf("  Transakcija %.0f RSD, %s, %s, %02d:00h → rizik: %d (%s)%n",
                    t.iznos(), t.zemlja(), t.noviKupac() ? "novi" : "postojeći", t.satUDanu(),
                    ukupno, nivo);

            // Koji uslovi su aktivirani?
            rizikPravila.stream()
                    .filter(r -> r.uslov().test(t))
                    .forEach(r -> System.out.println("    + " + r.opis()));
            System.out.println();
        });
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        pricingPrimer();
        accessControlPrimer();
        riskScoringPrimer();
    }
}
