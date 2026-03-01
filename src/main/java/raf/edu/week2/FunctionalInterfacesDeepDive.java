package raf.edu.week2;

import java.util.List;
import java.util.function.*;

/**
 * Nedelja 2 — Funkcionalni interfejsi: detaljniji pregled
 *
 * Cilj: razumeti potpise Predicate, Consumer, Function, Supplier
 * i kako se oni koriste kao tipovi argumenata Stream operacija.
 *
 * Ključna ideja: kada pišemo lambda izraz za filter(), map(), forEach(),
 * mi zapravo implementiramo apstraktnu metodu odgovarajućeg funkcionalnog interfejsa.
 */
public class FunctionalInterfacesDeepDive {

    record Student(String ime, int poeni, String grad) {}

    public static void main(String[] args) {
        List<Student> studenti = List.of(
                new Student("Ana", 85, "Beograd"),
                new Student("Bojan", 45, "Niš"),
                new Student("Čedomir", 92, "Beograd"),
                new Student("Dragana", 67, "Novi Sad"),
                new Student("Elena", 55, "Niš"),
                new Student("Filip", 78, "Beograd")
        );

        System.out.println("=== Predicate<T> — test(T) : boolean ===\n");
        predicateDeepDive(studenti);

        System.out.println("\n=== Consumer<T> — accept(T) : void ===\n");
        consumerDeepDive(studenti);

        System.out.println("\n=== Function<T, R> — apply(T) : R ===\n");
        functionDeepDive(studenti);

        System.out.println("\n=== Supplier<T> — get() : T ===\n");
        supplierDeepDive();

        System.out.println("\n=== Veza sa Stream operacijama ===\n");
        streamConnectionDemo(studenti);
    }

    // -----------------------------------------------------------------------
    // Predicate<T> — koristi se u filter(), anyMatch(), allMatch(), noneMatch()
    // -----------------------------------------------------------------------
    static void predicateDeepDive(List<Student> studenti) {
        // Definišemo predikat — test za prolaz
        Predicate<Student> jePolozio = s -> s.poeni() >= 50;
        Predicate<Student> jeIzBeograda = s -> s.grad().equals("Beograd");

        // Kompozicija predikata — and, or, negate
        Predicate<Student> polozioIzBeograda = jePolozio.and(jeIzBeograda);
        Predicate<Student> nijePolozio = jePolozio.negate();
        Predicate<Student> polozioIliIzBeograda = jePolozio.or(jeIzBeograda);

        System.out.println("Studenti koji su položili:");
        studenti.stream()
                .filter(jePolozio) // filter prima Predicate<Student>
                .forEach(s -> System.out.println("  " + s.ime() + " (" + s.poeni() + ")"));

        System.out.println("\nStudenti koji su položili I iz Beograda:");
        studenti.stream()
                .filter(polozioIzBeograda)
                .forEach(s -> System.out.println("  " + s.ime()));

        System.out.println("\nStudenti koji NISU položili:");
        studenti.stream()
                .filter(nijePolozio)
                .forEach(s -> System.out.println("  " + s.ime() + " (" + s.poeni() + ")"));

        // anyMatch, allMatch, noneMatch — svi primaju Predicate
        boolean imaPolozenih = studenti.stream().anyMatch(jePolozio);
        boolean sviPolozili = studenti.stream().allMatch(jePolozio);
        boolean nikoIspodNule = studenti.stream().noneMatch(s -> s.poeni() < 0);

        System.out.println("\nIma položenih? " + imaPolozenih);
        System.out.println("Svi položili? " + sviPolozili);
        System.out.println("Niko ispod nule? " + nikoIspodNule);
    }

    // -----------------------------------------------------------------------
    // Consumer<T> — koristi se u forEach()
    // -----------------------------------------------------------------------
    static void consumerDeepDive(List<Student> studenti) {
        // Definišemo Consumer-e
        Consumer<Student> stampajIme = s -> System.out.println("  Ime: " + s.ime());
        Consumer<Student> stampajPoene = s -> System.out.println("  Poeni: " + s.poeni());
        Consumer<Student> stampajGrad = s -> System.out.println("  Grad: " + s.grad());

        // andThen — ulančavanje Consumer-a
        Consumer<Student> stampajSve = stampajIme.andThen(stampajPoene).andThen(stampajGrad);

        System.out.println("Detalji prvog studenta:");
        stampajSve.accept(studenti.get(0));

        // Praktičan primer — formatiran ispis svih studenata
        Consumer<Student> formatiraniIspis = s ->
                System.out.printf("  %-10s | %3d poena | %s%n", s.ime(), s.poeni(), s.grad());

        System.out.println("\nSvi studenti (formatiran ispis):");
        studenti.forEach(formatiraniIspis); // forEach prima Consumer

        // BiConsumer — prima dva argumenta
        BiConsumer<String, Integer> stampajParKV = (kljuc, vrednost) ->
                System.out.println("  " + kljuc + " = " + vrednost);

        System.out.println("\nBiConsumer primer:");
        stampajParKV.accept("poeni", 85);
    }

    // -----------------------------------------------------------------------
    // Function<T, R> — koristi se u map()
    // -----------------------------------------------------------------------
    static void functionDeepDive(List<Student> studenti) {
        // Function koji izvlači ime
        Function<Student, String> uIme = Student::ime;
        // Function koji računa ocenu
        Function<Student, String> uOcenu = s -> {
            if (s.poeni() >= 90) return "A";
            else if (s.poeni() >= 80) return "B";
            else if (s.poeni() >= 70) return "C";
            else if (s.poeni() >= 60) return "D";
            else if (s.poeni() >= 50) return "E";
            else return "F";
        };

        // map prima Function
        System.out.println("Imena studenata:");
        studenti.stream()
                .map(uIme) // Student → String
                .forEach(ime -> System.out.println("  " + ime));

        System.out.println("\nOcene studenata:");
        studenti.stream()
                .map(s -> s.ime() + " → " + uOcenu.apply(s))
                .forEach(s -> System.out.println("  " + s));

        // andThen — kompozicija funkcija: f pa g
        Function<Student, String> imeVelikim = uIme.andThen(String::toUpperCase);

        System.out.println("\nImena velikim slovima (andThen):");
        studenti.stream()
                .map(imeVelikim) // Student → String → STRING
                .forEach(s -> System.out.println("  " + s));

        // Function.identity() — vraća isti objekat
        Function<String, String> identitet = Function.identity();
        System.out.println("\nIdentitet: " + identitet.apply("test")); // "test"
    }

    // -----------------------------------------------------------------------
    // Supplier<T> — nema ulaz, proizvodi vrednost
    // -----------------------------------------------------------------------
    static void supplierDeepDive() {
        // Supplier za lenjo kreiranje objekata
        Supplier<Student> podrazumevaniStudent =
                () -> new Student("N/A", 0, "N/A");

        Supplier<List<Student>> praznaLista = java.util.ArrayList::new;

        // Koristi se sa Optional.orElseGet — lenjo izračunava
        java.util.Optional<Student> prazan = java.util.Optional.empty();
        Student rezultat = prazan.orElseGet(podrazumevaniStudent);
        System.out.println("orElseGet: " + rezultat.ime() + " (" + rezultat.poeni() + ")");

        // orElse vs orElseGet — razlika
        // orElse(vrednost)     — UVEK izračunava vrednost, čak i kad Optional ima vrednost
        // orElseGet(supplier)  — izračunava SAMO kad je Optional prazan (lenjo)
        System.out.println("\norElse vs orElseGet:");
        java.util.Optional<String> opt = java.util.Optional.of("postoji");

        // Ovo se poziva UVEK (čak i kad opt ima vrednost):
        String r1 = opt.orElse(skupoIzracunavanje("orElse"));
        // Ovo se NE poziva jer opt ima vrednost:
        String r2 = opt.orElseGet(() -> skupoIzracunavanje("orElseGet"));
        System.out.println("Rezultat: " + r1 + ", " + r2);

        // Stream.generate — Supplier za beskonačan stream
        System.out.println("\nStream.generate sa Supplier-om (5 random brojeva):");
        java.util.stream.Stream.generate(Math::random) // Supplier<Double>
                .limit(5)
                .map(d -> String.format("%.2f", d))
                .forEach(s -> System.out.print("  " + s));
        System.out.println();
    }

    static String skupoIzracunavanje(String izvor) {
        System.out.println("  [Pozvan skupoIzracunavanje iz: " + izvor + "]");
        return "skupo";
    }

    // -----------------------------------------------------------------------
    // Demo: eksplicitna veza između Stream operacija i funkcionalnih interfejsa
    // -----------------------------------------------------------------------
    static void streamConnectionDemo(List<Student> studenti) {
        // Eksplicitno kreiramo svaki funkcionalni interfejs
        Predicate<Student> uslov = s -> s.poeni() >= 70;
        Function<Student, String> transformacija = s -> s.ime() + " (" + s.grad() + ")";
        Consumer<String> akcija = s -> System.out.println("  " + s);

        System.out.println("Pipeline: filter(Predicate) → map(Function) → forEach(Consumer)");
        studenti.stream()
                .filter(uslov)          // Predicate<Student>
                .map(transformacija)    // Function<Student, String>
                .forEach(akcija);       // Consumer<String>

        // Ista stvar inline:
        System.out.println("\nIsta stvar, inline:");
        studenti.stream()
                .filter(s -> s.poeni() >= 70)
                .map(s -> s.ime() + " (" + s.grad() + ")")
                .forEach(s -> System.out.println("  " + s));
    }
}
