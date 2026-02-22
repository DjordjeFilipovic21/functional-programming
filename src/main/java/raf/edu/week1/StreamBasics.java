package raf.edu.week1;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Nedelja 1 — Stream API: osnove
 *
 * Stream je sekvenca elemenata nad kojom primenjujemo operacije
 * u pipeline arhitekturi. Ne menja originalnu kolekciju.
 *
 * Tipovi operacija:
 *   - Intermediate (posredne): vraćaju Stream, LENJI su (lazy)
 *     filter, map, sorted, distinct, limit, skip, peek, ...
 *   - Terminal (završne): pokreću izračunavanje, vraćaju rezultat
 *     forEach, collect, count, sum, min, max, reduce, findFirst, anyMatch, ...
 *
 * Poređenje sa imperativnim rešenjima.
 */
public class StreamBasics {

    public static void main(String[] args) {
        List<String> prijatelji = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");
        List<Integer> cene = Arrays.asList(10, 30, 17, 20, 18, 45, 12);

        System.out.println("=== forEach ===\n");
        forEachPrimer(prijatelji);

        System.out.println("\n=== filter ===\n");
        filterPrimer(prijatelji, cene);

        System.out.println("\n=== map ===\n");
        mapPrimer(prijatelji);

        System.out.println("\n=== collect ===\n");
        collectPrimer(prijatelji);

        System.out.println("\n=== Agregacione operacije: sum, count, min, max ===\n");
        agregacije(cene);

        System.out.println("\n=== Složen pipeline ===\n");
        slozeniPipeline(prijatelji, cene);

        System.out.println("\n=== Kreiranje Stream-a na razne načine ===\n");
        kreiranjeStream();
    }

    // -----------------------------------------------------------------------
    // forEach — Terminal operacija
    // Tip argumenta: Consumer<T>  (T -> void)
    // -----------------------------------------------------------------------
    static void forEachPrimer(List<String> prijatelji) {
        // Imperativno — eksplicitna petlja
        System.out.println("Imperativno:");
        for (String ime : prijatelji) {
            System.out.println("  Zdravo, " + ime);
        }

        // Funkcionalno — forEach sa lambda izrazom
        System.out.println("Funkcionalno:");
        prijatelji.forEach(ime -> System.out.println("  Zdravo, " + ime));

        // Direktno na listi (bez stream()) — interno iterira
        System.out.println("Samo štampanje (method reference):");
        prijatelji.forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // filter — Intermediate operacija
    // Tip argumenta: Predicate<T>  (T -> boolean)
    // -----------------------------------------------------------------------
    static void filterPrimer(List<String> prijatelji, List<Integer> cene) {
        // Imperativno — filtriranje dugih imena
        System.out.println("Imperativno (imena duža od 4 slova):");
        for (String ime : prijatelji) {
            if (ime.length() > 4) {
                System.out.println("  " + ime);
            }
        }

        // Funkcionalno
        System.out.println("Funkcionalno (imena duža od 4 slova):");
        prijatelji.stream()
                .filter(ime -> ime.length() > 4)
                .forEach(System.out::println);

        // Višestruko filtriranje — cene između 15 i 30
        System.out.println("\nCene između 15 i 30:");
        cene.stream()
                .filter(c -> c >= 15)
                .filter(c -> c <= 30)
                .forEach(c -> System.out.print(c + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // map — Intermediate operacija
    // Tip argumenta: Function<T, R>  (T -> R)
    // Transformiše svaki element; ulazni i izlazni tip mogu se razlikovati
    // -----------------------------------------------------------------------
    static void mapPrimer(List<String> prijatelji) {
        // Imperativno — prebacivanje u velika slova
        System.out.println("Imperativno (velika slova):");
        for (String ime : prijatelji) {
            System.out.print(ime.toUpperCase() + " ");
        }
        System.out.println();

        // Funkcionalno
        System.out.println("Funkcionalno (velika slova):");
        prijatelji.stream()
                .map(String::toUpperCase)       // String → String
                .forEach(ime -> System.out.print(ime + " "));
        System.out.println();

        // map može promeniti tip — String → Integer
        System.out.print("Dužine imena: ");
        prijatelji.stream()
                .map(String::length)             // String → Integer
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Ulančavanje: filter + map
        System.out.println("Duga imena u velikim slovima (>4 slova):");
        prijatelji.stream()
                .filter(ime -> ime.length() > 4)
                .map(String::toUpperCase)
                .forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // collect — Terminal operacija
    // Skuplja rezultate u kolekciju ili drugu strukturu
    // -----------------------------------------------------------------------
    static void collectPrimer(List<String> prijatelji) {
        // Skupljanje u novu listu
        List<String> velikaSlova = prijatelji.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println("Nova lista (velika slova): " + velikaSlova);

        // Original je nepromenjen!
        System.out.println("Originalna lista: " + prijatelji);

        // Skupljanje samo filtriranih — kao String spojen zarezom
        String spojena = prijatelji.stream()
                .filter(s -> s.startsWith("N"))
                .collect(Collectors.joining(", "));
        System.out.println("Imena koja počinju sa N: " + spojena);
    }

    // -----------------------------------------------------------------------
    // Agregacione terminalne operacije
    // sum, count, min, max, average
    // -----------------------------------------------------------------------
    static void agregacije(List<Integer> cene) {
        // count
        long broj = cene.stream().filter(c -> c > 15).count();
        System.out.println("Broj cena > 15: " + broj);

        // sum — potreban mapToInt/mapToDouble za primitive stream
        int suma = cene.stream().mapToInt(Integer::intValue).sum();
        System.out.println("Suma svih cena: " + suma);

        // max i min — vraćaju Optional jer lista može biti prazna!
        cene.stream().mapToInt(Integer::intValue).max()
                .ifPresent(m -> System.out.println("Maksimalna cena: " + m));

        cene.stream().mapToInt(Integer::intValue).min()
                .ifPresent(m -> System.out.println("Minimalna cena: " + m));

        // average — vraća OptionalDouble
        cene.stream().mapToInt(Integer::intValue).average()
                .ifPresent(avg -> System.out.printf("Prosečna cena: %.2f%n", avg));
    }

    // -----------------------------------------------------------------------
    // Složen pipeline — kombinovanje više operacija
    // -----------------------------------------------------------------------
    static void slozeniPipeline(List<String> prijatelji, List<Integer> cene) {
        // Primer iz knjige: ukupna cena sa popustom za cene > 20
        double ukupno = cene.stream()
                .filter(c -> c > 20)          // Predicate
                .mapToDouble(c -> c * 0.9)    // Function (+ konverzija u DoubleStream)
                .sum();
        System.out.printf("Ukupno (cene>20, popust 10%%): %.2f%n", ukupno);

        // Poređenje sa Streamom koji "priča priču" — čita se kao zahtev
        // "uzmi prijatelje, filtriraj one sa imenima od tačno 4 slova,
        //  pretvori u velika slova, sortiraj, i ispiši"
        System.out.println("\nPrijatelji sa imenom od 4 slova (sortirano, velika slova):");
        prijatelji.stream()
                .filter(ime -> ime.length() == 4)
                .map(String::toUpperCase)
                .sorted()
                .forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // Kreiranje Stream-a na razne načine
    // -----------------------------------------------------------------------
    static void kreiranjeStream() {
        // Iz kolekcije
        List<String> lista = List.of("a", "b", "c");
        lista.stream();

        // Direktno — Stream.of
        Stream<String> direktno = Stream.of("x", "y", "z");
        System.out.print("Stream.of: ");
        direktno.forEach(s -> System.out.print(s + " "));
        System.out.println();

        // Iz niza
        int[] niz = {1, 2, 3, 4, 5};
        int sumaIzNiza = Arrays.stream(niz).sum();
        System.out.println("Suma niza: " + sumaIzNiza);

        // Generisanje — beskonačan stream, ograničen sa limit()
        System.out.print("Prvih 5 kvadrata: ");
        Stream.iterate(1, n -> n + 1)
                .limit(5)
                .map(n -> n * n)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Stream.generate
        System.out.print("5 random brojeva: ");
        Stream.generate(Math::random)
                .limit(5)
                .map(d -> String.format("%.2f", d))
                .forEach(s -> System.out.print(s + " "));
        System.out.println();
    }
}
