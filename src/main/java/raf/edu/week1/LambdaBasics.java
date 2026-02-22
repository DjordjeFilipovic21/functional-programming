package raf.edu.week1;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/**
 * Nedelja 1 — Lambda izrazi i funkcionalni interfejsi
 *
 * Teme:
 * - Sintaksa lambda izraza
 * - @FunctionalInterface
 * - Ugrađeni interfejsi: Predicate, Function, Consumer, Supplier
 * - Comparator kao primer (podsećanje)
 */
public class LambdaBasics {

    // Vlastiti funkcionalni interfejs — tačno jedna apstraktna metoda
    @FunctionalInterface
    interface Transformer<T, R> {
        R transform(T input);
    }

    public static void main(String[] args) {
        System.out.println("=== Sintaksa lambda izraza ===\n");
        lambdaSyntax();

        System.out.println("\n=== Predicate<T> ===\n");
        predicateExamples();

        System.out.println("\n=== Function<T, R> ===\n");
        functionExamples();

        System.out.println("\n=== Consumer<T> ===\n");
        consumerExamples();

        System.out.println("\n=== Supplier<T> ===\n");
        supplierExamples();

        System.out.println("\n=== Comparator (podsećanje) ===\n");
        comparatorExamples();

        System.out.println("\n=== Vlastiti funkcionalni interfejs ===\n");
        customFunctionalInterface();
    }

    // -----------------------------------------------------------------------
    // Sintaksa lambda izraza
    // -----------------------------------------------------------------------
    static void lambdaSyntax() {
        // Jedan parametar, jedan izraz (nema return, nema vitičastih zagrada)
        Function<Integer, Integer> dvostruko = x -> x * 2;
        System.out.println("Dvostruko od 5: " + dvostruko.apply(5));

        // Više parametara
        BiFunction<Integer, Integer, Integer> zbir = (a, b) -> a + b;
        System.out.println("Zbir 3 i 4: " + zbir.apply(3, 4));
        // postoje i drugi paketi koji dodaju TriFunction i slično, a mozemo ih i sami definisati

        // Bez parametara
        Supplier<String> pozdrav = () -> "Zdravo, Paradigme!";
        System.out.println(pozdrav.get());

        // Blok naredbi — potreban return
        Function<Integer, String> ocena = (points) -> {
            if (points >= 90) return "A";
            else if (points >= 80) return "B";
            else if (points >= 70) return "C";
            else return "F";
        };
        System.out.println("Ocena za 85 poena: " + ocena.apply(85));
    }

    // -----------------------------------------------------------------------
    // Predicate<T> — test(T t) : boolean
    // Koristi se u: filter(), removeIf(), anyMatch(), allMatch(), noneMatch()
    // -----------------------------------------------------------------------
    static void predicateExamples() {
        Predicate<Integer> jeParno = n -> n % 2 == 0;
        Predicate<String> jeKratko = s -> s.length() < 5;
        Predicate<Integer> jePositivno = n -> n > 0;

        System.out.println("4 je parno: " + jeParno.test(4));
        System.out.println("7 je parno: " + jeParno.test(7));
        System.out.println("\"Ana\" je kratko: " + jeKratko.test("Ana"));

        // Predicate se može kombinovati
        Predicate<Integer> jeParnoPozitivno = jeParno.and(jePositivno);
        Predicate<Integer> jeParnoIliNegativno = jeParno.or(jePositivno.negate());

        System.out.println("4 je parno i pozitivno: " + jeParnoPozitivno.test(4));
        System.out.println("-2 je parno i pozitivno: " + jeParnoPozitivno.test(-2));

        // Primer sa filter na Stream-u
        List<Integer> brojevi = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        System.out.print("Parni brojevi: ");
        brojevi.stream().filter(jeParno).forEach(n -> System.out.print(n + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Function<T, R> — apply(T t) : R
    // Koristi se u: map(), flatMap()
    // -----------------------------------------------------------------------
    static void functionExamples() {
        Function<String, Integer> duzina = String::length; // method reference
        Function<Integer, Integer> kvadrat = n -> n * n;

        System.out.println("Dužina \"Beograd\": " + duzina.apply("Beograd"));
        System.out.println("Kvadrat od 7: " + kvadrat.apply(7));

        // andThen — kompozicija funkcija: f pa g
        Function<String, Integer> duzinaNaKvadrat = duzina.andThen(kvadrat);
        System.out.println("Dužina^2 \"Java\": " + duzinaNaKvadrat.apply("Java")); // 4^2 = 16

        // Primer sa map na Stream-u
        List<String> gradovi = List.of("Beograd", "Niš", "Novi Sad");
        System.out.print("Dužine naziva gradova: ");
        gradovi.stream().map(duzina).forEach(n -> System.out.print(n + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Consumer<T> — accept(T t) : void
    // Koristi se u: forEach()
    // -----------------------------------------------------------------------
    static void consumerExamples() {
        Consumer<String> stampaj = s -> System.out.println("  > " + s);
        Consumer<String> stampajVeliko = s -> System.out.println("  > " + s.toUpperCase());

        // andThen — izvršava oba Consumer-a sekvencijalno
        Consumer<String> oba = stampaj.andThen(stampajVeliko);

        List<String> jezici = List.of("Java", "Haskell", "Scala");
        System.out.println("Jezici (mali i veliki):");
        jezici.forEach(oba);
    }

    // -----------------------------------------------------------------------
    // Supplier<T> — get() : T
    // Koristi se za lenjo kreiranje objekata, Optional.orElseGet()
    // -----------------------------------------------------------------------
    static void supplierExamples() {
        Supplier<Double> randomBroj = Math::random;
        Supplier<List<String>> praznaLista = java.util.ArrayList::new;

        System.out.println("Random broj: " + randomBroj.get());
        System.out.println("Prazna lista: " + praznaLista.get());

        // Praktičan primer — lenjo računanje skupog resursa
        Supplier<String> skupoIzracunavanje = () -> {
            // Simulacija skupog poziva (npr. baza podataka, web servis)
            return "Rezultat iz baze podataka";
        };

        // Izvršava se tek kada zatreba, ne na mestu deklaracije
        System.out.println("Vrednost: " + skupoIzracunavanje.get());
    }

    // -----------------------------------------------------------------------
    // Comparator — klasičan primer funkcionalnog interfejsa
    // -----------------------------------------------------------------------
    static void comparatorExamples() {
        List<String> gradovi = List.of("Beograd", "Niš", "Novi Sad", "Kragujevac");

        // Pre Java 8 — anonimna klasa
        Comparator<String> poDuziniStaro = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        };

        // Java 8+ — lambda izraz
        Comparator<String> poDuzini = (a, b) -> a.length() - b.length();

        // Još bolje — Comparator.comparing
        Comparator<String> poDuziniModerno = Comparator.comparingInt(String::length);

        System.out.print("Sortirano po dužini: ");
        gradovi.stream()
                .sorted(poDuziniModerno)
                .forEach(g -> System.out.print(g + " "));
        System.out.println();

        // Obrnuto sortiranje
        System.out.print("Sortirano po dužini (obrnuto): ");
        gradovi.stream()
                .sorted(poDuziniModerno.reversed())
                .forEach(g -> System.out.print(g + " "));
        System.out.println();

        // Kombinovano sortiranje — po dužini, pa alfabetski
        Comparator<String> kombinirano = Comparator.comparingInt(String::length)
                .thenComparing(Comparator.naturalOrder());
        System.out.print("Kombinirano sortiranje: ");
        gradovi.stream().sorted(kombinirano).forEach(g -> System.out.print(g + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Vlastiti funkcionalni interfejs
    // -----------------------------------------------------------------------
    static void customFunctionalInterface() {
        // Transformer je naš @FunctionalInterface
        Transformer<String, Integer> duzina = s -> s.length();
        Transformer<Integer, String> u_zvezdice = n -> "*".repeat(n);

        String rec = "Lambda";
        System.out.println("Reč: " + rec);
        System.out.println("Dužina: " + duzina.transform(rec));
        System.out.println("Zvezdice: " + u_zvezdice.transform(duzina.transform(rec)));
    }
}