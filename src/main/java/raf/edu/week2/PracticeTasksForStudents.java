package raf.edu.week2;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Nedelja 2 — Zadaci za vežbu na času
 *
 * Svaki zadatak ima:
 *   - Opis šta treba uraditi
 *   - TODO komentar gde treba napisati rešenje
 *   - Očekivani izlaz u komentaru
 *
 * Studenti treba da popune TODO delove i pokrenu program da provere rezultate.
 */
public class PracticeTasksForStudents {

    record Product(String name, double price, String category) {}
    record Employee(String name, int age, String department, double salary) {}

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Nedelja 2 — Zadaci za vežbu                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        zadatak1();
        zadatak2();
        zadatak3();
        zadatak4();
        zadatak5();
        zadatak6();
        zadatak7();
        zadatak8();
    }

    // =======================================================================
    // ZADATAK 1: Predicate — filtriranje
    // =======================================================================
    static void zadatak1() {
        System.out.println("--- Zadatak 1: Predicate ---");
        System.out.println("Dat je spisak brojeva. Koristeći Predicate<Integer>:");
        System.out.println("  a) Izdvojiti sve parne brojeve");
        System.out.println("  b) Izdvojiti brojeve veće od 10 i manje od 50");
        System.out.println("  c) Koristeći kompoziciju (and/or), izdvojiti parne I veće od 20\n");

        List<Integer> brojevi = List.of(5, 12, 8, 33, 47, 24, 60, 15, 42, 3, 18, 55);

        // a) Kreirati Predicate<Integer> za parne brojeve i ispisati filtrirane
        // TODO: Predicate<Integer> jeParno = ...
        // TODO: Ispisati filtrirane brojeve koristeći stream + filter + forEach

        // Očekivani izlaz: 12 8 24 60 42 18

        // b) Kreirati Predicate za brojeve između 10 i 50
        // TODO: Predicate<Integer> izmedju10i50 = ...
        // TODO: Ispisati filtrirane

        // Očekivani izlaz: 12 33 47 24 15 42 18

        // c) Kompozicija: parno AND > 20
        // TODO: Koristeći .and() kombinovati dva predikata
        // TODO: Ispisati filtrirane

        // Očekivani izlaz: 24 60 42

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 2: Function — transformacija
    // =======================================================================
    static void zadatak2() {
        System.out.println("--- Zadatak 2: Function ---");
        System.out.println("Data je lista stringova. Koristeći Function<String, ...>:");
        System.out.println("  a) Transformisati svaki string u njegovu dužinu");
        System.out.println("  b) Transformisati svaki string u uppercase + dužina (npr. \"ANA (3)\"");
        System.out.println("  c) Koristeći andThen, napraviti kompoziciju: ime → dužina → kvadrat\n");

        List<String> imena = List.of("Ana", "Bojan", "Čedomir", "Dragana", "Elena");

        // a) Function koji vraća dužinu stringa
        // TODO: Function<String, Integer> duzina = ...
        // TODO: Ispisati rezultat sa map + forEach

        // Očekivani izlaz: 3 5 7 7 5

        // b) Function koji formatira string
        // TODO: Function<String, String> formatiraj = ...
        // TODO: Ispisati rezultat

        // Očekivani izlaz: ANA (3), BOJAN (5), ČEDOMIR (7), DRAGANA (7), ELENA (5)

        // c) Kompozicija sa andThen: duzina → kvadrat
        // TODO: Function<Integer, Integer> kvadrat = ...
        // TODO: Function<String, Integer> duzinaNaKvadrat = duzina.andThen(kvadrat)
        // TODO: Ispisati rezultat

        // Očekivani izlaz: 9 25 49 49 25

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 3: Consumer i forEach
    // =======================================================================
    static void zadatak3() {
        System.out.println("--- Zadatak 3: Consumer i forEach ---");
        System.out.println("Data je lista proizvoda:");
        System.out.println("  a) Kreirati Consumer koji štampa proizvod u formatu: \"Ime — cena RSD\"");
        System.out.println("  b) Kreirati drugi Consumer koji štampa \"[SKUPO]\" ako je cena > 1000");
        System.out.println("  c) Ulančati ih sa andThen i primeniti na listu\n");

        List<Product> proizvodi = List.of(
                new Product("Laptop", 85000, "Elektronika"),
                new Product("Knjiga", 950, "Edukacija"),
                new Product("Miš", 2500, "Elektronika"),
                new Product("Olovka", 120, "Kancelarija"),
                new Product("Monitor", 45000, "Elektronika")
        );

        // a) Consumer koji formatira ispis
        // TODO: Consumer<Product> stampaj = p -> ...

        // b) Consumer koji označava skupe
        // TODO: Consumer<Product> oznaci = p -> ...

        // c) Ulančati i primeniti
        // TODO: Consumer<Product> oba = stampaj.andThen(oznaci);
        // TODO: proizvodi.forEach(oba);

        // Očekivani izlaz (otprilike):
        // Laptop — 85000.0 RSD
        //   [SKUPO]
        // Knjiga — 950.0 RSD
        // Miš — 2500.0 RSD
        //   [SKUPO]
        // ...

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 4: Supplier
    // =======================================================================
    static void zadatak4() {
        System.out.println("--- Zadatak 4: Supplier ---");
        System.out.println("  a) Kreirati Supplier<String> koji generiše podrazumevani pozdrav");
        System.out.println("  b) Koristiti ga sa Optional.orElseGet()");
        System.out.println("  c) Koristiti Stream.generate() sa Supplier-om za 5 random brojeva\n");

        // a) Supplier koji vraća podrazumevani pozdrav
        // TODO: Supplier<String> podrazumevaniPozdrav = ...

        // b) Koristiti sa Optional
        Optional<String> prazanOpt = Optional.empty();
        Optional<String> punOpt = Optional.of("Zdravo, studenti!");

        // TODO: String rezultat1 = prazanOpt.orElseGet(podrazumevaniPozdrav);
        // TODO: String rezultat2 = punOpt.orElseGet(podrazumevaniPozdrav);
        // TODO: Ispisati oba rezultata

        // Očekivani izlaz:
        // Prazan: Podrazumevani pozdrav
        // Pun: Zdravo, studenti!

        // c) Stream.generate sa Supplier<Integer> za random brojeve od 1 do 100
        // TODO: Supplier<Integer> randomBroj = () -> ...
        // TODO: Stream.generate(randomBroj).limit(5).forEach(...)

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 5: Ponovno korišćenje lambda izraza
    // =======================================================================
    static void zadatak5() {
        System.out.println("--- Zadatak 5: Ponovno korišćenje lambda izraza ---");
        System.out.println("Date su tri liste zaposlenih iz različitih odeljenja.");
        System.out.println("  a) BEZ duplikacije: prebrojati zaposlene starije od 30 u svakoj listi");
        System.out.println("  b) Napisati higher-order funkciju koja za datu minimalnu platu");
        System.out.println("     vraća Predicate<Employee>\n");

        List<Employee> it = List.of(
                new Employee("Ana", 28, "IT", 1200),
                new Employee("Bojan", 35, "IT", 1800),
                new Employee("Čedomir", 42, "IT", 2200)
        );
        List<Employee> hr = List.of(
                new Employee("Dragana", 31, "HR", 1100),
                new Employee("Elena", 26, "HR", 900)
        );
        List<Employee> finance = List.of(
                new Employee("Filip", 38, "Finance", 2000),
                new Employee("Goran", 29, "Finance", 1500),
                new Employee("Hana", 45, "Finance", 2500)
        );

        // a) Kreirati JEDAN Predicate i koristiti ga za sve tri liste
        // TODO: Predicate<Employee> starijiOd30 = ...
        // TODO: Ispisati count za svaku listu

        // Očekivani izlaz:
        // IT stariji od 30: 2
        // HR stariji od 30: 1
        // Finance stariji od 30: 2

        // b) Higher-order funkcija: za datu platu vraća Predicate
        // TODO: Function<Double, Predicate<Employee>> plataNajmanje = ...
        //       (ili napisati statičku metodu)
        // TODO: Koristiti za filtriranje zaposlenih sa platom >= 1500 i >= 2000

        // Očekivani izlaz:
        // Plata >= 1500: Bojan, Čedomir, Filip, Goran, Hana
        // Plata >= 2000: Čedomir, Filip, Hana

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 6: collect i Collectors
    // =======================================================================
    static void zadatak6() {
        System.out.println("--- Zadatak 6: collect i Collectors ---");
        System.out.println("Data je lista proizvoda. Koristeći collect:");
        System.out.println("  a) Skupiti imena proizvoda u listu");
        System.out.println("  b) Grupisati proizvode po kategoriji");
        System.out.println("  c) Particionisati na skupe (>1000) i jeftine\n");

        List<Product> proizvodi = List.of(
                new Product("Laptop", 85000, "Elektronika"),
                new Product("Knjiga", 950, "Edukacija"),
                new Product("Miš", 2500, "Elektronika"),
                new Product("Olovka", 120, "Kancelarija"),
                new Product("Monitor", 45000, "Elektronika"),
                new Product("Sveska", 200, "Kancelarija"),
                new Product("Kurs Java", 15000, "Edukacija")
        );

        // a) Skupiti imena svih proizvoda u List<String>
        // TODO: List<String> imena = proizvodi.stream()...collect(...)
        // TODO: Ispisati listu

        // Očekivani izlaz: [Laptop, Knjiga, Miš, Olovka, Monitor, Sveska, Kurs Java]

        // b) Grupisati po kategoriji: Map<String, List<Product>>
        // TODO: Map<String, List<Product>> poKategoriji = ...
        // TODO: Ispisati svaku kategoriju sa njenim proizvodima

        // Očekivani izlaz:
        // Elektronika: Laptop, Miš, Monitor
        // Edukacija: Knjiga, Kurs Java
        // Kancelarija: Olovka, Sveska

        // c) Particionisati na skupe (>1000) i jeftine
        // TODO: Map<Boolean, List<Product>> podela = ...
        // TODO: Ispisati obe grupe

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 7: joining i reduce
    // =======================================================================
    static void zadatak7() {
        System.out.println("--- Zadatak 7: joining i reduce ---");
        System.out.println("Data je lista gradova:");
        System.out.println("  a) Spojiti sve gradove u jedan String, razdvojene sa \" | \"");
        System.out.println("  b) Spojiti sa prefiksom i sufiksom: \"Gradovi: [Bg, Ni, NS]\"");
        System.out.println("  c) Naći najduži naziv grada koristeći reduce\n");

        List<String> gradovi = List.of("Beograd", "Niš", "Novi Sad", "Kragujevac", "Subotica", "Čačak");

        // a) Collectors.joining sa separatorom " | "
        // TODO: String spojeni = gradovi.stream()...collect(Collectors.joining(...))
        // TODO: Ispisati

        // Očekivani izlaz: Beograd | Niš | Novi Sad | Kragujevac | Subotica | Čačak

        // b) Collectors.joining sa prefiksom i sufiksom
        // TODO: String formatiran = gradovi.stream()...collect(Collectors.joining(...))
        // TODO: Ispisati

        // Očekivani izlaz: Gradovi: [Beograd, Niš, Novi Sad, Kragujevac, Subotica, Čačak]

        // c) reduce za najduži grad
        // TODO: Optional<String> najduzi = gradovi.stream().reduce(...)
        // TODO: Ispisati

        // Očekivani izlaz: Najduži grad: Kragujevac

        System.out.println();
    }

    // =======================================================================
    // ZADATAK 8: Kompleksan zadatak — sve zajedno
    // =======================================================================
    static void zadatak8() {
        System.out.println("--- Zadatak 8: Kompleksan zadatak ---");
        System.out.println("Data je lista zaposlenih. Potrebno je:\n");

        List<Employee> zaposleni = List.of(
                new Employee("Ana", 28, "IT", 1200),
                new Employee("Bojan", 35, "IT", 1800),
                new Employee("Čedomir", 42, "IT", 2200),
                new Employee("Dragana", 31, "HR", 1100),
                new Employee("Elena", 26, "HR", 900),
                new Employee("Filip", 38, "Finance", 2000),
                new Employee("Goran", 29, "Finance", 1500),
                new Employee("Hana", 45, "Finance", 2500),
                new Employee("Ivan", 33, "IT", 1600),
                new Employee("Jelena", 27, "HR", 1050)
        );

        // a) Naći prosečnu platu po odeljenju
        System.out.println("a) Prosečna plata po odeljenju:");
        // TODO: Koristiti groupingBy + averagingDouble

        // Očekivani izlaz:
        // IT: 1700.0
        // HR: 1016.67
        // Finance: 2000.0

        // b) Za svako odeljenje, naći zaposlenog sa najvećom platom
        System.out.println("\nb) Zaposleni sa najvećom platom po odeljenju:");
        // TODO: Koristiti groupingBy + maxBy ili sopstveni pristup

        // Očekivani izlaz:
        // IT: Čedomir (2200.0)
        // HR: Dragana (1100.0)
        // Finance: Hana (2500.0)

        // c) Ispisati imena svih zaposlenih koji zarađuju iznad proseka cele firme,
        //    spojena zarezom
        System.out.println("\nc) Zaposleni iznad proseka firme:");
        // TODO: Prvo izračunati prosek, pa filtrirati, pa joinovati imena

        // d) Ispisati ukupan fond zarada (suma svih plata) koristeći reduce ili sum
        System.out.println("\nd) Ukupan fond zarada:");
        // TODO: Koristiti mapToDouble + sum ili reduce

        System.out.println();
    }
}
