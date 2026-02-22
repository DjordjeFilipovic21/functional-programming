package raf.edu.week1;

import java.util.Arrays;
import java.util.List;

/**
 * Nedelja 1 — Poređenje imperativnog i funkcionalnog stila
 */
public class ImperativeVsFunctional {

    public static void main(String[] args) {
        List<String> cities = Arrays.asList("Beograd", "Novi Sad", "Niš", "Chicago", "Kragujevac");
        List<Integer> prices = Arrays.asList(10, 30, 17, 20, 18, 45, 12);

        System.out.println("=== Primer 1: Pronalaženje elementa u listi ===\n");
        findCityImperative(cities);
        findCityFunctional(cities);

        System.out.println("\n=== Primer 2: Suma uz popust ===\n");
        discountImperative(prices);
        discountFunctional(prices);

        System.out.println("\n=== Primer 3: Paralelizacija ===\n");
        parallelExample(prices);
    }

    // -----------------------------------------------------------------------
    // Primer 1: Pronalaženje elementa
    // -----------------------------------------------------------------------

    static void findCityImperative(List<String> cities) {
        // Imperativni stil:
        // Eksplicitna mutabilna promenljiva (found)
        // for petlja i break
        // Fokus na 'kako' nesto radimo
        boolean found = false;
        for (String city : cities) {
            if (city.equals("Chicago")) {
                found = true;
                break;
            }
        }
        System.out.println("[Imperativno] Pronađen Chicago? " + found);
    }

    static void findCityFunctional(List<String> cities) {
        // Funkcionalni / deklarativni stil:
        // - Nema mutabilnih promenljivih
        // - Fokus na 'šta' radimo — "da li lista sadrži Chicago?"
        // - Kraće i jasnije (nadamo se)
        boolean found = cities.stream()
                .anyMatch(city -> city.equals("Chicago"));
        System.out.println("[Funkcionalno] Pronađen Chicago? " + found);

        // Još kraće — direktno sa contains (klasična deklarativna metoda):
        System.out.println("[Deklarativno] Pronađen Chicago? " + cities.contains("Chicago"));
    }

    // -----------------------------------------------------------------------
    // Primer 2: Suma cena sa popustom (cene > 20, popust 10%)
    // -----------------------------------------------------------------------

    static void discountImperative(List<Integer> prices) {
        // Imperativni stil — krši SRP i SLAP principe
        // Tri nivoa ugnežđavanja: kolekcija → uslov → računanje
        double totalOfDiscountedPrices = 0.0;
        for (int price : prices) {
            if (price > 20) {
                totalOfDiscountedPrices += price * 0.9;
            }
        }
        System.out.println("[Imperativno] Ukupno sa popustom: " + totalOfDiscountedPrices);
    }

    static void discountFunctional(List<Integer> prices) {
        // Funkcionalni stil — čita se skoro kao specifikacija zahteva:
        // "filtriraj cene > 20, primeni popust 10%, izračunaj sumu"
        //
        //  filter  → zadržava elemente koji zadovoljavaju Predicate
        //  mapToDouble → transformiše svaki element (Function)
        //  sum     → terminalna operacija (reduce specijalni slučaj)
        final double totalOfDiscountedPrices = prices.stream()
                .filter(price -> price > 20)
                .mapToDouble(price -> price * 0.9)
                .sum();
        System.out.println("[Funkcionalno] Ukupno sa popustom: " + totalOfDiscountedPrices);
    }

    // -----------------------------------------------------------------------
    // Primer 3: Paralelizacija — samo jedna reč se menja!
    // -----------------------------------------------------------------------

    static void parallelExample(List<Integer> prices) {
        // Sekvencijalno
        double seqResult = prices.stream()
                .filter(p -> p > 20)
                .mapToDouble(p -> p * 0.9)
                .sum();

        // Paralelno — zamenjujemo stream() sa parallelStream()
        // U imperativnom kodu bi ovo zahtevalo značajan refaktoring i sinhronizaciju!
        double parResult = prices.parallelStream()
                .filter(p -> p > 20)
                .mapToDouble(p -> p * 0.9)
                .sum();

        System.out.println("[Sekvencijalno] " + seqResult);
        System.out.println("[Paralelno]    " + parResult);
        System.out.println("Rezultati jednaki: " + (seqResult == parResult));
    }
}
