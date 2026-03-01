package raf.edu.week2;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Nedelja 2 — Ponovno korišćenje lambda izraza
 *
 * Iz knjige (poglavlje 2): lambda izrazi mogu lako dovesti do dupliranja koda.
 * Ova klasa pokazuje:
 *   1. Problem dupliranja
 *   2. Čuvanje lambda izraza u promenljivoj
 *   3. Higher-order funkcije — funkcije koje vraćaju funkcije
 *   4. Leksički opseg (lexical scoping) i closure-i
 */
public class LambdaReuse {

    public static void main(String[] args) {
        List<String> friends = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");
        List<String> editors = Arrays.asList("Brian", "Jackie", "John", "Mike");
        List<String> comrades = Arrays.asList("Kate", "Ken", "Nick", "Paula", "Zach");

        System.out.println("=== 1. Problem: dupliranje lambda izraza ===\n");
        duplicationProblem(friends, editors, comrades);

        System.out.println("\n=== 2. Rešenje: čuvanje u promenljivoj ===\n");
        storeInVariable(friends, editors, comrades);

        System.out.println("\n=== 3. Novi problem: dupliranje sa različitim slovima ===\n");
        newDuplicationProblem(friends);

        System.out.println("\n=== 4. Rešenje: Higher-Order funkcija (statička metoda) ===\n");
        higherOrderFunction(friends);

        System.out.println("\n=== 5. Rešenje: Function<String, Predicate<String>> ===\n");
        functionReturningPredicate(friends);

        System.out.println("\n=== 6. Closure — zatvara promenljive iz spoljnog opsega ===\n");
        closureDemo();

        System.out.println("\n=== 7. Praktičan primer — reusable pipeline komponente ===\n");
        reusablePipeline(friends, editors, comrades);
    }

    // -----------------------------------------------------------------------
    // 1. Dupliranje — isti lambda izraz na više mesta
    // -----------------------------------------------------------------------
    static void duplicationProblem(List<String> friends, List<String> editors, List<String> comrades) {
        // LOŠE: lambda "name -> name.startsWith("N")" ponovljen 3 puta!
        // Ako treba da promenimo logiku, moramo na 3 mesta.
        long countFriends = friends.stream()
                .filter(name -> name.startsWith("N")).count();
        long countEditors = editors.stream()
                .filter(name -> name.startsWith("N")).count();
        long countComrades = comrades.stream()
                .filter(name -> name.startsWith("N")).count();

        System.out.println("Friends sa N: " + countFriends);
        System.out.println("Editors sa N: " + countEditors);
        System.out.println("Comrades sa N: " + countComrades);
        System.out.println("\n⚠ Problem: lambda izraz se ponavlja 3 puta!");
    }

    // -----------------------------------------------------------------------
    // 2. Čuvanje lambda izraza u Predicate promenljivoj
    // -----------------------------------------------------------------------
    static void storeInVariable(List<String> friends, List<String> editors, List<String> comrades) {
        // DOBRO: lambda izraz sačuvan u promenljivoj
        final Predicate<String> startsWithN = name -> name.startsWith("N");

        long countFriends = friends.stream().filter(startsWithN).count();
        long countEditors = editors.stream().filter(startsWithN).count();
        long countComrades = comrades.stream().filter(startsWithN).count();

        System.out.println("Friends sa N: " + countFriends);
        System.out.println("Editors sa N: " + countEditors);
        System.out.println("Comrades sa N: " + countComrades);
        System.out.println("\n✓ Lambda izraz definisan jednom, korišćen 3 puta.");
    }

    // -----------------------------------------------------------------------
    // 3. Novi problem — slovo se razlikuje
    // -----------------------------------------------------------------------
    static void newDuplicationProblem(List<String> friends) {
        // Šta ako treba da filtriramo po razlicitim slovima?
        // Opet dupliranje!
        final Predicate<String> startsWithN = name -> name.startsWith("N");
        final Predicate<String> startsWithB = name -> name.startsWith("B");
        final Predicate<String> startsWithS = name -> name.startsWith("S");

        System.out.println("Sa N: " + friends.stream().filter(startsWithN).count());
        System.out.println("Sa B: " + friends.stream().filter(startsWithB).count());
        System.out.println("Sa S: " + friends.stream().filter(startsWithS).count());
        System.out.println("\n⚠ Opet dupliranje — razlikuje se samo slovo!");
    }

    // -----------------------------------------------------------------------
    // 4. Higher-Order funkcija — metoda koja VRAĆA Predicate
    // -----------------------------------------------------------------------

    // Ova metoda prima String i VRAĆA Predicate<String>
    // To je "funkcija koja vraća funkciju" — higher-order function
    public static Predicate<String> checkIfStartsWith(final String letter) {
        return name -> name.startsWith(letter);
        // 'letter' se "zatvara" (closure) — pamti se vrednost
    }

    static void higherOrderFunction(List<String> friends) {
        // Sada nema dupliranja — razlikuje se samo argument
        System.out.println("Sa N: " + friends.stream().filter(checkIfStartsWith("N")).count());
        System.out.println("Sa B: " + friends.stream().filter(checkIfStartsWith("B")).count());
        System.out.println("Sa S: " + friends.stream().filter(checkIfStartsWith("S")).count());
        System.out.println("\n✓ checkIfStartsWith(\"X\") vraća Predicate za dato slovo.");
    }

    // -----------------------------------------------------------------------
    // 5. Ista stvar bez statičke metode — Function<String, Predicate<String>>
    // -----------------------------------------------------------------------
    static void functionReturningPredicate(List<String> friends) {
        // Verbose verzija — radi jasnoće
        final Function<String, Predicate<String>> startsWithLetterVerbose =
                (String letter) -> {
                    Predicate<String> checkStarts = (String name) -> name.startsWith(letter);
                    return checkStarts;
                };

        // Koncizna verzija — ista logika
        final Function<String, Predicate<String>> startsWithLetter =
                letter -> name -> name.startsWith(letter);
        //          ^^^^^    ^^^^
        //        ulaz u     ulaz u Predicate
        //       Function

        System.out.println("Sa N: " + friends.stream().filter(startsWithLetter.apply("N")).count());
        System.out.println("Sa B: " + friends.stream().filter(startsWithLetter.apply("B")).count());
        System.out.println("Sa S: " + friends.stream().filter(startsWithLetter.apply("S")).count());
        System.out.println("\n✓ Function<String, Predicate<String>> — bez statičke metode.");
        System.out.println("  Čita se: \"za dato slovo, vrati predikat koji proverava da li ime počinje tim slovom\"");
    }

    // -----------------------------------------------------------------------
    // 6. Closure demo — promenljive iz spoljnog opsega
    // -----------------------------------------------------------------------
    static void closureDemo() {
        String prefix = "Pozdrav, ";      // promenljiva iz metode
        int minDuzina = 4;                // efektivno final

        // Ove lambde koriste promenljive iz spoljnog opsega — to su CLOSURE-i
        Consumer<String> pozdravi = name -> System.out.println("  " + prefix + name);
        Predicate<String> dovoljnoDugo = name -> name.length() >= minDuzina;

        List<String> imena = List.of("Ana", "Bojan", "Čedomir", "Ivo");
        imena.stream()
                .filter(dovoljnoDugo) // 'minDuzina' iz spoljnog opsega
                .forEach(pozdravi);   // 'prefix' iz spoljnog opsega

        // PRAVILO: promenljive moraju biti final ili efektivno final
        // Sledeće NE KOMPAJLIRA:
        // int brojac = 0;
        // imena.forEach(name -> brojac++); // GREŠKA! brojac se menja
        System.out.println("\n  Pravilo: uhvaćene promenljive moraju biti (efektivno) final.");
        System.out.println("  int brojac = 0;");
        System.out.println("  imena.forEach(name -> brojac++); // NE KOMPAJLIRA!");
    }

    // -----------------------------------------------------------------------
    // 7. Praktičan primer — višestruko korišćenje pipeline komponenti
    // -----------------------------------------------------------------------
    static void reusablePipeline(List<String> friends, List<String> editors, List<String> comrades) {
        // Reusable komponente
        Predicate<String> duzeOd4 = name -> name.length() > 4;
        Function<String, String> uVelikaSlova = String::toUpperCase;
        Consumer<String> stampaj = name -> System.out.print(name + " ");

        System.out.println("Friends (duga imena, velika slova):");
        System.out.print("  ");
        friends.stream().filter(duzeOd4).map(uVelikaSlova).forEach(stampaj);
        System.out.println();

        System.out.println("Editors (duga imena, velika slova):");
        System.out.print("  ");
        editors.stream().filter(duzeOd4).map(uVelikaSlova).forEach(stampaj);
        System.out.println();

        System.out.println("Comrades (duga imena, velika slova):");
        System.out.print("  ");
        comrades.stream().filter(duzeOd4).map(uVelikaSlova).forEach(stampaj);
        System.out.println();
    }
}
