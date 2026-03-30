package raf.edu.week6.currying;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Nedelja 6 — Currying i parcijalna primena
 *
 * Demonstrira:
 *   1. Currying: f(a, b) → f(a)(b)
 *   2. Parcijalna primena: fiksiranje argumenata
 *   3. Praktični primeri: konfigurabilne funkcije
 *   4. Curry utility: generička konverzija
 *   5. Primena u stream pipeline-u
 */
public class CurryingExamples {

    public static void main(String[] args) {
        System.out.println("=== 1. Currying: osnove ===\n");
        curryingOsnove();

        System.out.println("\n=== 2. Parcijalna primena ===\n");
        parcijalnaPrimena();

        System.out.println("\n=== 3. Konfigurabilne funkcije ===\n");
        konfigurabilneFunkcije();

        System.out.println("\n=== 4. Curry utility ===\n");
        curryUtility();

        System.out.println("\n=== 5. Primena u stream pipeline-u ===\n");
        primenaUStreamu();
    }

    // =========================================================================
    // 1. Currying: osnove
    // =========================================================================
    static void curryingOsnove() {
        // Regularna funkcija sa dva argumenta
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        System.out.println("BiFunction add(3, 5) = " + add.apply(3, 5));

        // Curried verzija: Function koji VRAĆA Function
        Function<Integer, Function<Integer, Integer>> addCurried = a -> b -> a + b;
        System.out.println("Curried add(3)(5) = " + addCurried.apply(3).apply(5));

        // Čitanje: addCurried.apply(3) vraća NOVU FUNKCIJU: b -> 3 + b
        Function<Integer, Integer> add3 = addCurried.apply(3);
        System.out.println("add3(5) = " + add3.apply(5));
        System.out.println("add3(10) = " + add3.apply(10));
        System.out.println("add3(0) = " + add3.apply(0));

        // Tri argumenta
        Function<Integer, Function<Integer, Function<Integer, Integer>>> triArg =
                a -> b -> c -> a + b + c;
        System.out.println("\ntriArg(1)(2)(3) = " + triArg.apply(1).apply(2).apply(3));

        // Svaki korak vraća novu funkciju
        var korak1 = triArg.apply(10);          // b -> c -> 10 + b + c
        var korak2 = korak1.apply(20);          // c -> 10 + 20 + c
        var korak3 = korak2.apply(30);          // 60
        System.out.println("Korak po korak: " + korak3);
    }

    // =========================================================================
    // 2. Parcijalna primena
    // =========================================================================
    static void parcijalnaPrimena() {
        // Množenje
        Function<Double, Function<Double, Double>> multiply = a -> b -> a * b;

        // Fiksiramo prvi argument — dobijamo specijalizovane funkcije
        Function<Double, Double> double_ = multiply.apply(2.0);
        Function<Double, Double> triple  = multiply.apply(3.0);
        Function<Double, Double> half    = multiply.apply(0.5);

        System.out.println("double(7) = " + double_.apply(7.0));
        System.out.println("triple(7) = " + triple.apply(7.0));
        System.out.println("half(7) = " + half.apply(7.0));

        // Konverzija temperatura
        // formula: (celsius * factor) + offset
        // Celsius → Fahrenheit: C * 9/5 + 32
        // Celsius → Kelvin:     C * 1 + 273.15
        Function<Double, Function<Double, Function<Double, Double>>> convert =
                factor -> offset -> value -> value * factor + offset;

        Function<Double, Double> celsiusToFahrenheit = convert.apply(9.0 / 5).apply(32.0);
        Function<Double, Double> celsiusToKelvin     = convert.apply(1.0).apply(273.15);

        System.out.println("\n100°C → " + celsiusToFahrenheit.apply(100.0) + "°F");
        System.out.println("100°C → " + celsiusToKelvin.apply(100.0) + "K");
        System.out.println("0°C   → " + celsiusToFahrenheit.apply(0.0) + "°F");
        System.out.println("0°C   → " + celsiusToKelvin.apply(0.0) + "K");
    }

    // =========================================================================
    // 3. Konfigurabilne funkcije
    // =========================================================================
    static void konfigurabilneFunkcije() {
        // Predicate fabrika: "počinje sa X"
        Function<String, Predicate<String>> startsWith = prefix -> s -> s.startsWith(prefix);

        List<String> jezici = List.of("Java", "JavaScript", "Python", "Kotlin", "Julia");

        List<String> jJezici = jezici.stream().filter(startsWith.apply("J")).toList();
        List<String> kJezici = jezici.stream().filter(startsWith.apply("K")).toList();
        System.out.println("J* jezici: " + jJezici);
        System.out.println("K* jezici: " + kJezici);

        // Formatter fabrika: konfiguriši format, pa primeni na podatke
        Function<String, Function<Object[], String>> formatter =
                pattern -> args -> String.format(pattern, args);

        Function<Object[], String> logFormat = formatter.apply("[%s] %s: %s");
        Function<Object[], String> csvFormat = formatter.apply("%s,%s,%s");

        Object[] data = {"INFO", "main", "Started"};
        System.out.println("\nLog: " + logFormat.apply(data));
        System.out.println("CSV: " + csvFormat.apply(data));

        // Comparator fabrika
        Function<String, Comparator<Map<String, Object>>> sortBy =
                key -> Comparator.comparing(m -> m.get(key).toString());

        // Validator fabrika: min/max granica
        Function<Integer, Function<Integer, Predicate<Integer>>> inRange =
                min -> max -> n -> n >= min && n <= max;

        Predicate<Integer> validnaOcena = inRange.apply(1).apply(5);
        Predicate<Integer> validnaGodina = inRange.apply(1900).apply(2100);

        System.out.println("\n3 validna ocena? " + validnaOcena.test(3));
        System.out.println("6 validna ocena? " + validnaOcena.test(6));
        System.out.println("2024 validna godina? " + validnaGodina.test(2024));
    }

    // =========================================================================
    // 4. Curry utility: generička konverzija
    // =========================================================================
    static void curryUtility() {
        // Generička curry metoda: BiFunction → curried Function
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        Function<String, Function<Integer, String>> curriedRepeat = curry(repeat);

        Function<Integer, String> repeatStar = curriedRepeat.apply("* ");
        System.out.println("3 zvezdice: " + repeatStar.apply(3));
        System.out.println("5 zvezdica: " + repeatStar.apply(5));

        // Uncurry: obrnuta operacija
        BiFunction<String, Integer, String> uncurried = uncurry(curriedRepeat);
        System.out.println("Uncurried: " + uncurried.apply("- ", 4));
    }

    /** Pretvara BiFunction u curried Function */
    static <A, B, R> Function<A, Function<B, R>> curry(BiFunction<A, B, R> fn) {
        return a -> b -> fn.apply(a, b);
    }

    /** Pretvara curried Function nazad u BiFunction */
    static <A, B, R> BiFunction<A, B, R> uncurry(Function<A, Function<B, R>> fn) {
        return (a, b) -> fn.apply(a).apply(b);
    }

    // =========================================================================
    // 5. Primena u stream pipeline-u
    // =========================================================================
    static void primenaUStreamu() {
        record Student(String ime, int poeni, String smer) {}

        List<Student> studenti = List.of(
                new Student("Ana", 92, "IT"),
                new Student("Bojan", 45, "IT"),
                new Student("Cara", 78, "Matematika"),
                new Student("Dragan", 55, "IT"),
                new Student("Eva", 88, "Matematika"),
                new Student("Filip", 30, "Fizika")
        );

        // Curried filter fabrike
        Function<String, Predicate<Student>> izSmera =
                smer -> s -> s.smer().equals(smer);
        Function<Integer, Predicate<Student>> minPoeni =
                min -> s -> s.poeni() >= min;

        // Razne kombinacije filtera — bez dupliranja koda
        System.out.println("IT studenti:");
        studenti.stream().filter(izSmera.apply("IT"))
                .forEach(s -> System.out.println("  " + s));

        System.out.println("\nPoložili (>=50) iz Matematike:");
        studenti.stream()
                .filter(izSmera.apply("Matematika").and(minPoeni.apply(50)))
                .forEach(s -> System.out.println("  " + s));

        // Curried mapper: formatiranje sa konfiguracijom
        Function<String, Function<Student, String>> formatStudent =
                pattern -> s -> String.format(pattern, s.ime(), s.poeni(), s.smer());

        Function<Student, String> kratakFormat = formatStudent.apply("%s (%d)");
        Function<Student, String> dugacakFormat = formatStudent.apply("%s — %d poena, smer: %s");

        System.out.println("\nKratak format:");
        studenti.stream().map(kratakFormat).forEach(s -> System.out.println("  " + s));

        System.out.println("\nDugačak format (samo koji su položili):");
        studenti.stream()
                .filter(minPoeni.apply(50))
                .map(dugacakFormat)
                .forEach(s -> System.out.println("  " + s));
    }
}
