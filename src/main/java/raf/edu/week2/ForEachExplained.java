package raf.edu.week2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Nedelja 2 — forEach detaljno
 *
 * forEach je terminalna operacija koja prima Consumer<T> kao argument.
 * Iterable interfejs definise forEach metodu — dostupna je na svim kolekcijama.
 * Stream interfejs takodje ima forEach — koristi se na kraju pipeline-a.
 *
 * Potpis:
 *   void forEach(Consumer<? super T> action)
 *
 * Ključna poenta: argument forEach-a je Consumer<T>, interfejs sa metodom:
 *   void accept(T t)
 */
public class ForEachExplained {

    public static void main(String[] args) {
        List<String> prijatelji = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");

        System.out.println("=== 1. Evolucija iteracije ===\n");
        evolutionOfIteration(prijatelji);

        System.out.println("\n=== 2. forEach — tip argumenta (Consumer) ===\n");
        forEachArgumentType(prijatelji);

        System.out.println("\n=== 3. forEach na kolekciji vs na Stream-u ===\n");
        forEachCollectionVsStream(prijatelji);

        System.out.println("\n=== 4. forEach sa Map-om ===\n");
        forEachWithMap();

        System.out.println("\n=== 5. Česte greške sa forEach ===\n");
        commonMistakes(prijatelji);
    }

    // -----------------------------------------------------------------------
    // 1. Evolucija: od klasične petlje do forEach
    // -----------------------------------------------------------------------
    static void evolutionOfIteration(List<String> prijatelji) {
        // KORAK 1: Klasična for petlja (Java 1.0)
        // Problem: pristup preko indeksa, greška sa granicama, verbose
        System.out.println("Korak 1 — Klasična for petlja:");
        for (int i = 0; i < prijatelji.size(); i++) {
            System.out.println("  " + prijatelji.get(i));
        }

        // KORAK 2: Enhanced for (Java 5)
        // Bolje, ali još uvek eksterna iteracija
        System.out.println("\nKorak 2 — Enhanced for:");
        for (String ime : prijatelji) {
            System.out.println("  " + ime);
        }

        // KORAK 3: forEach sa anonimnom klasom
        // Interna iteracija, ali ružna sintaksa
        System.out.println("\nKorak 3 — forEach sa anonimnom klasom:");
        prijatelji.forEach(new Consumer<String>() {
            @Override
            public void accept(String ime) {
                System.out.println("  " + ime);
            }
        });

        // KORAK 4: forEach sa lambda izrazom (Java 8)
        // Isto kao korak 3, samo kraće — kompajler generiše Consumer
        System.out.println("\nKorak 4 — forEach sa lambda izrazom:");
        prijatelji.forEach((String ime) -> System.out.println("  " + ime));

        // KORAK 5: Lambda sa inferencijom tipa
        System.out.println("\nKorak 5 — Lambda sa inferencijom tipa:");
        prijatelji.forEach(ime -> System.out.println("  " + ime));

        // KORAK 6: Method reference
        System.out.println("\nKorak 6 — Method reference:");
        prijatelji.forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // 2. Šta je zapravo argument forEach-a?
    // -----------------------------------------------------------------------
    static void forEachArgumentType(List<String> prijatelji) {
        // forEach prima Consumer<T> — interfejs sa void accept(T t)
        // Kada napišemo lambda, Java KREIRA Consumer objekat za nas

        // Eksplicitno kreiranje Consumer-a
        Consumer<String> stampaj = ime -> System.out.println("  [Consumer] " + ime);

        System.out.println("Prosleđivanje Consumer promenljive:");
        prijatelji.forEach(stampaj);

        // Consumer sa andThen — ulančavanje akcija
        Consumer<String> stampajMalo = ime -> System.out.print("  malo: " + ime);
        Consumer<String> stampajVeliko = ime -> System.out.println(" → veliko: " + ime.toUpperCase());
        Consumer<String> oba = stampajMalo.andThen(stampajVeliko);

        System.out.println("\nUlančani Consumer (andThen):");
        prijatelji.forEach(oba);

        // Praktičan primer: logovanje + obrada
        Consumer<String> loguj = ime -> System.out.print("  [LOG] Obrađujem: " + ime);
        Consumer<String> obradi = ime -> System.out.println(" ✓ Dužina: " + ime.length());
        prijatelji.stream().limit(3).forEach(loguj.andThen(obradi));
    }

    // -----------------------------------------------------------------------
    // 3. forEach na kolekciji vs forEach na Stream-u
    // -----------------------------------------------------------------------
    static void forEachCollectionVsStream(List<String> prijatelji) {
        // DIREKTNO NA KOLEKCIJI — Iterable.forEach
        // Koristi se za jednostavan prolazak
        System.out.println("Direktno na kolekciji (Iterable.forEach):");
        prijatelji.forEach(ime -> System.out.println("  " + ime));

        // NA STREAM-U — Stream.forEach
        // Koristi se na kraju pipeline-a (nakon filter, map, itd.)
        System.out.println("\nNa Stream-u (posle filter + map):");
        prijatelji.stream()
                .filter(ime -> ime.length() <= 4) // Predicate
                .map(String::toUpperCase)          // Function
                .forEach(ime -> System.out.println("  " + ime)); // Consumer

        // Bitna razlika: forEachOrdered na paralelnom stream-u
        System.out.println("\nforEach vs forEachOrdered na parallelStream:");
        System.out.print("  forEach (redosled nije garantovan): ");
        prijatelji.parallelStream().forEach(ime -> System.out.print(ime + " "));
        System.out.println();
        System.out.print("  forEachOrdered (redosled garantovan): ");
        prijatelji.parallelStream().forEachOrdered(ime -> System.out.print(ime + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // 4. forEach sa Map-om — BiConsumer
    // -----------------------------------------------------------------------
    static void forEachWithMap() {
        Map<String, Integer> ocene = Map.of(
                "Ana", 9,
                "Bojan", 7,
                "Čedomir", 10,
                "Dragana", 8
        );

        // Map.forEach prima BiConsumer<K, V>
        // BiConsumer ima: void accept(T t, U u)
        System.out.println("Map.forEach sa BiConsumer:");
        ocene.forEach((ime, ocena) -> System.out.printf("  %s: %d%n", ime, ocena));
    }

    // -----------------------------------------------------------------------
    // 5. Česte greške i anti-patterni
    // -----------------------------------------------------------------------
    static void commonMistakes(List<String> prijatelji) {
        // GREŠKA 1: Modifikacija spoljne kolekcije u forEach
        // NE RADITI OVO — može izazvati ConcurrentModificationException
        System.out.println("Anti-pattern 1: Modifikacija spoljne promenljive");
        System.out.println("  // LOŠE:");
        System.out.println("  // List<String> rezultat = new ArrayList<>();");
        System.out.println("  // imena.forEach(ime -> rezultat.add(ime.toUpperCase()));");
        System.out.println("  // DOBRO — koristiti map + collect:");

        List<String> rezultat = prijatelji.stream()
                .map(String::toUpperCase)
                .toList();
        System.out.println("  " + rezultat);

        // GREŠKA 2: forEach umesto map+collect za transformaciju
        System.out.println("\nAnti-pattern 2: forEach za transformaciju");
        System.out.println("  Koristiti map() + collect() za kreiranje nove kolekcije,");
        System.out.println("  a forEach() samo za sporedne efekte (ispis, logovanje, itd.)");

        // ISPRAVNO korišćenje forEach — za sporedne efekte
        System.out.println("\nIspravno korišćenje forEach — sporedni efekti:");
        prijatelji.stream()
                .filter(ime -> ime.startsWith("N"))
                .forEach(ime -> System.out.println("  Šaljem email za: " + ime));
    }
}
