package raf.edu.week2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Nedelja 2 — collect, Collectors, join, reduce
 *
 * collect() je terminalna operacija koja skuplja elemente Stream-a
 * u konačnu strukturu (List, Set, Map, String, itd.)
 *
 * reduce() svodi kolekciju na jednu vrednost koristeći BinaryOperator.
 *
 * String.join() i Collectors.joining() — spajanje elemenata u String.
 */
public class CollectAndJoin {

    record Student(String ime, int poeni, String grad) {}

    public static void main(String[] args) {
        List<String> prijatelji = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");
        List<Student> studenti = List.of(
                new Student("Ana", 85, "Beograd"),
                new Student("Bojan", 45, "Niš"),
                new Student("Čedomir", 92, "Beograd"),
                new Student("Dragana", 67, "Novi Sad"),
                new Student("Elena", 55, "Niš"),
                new Student("Filip", 78, "Beograd")
        );

        System.out.println("=== 1. collect — skupljanje u List ===\n");
        collectToList(prijatelji);

        System.out.println("\n=== 2. collect — skupljanje u Set i Map ===\n");
        collectToSetAndMap(studenti);

        System.out.println("\n=== 3. Collectors.groupingBy ===\n");
        groupingByDemo(studenti);

        System.out.println("\n=== 4. Collectors.partitioningBy ===\n");
        partitioningByDemo(studenti);

        System.out.println("\n=== 5. String.join — spajanje elemenata ===\n");
        joinDemo(prijatelji);

        System.out.println("\n=== 6. Collectors.joining — sa transformacijom ===\n");
        collectorsJoiningDemo(prijatelji, studenti);

        System.out.println("\n=== 7. reduce — svođenje na jednu vrednost ===\n");
        reduceDemo(prijatelji);

        System.out.println("\n=== 8. Praktičan primer — kompleksan pipeline ===\n");
        practicalExample(studenti);
    }

    // -----------------------------------------------------------------------
    // 1. collect u List
    // -----------------------------------------------------------------------
    static void collectToList(List<String> prijatelji) {
        // Transformacija + skupljanje u novu listu
        List<String> velikaSlova = prijatelji.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());  // mutable lista

        System.out.println("Collectors.toList(): " + velikaSlova);

        // Java 16+ — toList() metoda na Stream-u (vraća immutable listu)
        List<String> nepromenljiva = prijatelji.stream()
                .map(String::toUpperCase)
                .toList();

        System.out.println("Stream.toList(): " + nepromenljiva);

        // Originalna lista je nepromenjena!
        System.out.println("Original: " + prijatelji);

        // Filter + collect
        List<String> saN = prijatelji.stream()
                .filter(name -> name.startsWith("N"))
                .collect(Collectors.toList());
        System.out.println("Imena sa N: " + saN);
    }

    // -----------------------------------------------------------------------
    // 2. collect u Set i Map
    // -----------------------------------------------------------------------
    static void collectToSetAndMap(List<Student> studenti) {
        // U Set — automatski uklanja duplikate
        Set<String> gradovi = studenti.stream()
                .map(Student::grad)
                .collect(Collectors.toSet());
        System.out.println("Gradovi (Set): " + gradovi);

        // U Map — ključ je ime, vrednost su poeni
        Map<String, Integer> imePoeni = studenti.stream()
                .collect(Collectors.toMap(
                        Student::ime,     // keyMapper: Student → String
                        Student::poeni    // valueMapper: Student → Integer
                ));
        System.out.println("Ime → Poeni: " + imePoeni);

        // U Map — ključ je ime, vrednost je ceo objekat
        Map<String, Student> imeStudent = studenti.stream()
                .collect(Collectors.toMap(
                        Student::ime,
                        student -> student  // ili Function.identity()
                ));
        System.out.println("Ime → Student: " + imeStudent.get("Ana"));
    }

    // -----------------------------------------------------------------------
    // 3. Collectors.groupingBy — grupisanje po kriterijumu
    // -----------------------------------------------------------------------
    static void groupingByDemo(List<Student> studenti) {
        // Grupisanje po gradu
        Map<String, List<Student>> poGradu = studenti.stream()
                .collect(Collectors.groupingBy(Student::grad));

        System.out.println("Grupisani po gradu:");
        poGradu.forEach((grad, lista) -> {
            System.out.print("  " + grad + ": ");
            lista.forEach(s -> System.out.print(s.ime() + " "));
            System.out.println();
        });

        // Grupisanje po prolaznosti
        Map<String, List<Student>> poOceni = studenti.stream()
                .collect(Collectors.groupingBy(s -> s.poeni() >= 50 ? "Položio" : "Pao"));

        System.out.println("\nGrupisani po prolaznosti:");
        poOceni.forEach((status, lista) -> {
            System.out.print("  " + status + ": ");
            lista.forEach(s -> System.out.print(s.ime() + "(" + s.poeni() + ") "));
            System.out.println();
        });

        // groupingBy sa downstream kolekcijom — npr. brojanje po gradu
        Map<String, Long> brojPoGradu = studenti.stream()
                .collect(Collectors.groupingBy(Student::grad, Collectors.counting()));
        System.out.println("\nBroj studenata po gradu: " + brojPoGradu);

        // groupingBy sa downstream — prosek poena po gradu
        Map<String, Double> prosekPoGradu = studenti.stream()
                .collect(Collectors.groupingBy(
                        Student::grad,
                        Collectors.averagingInt(Student::poeni)
                ));
        System.out.println("Prosek poena po gradu: " + prosekPoGradu);
    }

    // -----------------------------------------------------------------------
    // 4. Collectors.partitioningBy — podela na true/false
    // -----------------------------------------------------------------------
    static void partitioningByDemo(List<Student> studenti) {
        // partitioningBy uvek vraća Map<Boolean, List<T>>
        Map<Boolean, List<Student>> poloziliIliNe = studenti.stream()
                .collect(Collectors.partitioningBy(s -> s.poeni() >= 50));

        System.out.println("Položili:");
        poloziliIliNe.get(true).forEach(s -> System.out.println("  " + s.ime()));
        System.out.println("Pali:");
        poloziliIliNe.get(false).forEach(s -> System.out.println("  " + s.ime()));
    }

    // -----------------------------------------------------------------------
    // 5. String.join — jednostavno spajanje
    // -----------------------------------------------------------------------
    static void joinDemo(List<String> prijatelji) {
        // Problem sa ručnim spajanjem — zarez na kraju
        System.out.println("Ručno (LOŠE — višak zareza):");
        StringBuilder sb = new StringBuilder();
        for (String name : prijatelji) {
            sb.append(name).append(", ");
        }
        System.out.println("  " + sb);

        // String.join — elegantno, bez viška zareza
        System.out.println("String.join (DOBRO):");
        String spojena = String.join(", ", prijatelji);
        System.out.println("  " + spojena);

        // String.join sa varargs
        String put = String.join("/", "home", "user", "documents");
        System.out.println("Putanja: " + put);
    }

    // -----------------------------------------------------------------------
    // 6. Collectors.joining — spajanje sa transformacijom
    // -----------------------------------------------------------------------
    static void collectorsJoiningDemo(List<String> prijatelji, List<Student> studenti) {
        // joining sa separatorom
        String spojenaImena = prijatelji.stream()
                .collect(Collectors.joining(", "));
        System.out.println("joining(\", \"): " + spojenaImena);

        // joining sa separatorom, prefiksom i sufiksom
        String formatirana = prijatelji.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining(\", \", \"[\", \"]\"): " + formatirana);

        // Praktičan primer — lista studenata u CSV format
        String csv = studenti.stream()
                .map(s -> s.ime() + ";" + s.poeni() + ";" + s.grad())
                .collect(Collectors.joining("\n"));
        System.out.println("\nCSV format:\n" + csv);

        // Samo imena studenata koji su položili
        String polozili = studenti.stream()
                .filter(s -> s.poeni() >= 50)
                .map(Student::ime)
                .collect(Collectors.joining(", "));
        System.out.println("\nPoložili: " + polozili);
    }

    // -----------------------------------------------------------------------
    // 7. reduce — svođenje kolekcije
    // -----------------------------------------------------------------------
    static void reduceDemo(List<String> prijatelji) {
        // reduce bez početne vrednosti — vraća Optional
        Optional<String> najduze = prijatelji.stream()
                .reduce((name1, name2) -> name1.length() >= name2.length() ? name1 : name2);
        najduze.ifPresent(name -> System.out.println("Najduže ime: " + name));

        // reduce sa početnom vrednošću (identity) — vraća direktno vrednost
        String najduze2 = prijatelji.stream()
                .reduce("", (name1, name2) -> name1.length() >= name2.length() ? name1 : name2);
        System.out.println("Najduže ime (sa identity): " + najduze2);

        // reduce za sumu dužina
        int ukupnaDuzina = prijatelji.stream()
                .map(String::length)
                .reduce(0, Integer::sum);
        System.out.println("Ukupna dužina svih imena: " + ukupnaDuzina);

        // Ekvivalentno specijalizovanim metodama
        int ukupnaDuzina2 = prijatelji.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("Isto sa mapToInt + sum: " + ukupnaDuzina2);

        // reduce za spajanje (da vidimo kako joining radi "ispod haube")
        String spojena = prijatelji.stream()
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        System.out.println("Ručni join sa reduce: " + spojena);
    }

    // -----------------------------------------------------------------------
    // 8. Praktičan primer
    // -----------------------------------------------------------------------
    static void practicalExample(List<Student> studenti) {
        // Izveštaj: prosek poena studenata iz Beograda koji su položili
        double prosek = studenti.stream()
                .filter(s -> s.grad().equals("Beograd"))
                .filter(s -> s.poeni() >= 50)
                .mapToInt(Student::poeni)
                .average()
                .orElse(0);
        System.out.printf("Prosek (Beograd, položili): %.1f%n", prosek);

        // Izveštaj po gradu
        System.out.println("\nIzveštaj po gradu:");
        studenti.stream()
                .collect(Collectors.groupingBy(Student::grad))
                .forEach((grad, lista) -> {
                    String imena = lista.stream().map(Student::ime).collect(Collectors.joining(", "));
                    double avg = lista.stream().mapToInt(Student::poeni).average().orElse(0);
                    System.out.printf("  %s: [%s] — prosek: %.1f%n", grad, imena, avg);
                });

        // Top 3 studenta po poenima
        System.out.println("\nTop 3 studenta:");
        studenti.stream()
                .sorted(Comparator.comparingInt(Student::poeni).reversed())
                .limit(3)
                .forEach(s -> System.out.printf("  %s (%d poena)%n", s.ime(), s.poeni()));
    }
}
