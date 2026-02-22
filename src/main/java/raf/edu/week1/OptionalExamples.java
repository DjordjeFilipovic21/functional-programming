package raf.edu.week1;

import java.util.List;
import java.util.Optional;

/**
 * Nedelja 1 — Optional<T>
 *
 * Optional je kontejner koji može, ali ne mora, sadržati vrednost.
 * Koristi se kao alternativa null vrednostima.
 *
 * Zašto Optional?
 *   - Eksplicitno označava da vrednost možda ne postoji
 *   - Sprečava NullPointerException
 *   - Forsira programera da razmisli o slučaju kada vrednosti nema
 *   - Omogućava funkcionalni stil rada sa potencijalno null vrednostima
 */
public class OptionalExamples {

    record Student(String ime, String email) {}

    static List<Student> baza = List.of(
            new Student("Ana", "ana@uni.rs"),
            new Student("Bojan", null),         // nema email
            new Student("Čedomir", "cedomir@uni.rs")
    );

    public static void main(String[] args) {
        System.out.println("=== Kreiranje Optional-a ===\n");
        kreiranjeOptional();

        System.out.println("\n=== Ekstrakcija vrednosti ===\n");
        ekstrakcija();

        System.out.println("\n=== Funkcionalne operacije ===\n");
        funkcionalneOperacije();

        System.out.println("\n=== Praktičan primer: pronalaženje studenta ===\n");
        prakticanPrimer();

        System.out.println("\n=== Optional u Streamu ===\n");
        optionalUStream();
    }

    // -----------------------------------------------------------------------
    // Kreiranje Optional-a
    // -----------------------------------------------------------------------
    static void kreiranjeOptional() {
        // Optional sa vrednosću
        Optional<String> saVrednoscu = Optional.of("Zdravo");
        System.out.println("Optional.of: " + saVrednoscu);

        // Optional bez vrednosti
        Optional<String> prazan = Optional.empty();
        System.out.println("Optional.empty: " + prazan);

        // Optional koji može biti null — koristiti ofNullable
        String mozeBitiNull = null;
        Optional<String> mozda = Optional.ofNullable(mozeBitiNull);
        System.out.println("Optional.ofNullable(null): " + mozda);

        Optional<String> nijeNull = Optional.ofNullable("vrednost");
        System.out.println("Optional.ofNullable(\"vrednost\"): " + nijeNull);

        // Optional.of sa null — baca NullPointerException!
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("Optional.of(null) baca NullPointerException!");
        }
    }

    // -----------------------------------------------------------------------
    // Ekstrakcija vrednosti
    // -----------------------------------------------------------------------
    static void ekstrakcija() {
        Optional<String> opt = Optional.of("Java");
        Optional<String> prazan = Optional.empty();

        // isPresent / isEmpty
        System.out.println("opt.isPresent(): " + opt.isPresent());
        System.out.println("prazan.isEmpty(): " + prazan.isEmpty());

        // get() — opasno! Baca NoSuchElementException ako je prazan
        System.out.println("opt.get(): " + opt.get());

        // orElse — vraća default vrednost ako je prazan
        System.out.println("prazan.orElse(\"default\"): " + prazan.orElse("default"));

        // orElseGet — lenjo, prima Supplier (izvršava se samo ako je prazan)
        System.out.println("prazan.orElseGet(...): " + prazan.orElseGet(() -> "skupo izračunata vrednost"));

        // orElseThrow — baca izuzetak ako je prazan
        try {
            prazan.orElseThrow(() -> new IllegalStateException("Vrednost ne postoji!"));
        } catch (IllegalStateException e) {
            System.out.println("orElseThrow: " + e.getMessage());
        }

        // ifPresent — Consumer, izvršava se samo ako vrednost postoji
        opt.ifPresent(v -> System.out.println("ifPresent: " + v));
        prazan.ifPresent(v -> System.out.println("Ovo se neće odštampati"));
    }

    // -----------------------------------------------------------------------
    // Funkcionalne operacije na Optional-u
    // -----------------------------------------------------------------------
    static void funkcionalneOperacije() {
        Optional<String> opt = Optional.of("  Java Programiranje  ");

        // map — transformiše vrednost ako postoji
        Optional<String> ocisceno = opt.map(String::trim);
        System.out.println("map(trim): " + ocisceno);

        Optional<Integer> duzina = opt.map(String::trim).map(String::length);
        System.out.println("map(trim).map(length): " + duzina);

        // filter — zadržava vrednost samo ako zadovoljava uslov
        Optional<String> dugacko = opt.map(String::trim).filter(s -> s.length() > 5);
        Optional<String> kratko  = opt.map(String::trim).filter(s -> s.length() < 3);
        System.out.println("filter(duzina > 5): " + dugacko);
        System.out.println("filter(duzina < 3): " + kratko);  // Optional.empty

        // Ulančavanje — elegantno bez null provera
        String rezultat = Optional.of("  funkcionalno  ")
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .orElse("prazno");
        System.out.println("Ulančavanje: " + rezultat);
    }

    // -----------------------------------------------------------------------
    // Praktičan primer — pronalaženje studenta bez NullPointerException
    // -----------------------------------------------------------------------
    static void prakticanPrimer() {
        // Metoda koja može da ne vrati rezultat
        Optional<Student> student1 = pronadiStudenta("Ana");
        Optional<Student> student2 = pronadiStudenta("Nepostojeci");

        // Bez Optional — opasan kod:
        // Student s = pronadiStudenta("Bojan"); // šta ako nema studenta?
        // System.out.println(s.email()); // NullPointerException!

        // Sa Optional — bezbedno:
        student1.ifPresent(s -> System.out.println("Pronađen: " + s.ime()));
        student2.ifPresent(s -> System.out.println("Pronađen: " + s.ime())); // ne štampa ništa

        // Dobijanje emaila — može biti null!
        String email1 = pronadiStudenta("Ana")
                .map(Student::email)
                .orElse("(nema emaila)");

        String email2 = pronadiStudenta("Bojan")
                .map(Student::email)
                .orElse("(nema emaila)");  // email Bojana je null, orElse daje default

        String email3 = pronadiStudenta("Nepostojeci")
                .map(Student::email)
                .orElse("(student ne postoji)");

        System.out.println("Email Ane: " + email1);
        System.out.println("Email Bojana: " + email2);
        System.out.println("Email nepostojećeg: " + email3);
    }

    static Optional<Student> pronadiStudenta(String ime) {
        return baza.stream()
                .filter(s -> s.ime().equals(ime))
                .findFirst(); // vraća Optional<Student>
    }

    // -----------------------------------------------------------------------
    // Optional u kombinaciji sa Stream-om
    // -----------------------------------------------------------------------
    static void optionalUStream() {
        List<String> reci = List.of("hello", "world", "java", "stream");

        // findFirst — vraća Optional
        Optional<String> prvaKratka = reci.stream()
                .filter(s -> s.length() == 4)
                .findFirst();

        System.out.println("Prva reč dužine 4: " + prvaKratka.orElse("nema"));

        // Kreiranje Optional iz Stream operacije
        Optional<Integer> maxDuzina = reci.stream()
                .map(String::length)
                .max(Integer::compareTo);

        maxDuzina.ifPresent(max -> System.out.println("Maksimalna dužina: " + max));

        // Stream iz Optional (Java 9+) — korisno za flatMap
        long brojDugackih = reci.stream()
                .map(s -> s.length() > 4 ? Optional.of(s) : Optional.<String>empty())
                .flatMap(Optional::stream) // Optional.stream() vraća Stream od 0 ili 1 elementa
                .count();
        System.out.println("Broj reči dužih od 4: " + brojDugackih);
    }
}
