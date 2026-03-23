package raf.edu.week4;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.*;

/**
 * Paradigme Programiranja — Nedelja 4
 * REŠENJA zadataka za vežbe
 */
public class PracticeTasksSolutions {

    // =========================================================================
    // GRUPA E — Collectors.joining i flatMapping
    // =========================================================================

    record StudentPoeni(String ime, int poeni) {}

    // E1
    static String rangLista(List<StudentPoeni> studenti) {
        List<StudentPoeni> sortirani = studenti.stream()
                .sorted(Comparator.comparingInt(StudentPoeni::poeni).reversed())
                .toList();

        return "Rang lista: " + IntStream.range(0, sortirani.size())
                .mapToObj(i -> (i + 1) + ". " + sortirani.get(i).ime() + " (" + sortirani.get(i).poeni() + ")")
                .collect(Collectors.joining(", "));
    }

    // E2
    record BlogPost(String naslov, List<String> tagovi) {}

    static Map<String, List<String>> nasloviPoTagu(List<BlogPost> postovi) {
        return postovi.stream()
                .flatMap(p -> p.tagovi().stream().map(tag -> Map.entry(tag, p.naslov())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    // E3
    record StavkaCena(String naziv, int cena) {}
    record NarudzbinaDetaljno(String kupac, List<StavkaCena> stavke) {}

    static Map<String, Integer> potrosnjaPoKupcu(List<NarudzbinaDetaljno> narudzbine) {
        return narudzbine.stream()
                .collect(Collectors.groupingBy(
                        NarudzbinaDetaljno::kupac,
                        Collectors.flatMapping(
                                n -> n.stavke().stream(),
                                Collectors.summingInt(StavkaCena::cena)
                        )
                ));
    }

    // =========================================================================
    // GRUPA F — Memoizacija i rekurzija
    // =========================================================================

    // F1
    private static final Map<Integer, Long> triboCache = new HashMap<>();

    static long tribonaci(int n) {
        if (n == 0) return 0;
        if (n == 1) return 0;
        if (n == 2) return 1;
        if (triboCache.containsKey(n)) return triboCache.get(n);
        long result = tribonaci(n - 1) + tribonaci(n - 2) + tribonaci(n - 3);
        triboCache.put(n, result);
        return result;
    }

    // F2
    static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return argument -> cache.computeIfAbsent(argument, fn);
    }

    // =========================================================================
    // GRUPA G — Kombinovani zadaci
    // =========================================================================

    // G1
    // Stream.iterate sa 3 argumenta ne uključuje element koji ne zadovoljava hasNext (tj. 1),
    // pa koristimo takeWhile alternativu ili dodamo 1 ručno.
    static List<Integer> collatzNiz(int n) {
        List<Integer> rezultat = Stream.iterate(n, x -> x != 1, x -> x % 2 == 0 ? x / 2 : 3 * x + 1)
                .collect(Collectors.toCollection(ArrayList::new));
        rezultat.add(1);
        return rezultat;
    }

    // G2
    static List<String> prevedeneRecenice(List<String> recenice, Map<String, String> recnik) {
        return recenice.stream()
                .map(recenica -> Arrays.stream(recenica.split(" "))
                        .map(rec -> Optional.ofNullable(recnik.get(rec)).orElse(rec))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // main — pokretanje testova
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("Nedelja 4 — Rešenja zadataka\n");

        // --- E1 ---
        System.out.println("─── E1: Rang lista ───");
        String rang = rangLista(List.of(
                new StudentPoeni("Cara", 78),
                new StudentPoeni("Ana", 92),
                new StudentPoeni("Bojan", 85)));
        System.out.println("  E1: " + rang);
        proveri("E1", rang, "Rang lista: 1. Ana (92), 2. Bojan (85), 3. Cara (78)");

        // --- E2 ---
        System.out.println("─── E2: Naslovi po tagu ───");
        var postovi = List.of(
                new BlogPost("Java Streams", List.of("java", "fp")),
                new BlogPost("Kotlin Intro", List.of("kotlin", "fp")),
                new BlogPost("Java Optional", List.of("java")));
        Map<String, List<String>> e2 = nasloviPoTagu(postovi);
        proveri("E2 java",   e2.getOrDefault("java", List.of()),   List.of("Java Streams", "Java Optional"));
        proveri("E2 fp",     e2.getOrDefault("fp", List.of()),     List.of("Java Streams", "Kotlin Intro"));
        proveri("E2 kotlin", e2.getOrDefault("kotlin", List.of()), List.of("Kotlin Intro"));

        // --- E3 ---
        System.out.println("─── E3: Potrošnja po kupcu ───");
        var nar = List.of(
                new NarudzbinaDetaljno("Ana", List.of(new StavkaCena("hleb", 80), new StavkaCena("mleko", 120))),
                new NarudzbinaDetaljno("Bojan", List.of(new StavkaCena("sok", 150))),
                new NarudzbinaDetaljno("Ana", List.of(new StavkaCena("jaja", 200))));
        Map<String, Integer> e3 = potrosnjaPoKupcu(nar);
        proveri("E3 Ana",   e3.getOrDefault("Ana", 0),   400);
        proveri("E3 Bojan", e3.getOrDefault("Bojan", 0), 150);

        // --- F1 ---
        System.out.println("─── F1: Tribonači ───");
        proveri("F1 t(4)", tribonaci(4), 2L);
        proveri("F1 t(7)", tribonaci(7), 13L);
        proveri("F1 t(0)", tribonaci(0), 0L);

        // --- F2 ---
        System.out.println("─── F2: Memoize ───");
        int[] brojac = {0};
        Function<String, Integer> duzina = memoize(s -> { brojac[0]++; return s.length(); });
        duzina.apply("java");
        duzina.apply("java");
        duzina.apply("stream");
        proveri("F2 pozivi", brojac[0], 2);

        // --- G1 ---
        System.out.println("─── G1: Collatz niz ───");
        proveri("G1 n=6", collatzNiz(6), List.of(6, 3, 10, 5, 16, 8, 4, 2, 1));
        proveri("G1 n=1", collatzNiz(1), List.of(1));

        // --- G2 ---
        System.out.println("─── G2: Prevedene rečenice ───");
        Map<String, String> recnik = Map.of("hello", "zdravo", "world", "svete", "good", "dobar");
        proveri("G2", prevedeneRecenice(List.of("hello world", "good day"), recnik),
                List.of("zdravo svete", "dobar day"));
    }

    static <T> void proveri(String naziv, T dobijeno, T ocekivano) {
        boolean ok = Objects.equals(dobijeno, ocekivano);
        System.out.printf("  [%s] %s%n  Dobijeno:  %s%n  Očekivano: %s%n%n",
                ok ? "✓" : "✗", naziv, dobijeno, ocekivano);
    }
}
