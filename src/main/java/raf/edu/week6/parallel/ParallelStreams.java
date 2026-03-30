package raf.edu.week6.parallel;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Nedelja 6 — Parallel Streams
 *
 * Demonstrira:
 *   1. parallelStream() vs stream() — osnove
 *   2. Benchmark: kada se paralelizam isplati
 *   3. Opasnosti: shared mutable state
 *   4. Redosled: forEach vs forEachOrdered
 *   5. reduce sa combiner-om — zašto postoji treći argument
 *   6. Custom thread pool za paralelne streamove
 */
public class ParallelStreams {

    public static void main(String[] args) {
        System.out.println("=== 1. Osnove: stream() vs parallelStream() ===\n");
        osnove();

        System.out.println("\n=== 2. Benchmark: paralelno vs sekvencijalno ===\n");
        benchmark();

        System.out.println("\n=== 3. Opasnost: shared mutable state ===\n");
        sharedStateProblem();

        System.out.println("\n=== 4. Redosled: forEach vs forEachOrdered ===\n");
        redosled();

        System.out.println("\n=== 5. reduce sa combiner-om ===\n");
        reduceSaCombinerom();

        System.out.println("\n=== 6. Custom thread pool ===\n");
        customThreadPool();
    }

    // =========================================================================
    // 1. Osnove
    // =========================================================================
    static void osnove() {
        List<Integer> lista = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        // Sekvencijalno: svaki element na ISTOM thread-u
        System.out.println("Sekvencijalno:");
        lista.stream()
                .map(n -> {
                    System.out.printf("  map(%d) na %s%n", n, Thread.currentThread().getName());
                    return n * 2;
                })
                .forEach(n -> {});

        // Paralelno: elementi se distribuiraju na RAZNE thread-ove
        System.out.println("\nParalelno:");
        lista.parallelStream()
                .map(n -> {
                    System.out.printf("  map(%d) na %s%n", n, Thread.currentThread().getName());
                    return n * 2;
                })
                .forEach(n -> {});

        // Rezultat je ISTI — samo je izvršavanje drugačije
        List<Integer> seq = lista.stream().map(n -> n * 2).toList();
        List<Integer> par = lista.parallelStream().map(n -> n * 2).toList();
        // toList() čuva redosled i u paralelnom streamu (za razliku od forEach)
        System.out.println("\nRezultat seq: " + seq);
        System.out.println("Rezultat par: " + par);
        System.out.println("Jednaki: " + seq.equals(par));
    }

    // =========================================================================
    // 2. Benchmark
    // =========================================================================
    static void benchmark() {
        // Mala kolekcija — sekvencijalno je BRŽE (overhead thread-ova)
        List<Integer> mala = IntStream.rangeClosed(1, 100).boxed().toList();
        long t1 = timeIt(() -> mala.stream().map(n -> n * n).reduce(0, Integer::sum));
        long t2 = timeIt(() -> mala.parallelStream().map(n -> n * n).reduce(0, Integer::sum));
        System.out.printf("Mala lista (100):    seq=%dμs  par=%dμs%n", t1, t2);

        // Velika kolekcija sa "skupom" operacijom — paralelno je BRŽE
        List<Integer> velika = IntStream.rangeClosed(1, 10_000_000).boxed().toList();
        long t3 = timeIt(() -> velika.stream().map(ParallelStreams::skupaOperacija).reduce(0, Integer::sum));
        long t4 = timeIt(() -> velika.parallelStream().map(ParallelStreams::skupaOperacija).reduce(0, Integer::sum));
        System.out.printf("Velika lista (10M):  seq=%dμs  par=%dμs%n", t3, t4);
        System.out.printf("Ubrzanje: %.1fx%n", (double) t3 / Math.max(t4, 1));

        System.out.println("\nPoruka: paralelizam se isplati za VELIKE kolekcije sa SKUPIM operacijama.");
        System.out.println("Za male kolekcije ili trivijalne operacije, overhead premašuje dobit.");
    }

    static int skupaOperacija(int n) {
        // Simuliramo CPU-intenzivnu operaciju
        int result = n;
        for (int i = 0; i < 10; i++) result = (result * 31 + 7) % 1_000_000;
        return result;
    }

    static long timeIt(Runnable action) {
        long start = System.nanoTime();
        action.run();
        return (System.nanoTime() - start) / 1_000; // microseconds
    }

    // =========================================================================
    // 3. Shared mutable state — NAJČEŠĆI bug
    // =========================================================================
    static void sharedStateProblem() {
        List<Integer> lista = IntStream.rangeClosed(1, 10_000).boxed().toList();

        // POGREŠNO: ArrayList nije thread-safe!
        List<Integer> losRezultat = new ArrayList<>();
        lista.parallelStream().forEach(losRezultat::add); // race condition!
        System.out.println("ArrayList + parallel forEach: " + losRezultat.size() + " elemenata (očekivano: 10000)");
        // Moguće: manje od 10000, ArrayIndexOutOfBoundsException, ili korumpiran niz

        // ISPRAVNO: koristi collect
        List<Integer> dobarRezultat = lista.parallelStream().collect(Collectors.toList());
        System.out.println("collect(toList()): " + dobarRezultat.size() + " elemenata");

        // ISPRAVNO alternativa: Collections.synchronizedList
        List<Integer> syncLista = Collections.synchronizedList(new ArrayList<>());
        lista.parallelStream().forEach(syncLista::add);
        System.out.println("synchronizedList:  " + syncLista.size() + " elemenata");

        System.out.println("\nPravilo: NIKAD ne mutirajte deljeno stanje iz paralelnog streama.");
        System.out.println("Koristite collect() ili thread-safe kolekcije.");
    }

    // =========================================================================
    // 4. Redosled
    // =========================================================================
    static void redosled() {
        List<String> lista = List.of("A", "B", "C", "D", "E", "F");

        // forEach na paralelnom — redosled NIJE garantovan
        System.out.print("parallel forEach:        ");
        lista.parallelStream().forEach(s -> System.out.print(s + " "));
        System.out.println();

        // forEachOrdered — redosled JE garantovan (ali sporije)
        System.out.print("parallel forEachOrdered: ");
        lista.parallelStream().forEachOrdered(s -> System.out.print(s + " "));
        System.out.println();

        // collect (toList) — uvek čuva redosled
        System.out.println("parallel toList:         " + lista.parallelStream().toList());

        // findFirst vs findAny na paralelnom
        Optional<String> first = lista.parallelStream().findFirst(); // uvek "A"
        Optional<String> any   = lista.parallelStream().findAny();   // bilo koji
        System.out.println("findFirst: " + first.orElse("?") + " (determinističan)");
        System.out.println("findAny:   " + any.orElse("?") + " (nedeterminističan)");
    }

    // =========================================================================
    // 5. reduce sa combiner-om
    // =========================================================================
    static void reduceSaCombinerom() {
        List<String> reci = List.of("hello", "functional", "world", "java", "stream");

        // Sekvencijalni reduce: samo accumulator
        int sumaSeq = reci.stream()
                .reduce(0,
                        (acc, s) -> acc + s.length(),   // accumulator: int + String → int
                        Integer::sum);                   // combiner: nikad se ne poziva
        System.out.println("Sekvencijalno: suma dužina = " + sumaSeq);

        // Paralelni reduce: combiner je NEOPHODAN
        int sumaPar = reci.parallelStream()
                .reduce(0,
                        (acc, s) -> {
                            System.out.printf("  accumulator(%d, \"%s\") na %s%n",
                                    acc, s, Thread.currentThread().getName());
                            return acc + s.length();
                        },
                        (a, b) -> {
                            System.out.printf("  combiner(%d, %d) na %s%n",
                                    a, b, Thread.currentThread().getName());
                            return a + b;
                        });
        System.out.println("Paralelno: suma dužina = " + sumaPar);

        System.out.println("\nBez combiner-a, paralelni reduce ne može da spoji parcijalne rezultate.");
        System.out.println("Combiner MORA biti asocijativan i kompatibilan sa accumulator-om.");
    }

    // =========================================================================
    // 6. Custom thread pool
    // =========================================================================
    static void customThreadPool() {
        // Po default-u, parallelStream koristi ForkJoinPool.commonPool()
        System.out.println("Default pool: " + ForkJoinPool.commonPool());
        System.out.println("Paralelizam:  " + ForkJoinPool.commonPool().getParallelism());

        // Problem: svi paralelni streamovi dele ISTI pool!
        // Ako jedan pipeline blokira (I/O), drugi čekaju.

        // Rešenje: custom ForkJoinPool
        ForkJoinPool customPool = new ForkJoinPool(2); // samo 2 thread-a
        try {
            List<Integer> lista = IntStream.rangeClosed(1, 8).boxed().toList();
            List<Integer> rezultat = customPool.submit(() ->
                    lista.parallelStream()
                            .map(n -> {
                                System.out.printf("  %d na %s%n", n, Thread.currentThread().getName());
                                return n * 2;
                            })
                            .toList()
            ).get();
            System.out.println("Rezultat sa custom pool (2 thread-a): " + rezultat);
        } catch (Exception e) {
            System.out.println("Greška: " + e.getMessage());
        } finally {
            customPool.shutdown();
        }
    }
}
