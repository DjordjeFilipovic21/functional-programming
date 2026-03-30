package raf.edu.week6.errorhandling;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Nedelja 6 — Obrada grešaka u FP
 *
 * Demonstrira:
 *   1. Problem: checked exceptions u lambdama
 *   2. Wrapper pattern: unchecked() adapter
 *   3. Either tip: eksplicitna greška u tipu
 *   4. Praktični primeri obrade grešaka u stream pipeline-u
 */
public class ErrorHandling {

    // =========================================================================
    // 1. Problem: checked exceptions u lambdama
    // =========================================================================

    // Simuliramo operacije koje bacaju checked exception
    static String procitajFajl(String path) throws java.io.IOException {
        if (path.contains("error")) throw new java.io.IOException("Fajl ne postoji: " + path);
        return "sadržaj od " + path;
    }

    static int parsirajBroj(String s) throws Exception {
        if (s == null || s.isBlank()) throw new Exception("Prazan string");
        return Integer.parseInt(s); // baca NumberFormatException (unchecked, ali ilustracija)
    }

    // =========================================================================
    // 2. Wrapper pattern: CheckedFunction → Function
    // =========================================================================

    /**
     * Funkcionalni interfejs koji DOZVOLJAVA checked exception.
     * Java-in Function<T,R> to ne dozvoljava.
     */
    @FunctionalInterface
    interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    interface CheckedConsumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Omotač: pretvara CheckedFunction u Function.
     * Checked exception se baca kao RuntimeException.
     */
    static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Sigurniji omotač: vraća Optional.empty() umesto da baca exception.
     */
    static <T, R> Function<T, Optional<R>> safe(CheckedFunction<T, R> fn) {
        return t -> {
            try {
                return Optional.of(fn.apply(t));
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    /**
     * Najinformativniji omotač: vraća Either sa greškom ili rezultatom.
     */
    static <T, R> Function<T, Either<String, R>> trying(CheckedFunction<T, R> fn) {
        return t -> {
            try {
                return Either.right(fn.apply(t));
            } catch (Exception e) {
                return Either.left(e.getMessage());
            }
        };
    }

    // =========================================================================
    // 3. Either tip
    // =========================================================================

    /**
     * Either<L, R> — ili leva vrednost (greška) ili desna vrednost (uspeh).
     *
     * Konvencija: Left = greška, Right = uspeh ("right" = ispravno).
     */
    sealed interface Either<L, R> {
        record Left<L, R>(L value) implements Either<L, R> {}
        record Right<L, R>(R value) implements Either<L, R> {}

        static <L, R> Either<L, R> left(L value)  { return new Left<>(value); }
        static <L, R> Either<L, R> right(R value) { return new Right<>(value); }

        default boolean isRight() { return this instanceof Right; }
        default boolean isLeft()  { return this instanceof Left; }

        /** map na desnoj strani (uspeh) — leva se ne dira */
        default <T> Either<L, T> map(Function<R, T> fn) {
            return switch (this) {
                case Right<L, R> r -> right(fn.apply(r.value()));
                case Left<L, R> l  -> left(l.value());
            };
        }

        /** flatMap na desnoj strani */
        default <T> Either<L, T> flatMap(Function<R, Either<L, T>> fn) {
            return switch (this) {
                case Right<L, R> r -> fn.apply(r.value());
                case Left<L, R> l  -> left(l.value());
            };
        }

        /** Raspakuj: obradi oba slučaja */
        default <T> T fold(Function<L, T> onLeft, Function<R, T> onRight) {
            return switch (this) {
                case Right<L, R> r -> onRight.apply(r.value());
                case Left<L, R> l  -> onLeft.apply(l.value());
            };
        }

        /** Konvertuj u Optional (gubi informaciju o grešci) */
        default Optional<R> toOptional() {
            return switch (this) {
                case Right<L, R> r -> Optional.of(r.value());
                case Left<L, R> l  -> Optional.empty();
            };
        }
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("=== 1. Problem: checked exceptions ===\n");
        checkedExceptionProblem();

        System.out.println("\n=== 2. Wrapper pattern: unchecked() ===\n");
        wrapperPattern();

        System.out.println("\n=== 3. Safe wrapper: Optional ===\n");
        safeWrapper();

        System.out.println("\n=== 4. Either: eksplicitna greška ===\n");
        eitherPattern();

        System.out.println("\n=== 5. Praktičan primer: obrada fajlova ===\n");
        prakticanPrimer();
    }

    // =========================================================================
    // Primeri
    // =========================================================================

    static void checkedExceptionProblem() {
        List<String> putanje = List.of("file1.txt", "error.txt", "file2.txt");

        System.out.println("Problem: ovo NE KOMPAJLIRA:");
        System.out.println("  putanje.stream().map(p -> procitajFajl(p))");
        System.out.println("  // error: unreported exception IOException");
        System.out.println();

        // Ružno rešenje: try-catch UNUTAR lambde
        System.out.println("Ružno rešenje (try-catch u lambdi):");
        putanje.stream()
                .map(p -> {
                    try {
                        return procitajFajl(p);
                    } catch (java.io.IOException e) {
                        return "GREŠKA: " + e.getMessage();
                    }
                })
                .forEach(s -> System.out.println("  " + s));

        System.out.println("\nProblem: try-catch u svakoj lambdi je ružno i narušava čitljivost.");
    }

    static void wrapperPattern() {
        List<String> putanje = List.of("file1.txt", "file2.txt");

        // unchecked() — čisto, čitljivo
        System.out.println("Sa unchecked() wrapperom:");
        putanje.stream()
                .map(unchecked(ErrorHandling::procitajFajl))
                .forEach(s -> System.out.println("  " + s));

        // Ali: ako DOĐE do greške, baca RuntimeException
        System.out.println("\nunchecked() sa greškom:");
        try {
            List.of("file1.txt", "error.txt").stream()
                    .map(unchecked(ErrorHandling::procitajFajl))
                    .forEach(s -> System.out.println("  " + s));
        } catch (RuntimeException e) {
            System.out.println("  RuntimeException: " + e.getCause().getMessage());
        }

        System.out.println("\nunchecked() je dobar kad greška TREBA da prekine pipeline.");
    }

    static void safeWrapper() {
        List<String> putanje = List.of("file1.txt", "error.txt", "file2.txt", "error2.txt");

        // safe() — vraća Optional, neuspeli se tiho preskaču
        System.out.println("Sa safe() wrapperom (preskače greške):");
        List<String> rezultati = putanje.stream()
                .map(safe(ErrorHandling::procitajFajl))
                .flatMap(Optional::stream)
                .toList();
        System.out.println("  Rezultati: " + rezultati);
        System.out.println("  (2 od 4 uspešna — 2 greške tiho preskočene)");

        System.out.println("\nsafe() je dobar kad neuspeh znači 'preskoči ovaj element'.");
        System.out.println("Loše: ne znamo ZAŠTO je nešto preskočeno.");
    }

    static void eitherPattern() {
        List<String> putanje = List.of("file1.txt", "error.txt", "file2.txt", "error2.txt");

        // trying() — vraća Either, i uspesi i greške su sačuvani
        System.out.println("Sa trying() wrapperom (čuva informaciju o grešci):");
        List<Either<String, String>> rezultati = putanje.stream()
                .map(trying(ErrorHandling::procitajFajl))
                .toList();

        rezultati.forEach(either -> {
            String poruka = either.fold(
                    err -> "  GREŠKA: " + err,
                    ok  -> "  OK: " + ok
            );
            System.out.println(poruka);
        });

        // Razdvajanje uspešnih i neuspešnih
        List<String> uspesni = rezultati.stream()
                .filter(Either::isRight)
                .map(e -> ((Either.Right<String, String>) e).value())
                .toList();
        List<String> greske = rezultati.stream()
                .filter(Either::isLeft)
                .map(e -> ((Either.Left<String, String>) e).value())
                .toList();

        System.out.println("\n  Uspešnih: " + uspesni.size() + " → " + uspesni);
        System.out.println("  Grešaka:  " + greske.size() + " → " + greske);
    }

    static void prakticanPrimer() {
        // Scenario: parsiramo konfiguraciju iz stringova "ključ=vrednost"
        List<String> konfig = List.of(
                "port=8080",
                "timeout=30",
                "debug=true",
                "maxconn=",       // nevalidno — prazna vrednost
                "invalid-line",   // nevalidno — nema =
                "threads=4"
        );

        record KonfigEntry(String kljuc, String vrednost) {}

        // Either za parsiranje
        Function<String, Either<String, KonfigEntry>> parseEntry = line -> {
            String[] delovi = line.split("=", 2);
            if (delovi.length != 2)
                return Either.left("Nema '=' u liniji: " + line);
            if (delovi[1].isBlank())
                return Either.left("Prazna vrednost za ključ: " + delovi[0]);
            return Either.right(new KonfigEntry(delovi[0].trim(), delovi[1].trim()));
        };

        List<Either<String, KonfigEntry>> rezultati = konfig.stream()
                .map(parseEntry)
                .toList();

        System.out.println("Parsiranje konfiguracije:");
        rezultati.forEach(e -> System.out.println("  " + e));

        // Samo validne entry-je u mapu
        Map<String, String> mapa = rezultati.stream()
                .filter(Either::isRight)
                .map(e -> ((Either.Right<String, KonfigEntry>) e).value())
                .collect(Collectors.toMap(KonfigEntry::kljuc, KonfigEntry::vrednost));
        System.out.println("\nValidna konfiguracija: " + mapa);

        // Greške za logovanje
        List<String> greske = rezultati.stream()
                .filter(Either::isLeft)
                .map(e -> ((Either.Left<String, KonfigEntry>) e).value())
                .toList();
        System.out.println("Greške: " + greske);
    }
}
