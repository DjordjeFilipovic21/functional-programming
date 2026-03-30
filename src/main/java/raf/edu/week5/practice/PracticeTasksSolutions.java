package raf.edu.week5.practice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 5
 * REŠENJA zadataka za vežbe
 */
public class PracticeTasksSolutions {

    // =========================================================================
    // Zadatak 1 — ETL pipeline: obrada log zapisa
    // =========================================================================

    record LogEntry(String level, String timestamp, String message) {}

    static Optional<LogEntry> parseLog(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) return Optional.empty();
        return Optional.of(new LogEntry(parts[0].trim(), parts[1].trim(), parts[2].trim()));
    }

    static List<String> sumarniLogIzvestaj(List<String> logZapisi) {
        return logZapisi.stream()
                .map(PracticeTasksSolutions::parseLog)
                .flatMap(Optional::stream)
                .filter(e -> e.level().equals("ERROR") || e.level().equals("WARN"))
                .collect(Collectors.groupingBy(LogEntry::level))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s (%d): %s",
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .map(LogEntry::message)
                                .collect(Collectors.joining("; "))))
                .toList();
    }

    // =========================================================================
    // Zadatak 2 — Reduce: kumulativni bilans
    // =========================================================================

    record Transakcija(String opis, double iznos) {}

    record Bilans(double prihod, double rashod, double bilans, int brojTransakcija) {
        static final Bilans PRAZAN = new Bilans(0, 0, 0, 0);

        Bilans dodaj(Transakcija t) {
            double noviPrihod = prihod + Math.max(0, t.iznos());
            double noviRashod = rashod + Math.abs(Math.min(0, t.iznos()));
            return new Bilans(noviPrihod, noviRashod, noviPrihod - noviRashod, brojTransakcija + 1);
        }

        Bilans spoji(Bilans other) {
            double p = prihod + other.prihod();
            double r = rashod + other.rashod();
            return new Bilans(p, r, p - r, brojTransakcija + other.brojTransakcija());
        }
    }

    static Bilans izracunajBilans(List<Transakcija> transakcije) {
        return transakcije.stream()
                .reduce(Bilans.PRAZAN,
                        Bilans::dodaj,
                        Bilans::spoji);
    }

    // =========================================================================
    // Zadatak 3 — Function composition
    // =========================================================================

    static UnaryOperator<String> compose(List<UnaryOperator<String>> transformacije) {
        return transformacije.stream()
                .reduce(UnaryOperator.identity(), (a, b) -> s -> b.apply(a.apply(s)));
    }

    static String sanitize(String input, int maxLen) {
        UnaryOperator<String> pipeline = compose(List.of(
                String::trim,
                String::toLowerCase,
                s -> s.replaceAll("\\s+", " "),
                s -> s.replaceAll("[^a-z0-9 ]", ""),
                s -> s.substring(0, Math.min(s.length(), maxLen))
        ));
        return pipeline.apply(input);
    }

    // =========================================================================
    // Zadatak 4 — Refactoring: inventar prodavnice
    // =========================================================================

    record Proizvod(String naziv, String kategorija, double cena, int kolicina) {}

    static List<String> inventarIzvestajImperativ(List<Proizvod> proizvodi, int topN) {
        Map<String, Double> vrednostPoKategoriji = new HashMap<>();
        for (Proizvod p : proizvodi) {
            if (p.kolicina() > 0) {
                double vrednost = p.cena() * p.kolicina();
                vrednostPoKategoriji.merge(p.kategorija(), vrednost, Double::sum);
            }
        }
        List<Map.Entry<String, Double>> sortiran = new ArrayList<>(vrednostPoKategoriji.entrySet());
        sortiran.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<String> rezultat = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, sortiran.size()); i++) {
            Map.Entry<String, Double> entry = sortiran.get(i);
            rezultat.add(String.format("%s: %.2f din", entry.getKey(), entry.getValue()));
        }
        return rezultat;
    }

    static List<String> inventarIzvestajFP(List<Proizvod> proizvodi, int topN) {
        return proizvodi.stream()
                .filter(p -> p.kolicina() > 0)
                .collect(Collectors.groupingBy(
                        Proizvod::kategorija,
                        Collectors.summingDouble(p -> p.cena() * p.kolicina())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(e -> String.format("%s: %.2f din", e.getKey(), e.getValue()))
                .toList();
    }

    // =========================================================================
    // Zadatak 5 — Refactoring: analiza teksta
    // =========================================================================

    static final Set<String> STOP_RECI = Set.of(
            "i", "u", "na", "je", "za", "sa", "se", "da", "ne", "od", "do", "a", "ali", "ili"
    );

    static List<String> analizaTekstaImperativ(String tekst, int topN) {
        String[] reci = tekst.toLowerCase().split("\\s+");
        Map<String, Integer> brojac = new HashMap<>();
        for (String rec : reci) {
            String cista = rec.replaceAll("[^a-zčćšđž]", "");
            if (!cista.isEmpty() && !STOP_RECI.contains(cista)) {
                brojac.merge(cista, 1, Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> sortiran = new ArrayList<>(brojac.entrySet());
        sortiran.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            if (cmp != 0) return cmp;
            return a.getKey().compareTo(b.getKey());
        });
        List<String> rezultat = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, sortiran.size()); i++) {
            Map.Entry<String, Integer> e = sortiran.get(i);
            rezultat.add(e.getKey() + ": " + e.getValue());
        }
        return rezultat;
    }

    static List<String> analizaTekstaFP(String tekst, int topN) {
        return Arrays.stream(tekst.toLowerCase().split("\\s+"))
                .map(rec -> rec.replaceAll("[^a-zčćšđž]", ""))
                .filter(rec -> !rec.isEmpty())
                .filter(rec -> !STOP_RECI.contains(rec))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 5 — Rešenja zadataka\n");

        // --- Zadatak 1 ---
        System.out.println("─── 1: Sumarni log izveštaj ───");
        List<String> logovi = List.of(
                "ERROR|10:00|Disk full",
                "INFO|10:01|Started",
                "WARN|10:02|High CPU",
                "nevalidan zapis",
                "ERROR|10:03|Out of memory",
                "INFO|10:04|Request OK",
                "WARN|10:05|Low disk space",
                "DEBUG|10:06|Cache hit"
        );
        proveri("Z1", sumarniLogIzvestaj(logovi),
                List.of("ERROR (2): Disk full; Out of memory",
                        "WARN (2): High CPU; Low disk space"));

        // --- Zadatak 2 ---
        System.out.println("─── 2: Bilans ───");
        List<Transakcija> transakcije = List.of(
                new Transakcija("Plata", 5000),
                new Transakcija("Kirija", -2000),
                new Transakcija("Freelance", 1500),
                new Transakcija("Računi", -800)
        );
        Bilans b = izracunajBilans(transakcije);
        proveriDouble("Z2 prihod",  b.prihod(),  6500.0);
        proveriDouble("Z2 rashod",  b.rashod(),  2800.0);
        proveriDouble("Z2 bilans",  b.bilans(),  3700.0);
        proveri("Z2 broj",          b.brojTransakcija(), 4);

        Bilans prazan = izracunajBilans(List.of());
        proveri("Z2 prazan", prazan, Bilans.PRAZAN);

        // --- Zadatak 3 ---
        System.out.println("─── 3: Function composition ───");
        UnaryOperator<String> pipeline = compose(List.of(
                String::trim,
                String::toLowerCase
        ));
        proveri("Z3 compose", pipeline.apply("  HELLO  "), "hello");
        proveri("Z3 sanitize", sanitize("  Hello   WORLD @#$ 123  ", 20), "hello world  123");
        proveri("Z3 sanitize short", sanitize("abcdef", 3), "abc");

        // --- Zadatak 4 ---
        System.out.println("─── 4: Inventar (imperativ vs FP) ───");
        List<Proizvod> proizvodi = List.of(
                new Proizvod("Laptop",     "Elektronika", 80000, 5),
                new Proizvod("Miš",        "Elektronika", 2000,  20),
                new Proizvod("Stolica",    "Nameštaj",    15000, 8),
                new Proizvod("Sto",        "Nameštaj",    25000, 3),
                new Proizvod("Java knjiga","Knjige",       3000,  0),
                new Proizvod("FP knjiga",  "Knjige",       2500, 12),
                new Proizvod("Monitor",    "Elektronika", 35000, 3)
        );
        List<String> imperative4 = inventarIzvestajImperativ(proizvodi, 3);
        List<String> fp4 = inventarIzvestajFP(proizvodi, 3);
        System.out.println("  Imperativ: " + imperative4);
        System.out.println("  FP:        " + fp4);
        proveri("Z4", fp4, imperative4);

        // --- Zadatak 5 ---
        System.out.println("─── 5: Analiza teksta (imperativ vs FP) ───");
        String tekst = "Java je moćan jezik. Java se koristi za web aplikacije i za mobilne aplikacije. "
                + "Funkcionalno programiranje u Java jeziku je sve popularnije. "
                + "Stream API je deo Java platforme.";
        List<String> imperative5 = analizaTekstaImperativ(tekst, 5);
        List<String> fp5 = analizaTekstaFP(tekst, 5);
        System.out.println("  Imperativ: " + imperative5);
        System.out.println("  FP:        " + fp5);
        proveri("Z5", fp5, imperative5);
    }

    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }

    static void proveriDouble(String naziv, double dobijeno, double ocekivano) {
        boolean ok = Math.abs(dobijeno - ocekivano) < 0.01;
        System.out.printf("  [%s] %s%n  Dobijeno:  %.2f%n  Očekivano: %.2f%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
