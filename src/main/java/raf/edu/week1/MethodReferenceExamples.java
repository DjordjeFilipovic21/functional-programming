package raf.edu.week1;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Nedelja 1 — Method Reference (Reference na metodu)
 *
 * Method reference je skraćena sintaksa za lambda izraz koji samo
 * poziva već postojeću metodu. Kompajler automatski mapira parametre.
 *
 * Četiri oblika:
 *   1. Statička metoda:          Klasa::statickaMetoda
 *   2. Metoda instance (tip):    Klasa::metodaInstance
 *   3. Metoda instance (obj):    objekat::metoda
 *   4. Konstruktor:              Klasa::new
 */
public class MethodReferenceExamples {

    static class Student {
        private final String ime;
        private final int poeni;

        public Student(String ime, int poeni) {
            this.ime = ime;
            this.poeni = poeni;
        }

        public String getIme() { return ime; }
        public int getPoeni() { return poeni; }

        // Statička metoda
        public static boolean jePolozio(Student s) {
            return s.poeni >= 50;
        }

        // Metoda instance
        public String opis() {
            return ime + " (" + poeni + " poena)";
        }

        @Override
        public String toString() {
            return opis();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Statička metoda ===\n");
        statickaMetoda();

        System.out.println("\n=== 2. Metoda instance (na tipu) ===\n");
        metodaInstanceNaTipu();

        System.out.println("\n=== 3. Metoda instance (na konkretnom objektu) ===\n");
        metodaInstanceNaObjektu();

        System.out.println("\n=== 4. Konstruktor reference ===\n");
        konstruktorReference();

        System.out.println("\n=== Kada koristiti method reference? ===\n");
        kaadKoristiti();
    }

    // -----------------------------------------------------------------------
    // 1. Statička metoda: Klasa::statickaMetoda
    //    Lambda: s -> Student.jePolozio(s)
    // -----------------------------------------------------------------------
    static void statickaMetoda() {
        List<Student> studenti = List.of(
                new Student("Ana", 85),
                new Student("Bojan", 45),
                new Student("Čedomir", 72),
                new Student("Dragana", 30)
        );

        System.out.println("Lambda verzija:");
        studenti.stream()
                .filter(s -> Student.jePolozio(s))
                .forEach(s -> System.out.println("  " + s));

        System.out.println("Method reference verzija:");
        studenti.stream()
                .filter(Student::jePolozio)   // ekvivalentno gornjem
                .forEach(System.out::println); // Consumer — println na System.out objektu
    }

    // -----------------------------------------------------------------------
    // 2. Metoda instance na tipu: Klasa::metodaInstance
    //    Lambda: s -> s.getIme()
    //    Kompajler prosleđuje instancu kao prvi argument
    // -----------------------------------------------------------------------
    static void metodaInstanceNaTipu() {
        List<String> reci = List.of("zdravo", "svet", "funkcionalno", "java");

        System.out.println("Lambda: map(s -> s.toUpperCase())");
        reci.stream()
                .map(s -> s.toUpperCase())
                .forEach(s -> System.out.print(s + " "));
        System.out.println();

        System.out.println("Method reference: map(String::toUpperCase)");
        reci.stream()
                .map(String::toUpperCase)     // String::toUpperCase ≡ s -> s.toUpperCase()
                .forEach(System.out::println);

        // Još primera
        System.out.print("\nDužine reči: ");
        reci.stream()
                .map(String::length)          // s -> s.length()
                .forEach(n -> System.out.print(n + " "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // 3. Metoda instance na konkretnom objektu: objekat::metoda
    //    Lambda: x -> prefix.concat(x)
    // -----------------------------------------------------------------------
    static void metodaInstanceNaObjektu() {
        String prefix = "Student: ";

        // objekat je 'prefix', metoda je 'concat'
        // Lambda: s -> prefix.concat(s)
        List<String> imena = List.of("Ana", "Bojan", "Čedomir");

        System.out.println("Sa method reference na objektu:");
        imena.stream()
                .map(prefix::concat)   // ekvivalentno: s -> prefix.concat(s)
                .forEach(System.out::println);
    }

    // -----------------------------------------------------------------------
    // 4. Konstruktor reference: Klasa::new
    //    Lambda: (ime, poeni) -> new Student(ime, poeni)
    // -----------------------------------------------------------------------
    static void konstruktorReference() {
        // BiFunction jer Student ima 2 parametra
        BiFunction<String, Integer, Student> kreirajStudenta = Student::new;

        Student s1 = kreirajStudenta.apply("Eva", 91);
        Student s2 = kreirajStudenta.apply("Filip", 60);
        System.out.println("Kreirani studenti:");
        System.out.println("  " + s1);
        System.out.println("  " + s2);

        // Praktičan primer — kreiranje liste objekata iz podataka
        List<String> podaci = List.of("Goran:88", "Hana:55", "Ivan:40");
        List<Student> studenti = podaci.stream()
                .map(p -> p.split(":"))
                .map(parts -> new Student(parts[0], Integer.parseInt(parts[1])))
                .collect(Collectors.toList());

        System.out.println("\nStudenti iz sirovih podataka:");
        studenti.forEach(st -> System.out.println("  " + st));
    }

    // -----------------------------------------------------------------------
    // Kada koristiti method reference, a kada lambda?
    // -----------------------------------------------------------------------
    static void kaadKoristiti() {
        List<Integer> brojevi = List.of(1, 2, 3, 4, 5);

        // KORISTITI method reference kada lambda SAMO prosleđuje parametre metodi:
        // Dobro
        brojevi.stream().map(Math::sqrt).forEach(System.out::println);

        // KORISTITI lambda kada radimo nešto sa parametrom pre poziva:
        // Dobro — ne može biti method reference jer maniplišemo vrednosću
        brojevi.stream()
                .map(n -> Math.sqrt(n * 2))
                .forEach(d -> System.out.printf("%.2f%n", d));
    }
}
