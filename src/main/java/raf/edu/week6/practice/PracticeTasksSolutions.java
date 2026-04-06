package raf.edu.week6.practice;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 6
 * REŠENJA zadataka za vežbe
 */
public class PracticeTasksSolutions {

    // =========================================================================
    // Zadatak 1 — Parallel reduce: statistika
    // =========================================================================

    record Stats(int min, int max, long suma, int broj) {
        static final Stats PRAZAN = new Stats(Integer.MAX_VALUE, Integer.MIN_VALUE, 0, 0);

        Stats dodaj(int n) {
            return new Stats(
                    Math.min(min, n),
                    Math.max(max, n),
                    suma + n,
                    broj + 1
            );
        }

        Stats spoji(Stats other) {
            if (other.broj() == 0) return this;
            if (this.broj() == 0) return other;
            return new Stats(
                    Math.min(min, other.min()),
                    Math.max(max, other.max()),
                    suma + other.suma(),
                    broj + other.broj()
            );
        }
    }

    static Stats paralelnaStatistika(List<Integer> brojevi) {
        return brojevi.parallelStream()
                .reduce(Stats.PRAZAN,
                        (stats, n) -> stats.dodaj(n),
                        Stats::spoji);
    }

    // =========================================================================
    // Zadatak 2 — Either parsiranje
    // =========================================================================

    sealed interface Either<L, R> {
        record Left<L, R>(L value) implements Either<L, R> {}
        record Right<L, R>(R value) implements Either<L, R> {}

        static <L, R> Either<L, R> left(L value) { return new Left<>(value); }
        static <L, R> Either<L, R> right(R value) { return new Right<>(value); }

        default boolean isRight() { return this instanceof Right; }
    }

    static Map.Entry<List<Integer>, List<String>> parsirajUnose(List<String> unosi) {
        List<Either<String, Integer>> rezultati = unosi.stream()
                .map(s -> {
                    try {
                        return Either.<String, Integer>right(Integer.parseInt(s.trim()));
                    } catch (NumberFormatException e) {
                        return Either.<String, Integer>left("Neispravan unos: " + s);
                    }
                })
                .toList();

        List<Integer> uspesni = rezultati.stream()
                .filter(Either::isRight)
                .map(e -> ((Either.Right<String, Integer>) e).value())
                .toList();

        List<String> greske = rezultati.stream()
                .filter(e -> !e.isRight())
                .map(e -> ((Either.Left<String, Integer>) e).value())
                .toList();

        return Map.entry(uspesni, greske);
    }

    // =========================================================================
    // Zadatak 3 — Table formatter
    // =========================================================================

    static Function<String, Function<List<Integer>, Function<List<String>, String>>>
    tableFormatter = separator -> widths -> values -> {
        return IntStream.range(0, widths.size())
                .mapToObj(i -> {
                    String val = i < values.size() ? values.get(i) : "";
                    return String.format("%-" + widths.get(i) + "s", val);
                })
                .collect(Collectors.joining(separator));
    };

    // =========================================================================
    // Zadatak 4 — Prihod po državi
    // =========================================================================

    record Porudzbina(String kupac, String drzava, double vrednost, String status) {}

    static Map<String, Double> prihodPoDrzaviImperativ(List<Porudzbina> porudzbine) {
        Map<String, Double> rezultat = new HashMap<>();
        for (Porudzbina p : porudzbine) {
            if (p.status().equals("ISPORUCENO") && p.vrednost() > 100) {
                rezultat.merge(p.drzava(), p.vrednost(), Double::sum);
            }
        }
        List<Map.Entry<String, Double>> sortiran = new ArrayList<>(rezultat.entrySet());
        sortiran.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<String, Double> sortiranaMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : sortiran) {
            sortiranaMap.put(entry.getKey(), entry.getValue());
        }
        return sortiranaMap;
    }

    static Map<String, Double> prihodPoDrzaviFP(List<Porudzbina> porudzbine) {
        return porudzbine.stream()
                .filter(p -> p.status().equals("ISPORUCENO"))
                .filter(p -> p.vrednost() > 100)
                .collect(Collectors.groupingBy(
                        Porudzbina::drzava,
                        Collectors.summingDouble(Porudzbina::vrednost)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // =========================================================================
    // Zadatak 5 — Zaposleni po departmanu
    // =========================================================================

    record Zaposleni(String ime, String departman, String pozicija, int godineStaza) {}

    static Map<String, List<String>> zaposlenaPoDeptImperativ(List<Zaposleni> zaposleni) {
        Map<String, List<String>> rezultat = new TreeMap<>();
        for (Zaposleni z : zaposleni) {
            if (z.godineStaza() > 2) {
                String formatted = z.ime() + " (" + z.pozicija() + ")";
                rezultat.computeIfAbsent(z.departman(), k -> new ArrayList<>()).add(formatted);
            }
        }
        for (List<String> lista : rezultat.values()) {
            Collections.sort(lista);
        }
        return rezultat;
    }

    static Map<String, List<String>> zaposlenaPoDeptFP(List<Zaposleni> zaposleni) {
        return zaposleni.stream()
                .filter(z -> z.godineStaza() > 2)
                .sorted(Comparator.comparing(Zaposleni::ime))
                .collect(Collectors.groupingBy(
                        Zaposleni::departman,
                        TreeMap::new,
                        Collectors.mapping(
                                z -> z.ime() + " (" + z.pozicija() + ")",
                                Collectors.toList()
                        )
                ));
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 6 — Rešenja zadataka\n");

        // --- Zadatak 1 ---
        System.out.println("─── 1: Paralelna statistika ───");
        Stats s1 = paralelnaStatistika(List.of(3, 1, 4, 1, 5, 9));
        proveri("Z1 min",  s1.min(),  1);
        proveri("Z1 max",  s1.max(),  9);
        proveri("Z1 suma", s1.suma(), 23L);
        proveri("Z1 broj", s1.broj(), 6);

        Stats s2 = paralelnaStatistika(List.of(42));
        proveri("Z1 jedan element", s2, new Stats(42, 42, 42, 1));

        // --- Zadatak 2 ---
        System.out.println("─── 2: Either parsiranje ───");
        var r2 = parsirajUnose(List.of("42", "abc", "7", "", "13"));
        proveri("Z2 uspesni", r2.getKey(), List.of(42, 7, 13));
        proveri("Z2 greske",  r2.getValue(),
                List.of("Neispravan unos: abc", "Neispravan unos: "));

        // --- Zadatak 3 ---
        System.out.println("─── 3: Table formatter ───");
        var formater = tableFormatter.apply(" | ").apply(List.of(10, 5, 8));
        proveri("Z3 pun red",
                formater.apply(List.of("Ana", "92", "IT")),
                "Ana        | 92    | IT      ");
        proveri("Z3 kratak red",
                formater.apply(List.of("Bojan", "45")),
                "Bojan      | 45    |         ");

        // --- Zadatak 4 ---
        System.out.println("─── 4: Prihod po državi ───");
        List<Porudzbina> porudzbine = List.of(
                new Porudzbina("Ana",    "Srbija",  250, "ISPORUCENO"),
                new Porudzbina("Bojan",  "Srbija",  80,  "ISPORUCENO"),
                new Porudzbina("Cara",   "BiH",     300, "ISPORUCENO"),
                new Porudzbina("Dragan", "Srbija",  150, "OTKAZANO"),
                new Porudzbina("Eva",    "BiH",     200, "ISPORUCENO"),
                new Porudzbina("Filip",  "CG",      120, "ISPORUCENO")
        );
        Map<String, Double> imp4 = prihodPoDrzaviImperativ(porudzbine);
        Map<String, Double> fp4  = prihodPoDrzaviFP(porudzbine);
        System.out.println("  Imperativ: " + imp4);
        System.out.println("  FP:        " + fp4);
        proveri("Z4", fp4, imp4);

        // --- Zadatak 5 ---
        System.out.println("─── 5: Zaposleni po departmanu ───");
        List<Zaposleni> zaposleni = List.of(
                new Zaposleni("Ana",    "IT",  "Developer",   5),
                new Zaposleni("Bojan",  "IT",  "QA",          1),
                new Zaposleni("Cara",   "HR",  "Recruiter",   3),
                new Zaposleni("Dragan", "IT",  "DevOps",      4),
                new Zaposleni("Eva",    "HR",  "Manager",     7),
                new Zaposleni("Filip",  "IT",  "Developer",   3)
        );
        Map<String, List<String>> imp5 = zaposlenaPoDeptImperativ(zaposleni);
        Map<String, List<String>> fp5  = zaposlenaPoDeptFP(zaposleni);
        System.out.println("  Imperativ: " + imp5);
        System.out.println("  FP:        " + fp5);
        proveri("Z5", fp5, imp5);
    }

    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
