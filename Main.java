import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {

    record StockPrice(String company, LocalDate date, double price) {
    }

    record PortfolioEntry(String company, int quantity) {
    }

    sealed interface TransactionType permits Buy, Sell {
    }

    record Buy() implements TransactionType {
    }

    record Sell() implements TransactionType {
    }

    record Transaction(String company, LocalDate date, int quantity, double price, TransactionType type) {
    }

    record Portfolio(List<PortfolioEntry> entries, List<Transaction> transactions) {

        public Portfolio {
            entries = List.copyOf(entries);
            transactions = List.copyOf(transactions);
        }

        Portfolio buy(String company, int quantity, double price, LocalDate date) {
            List<PortfolioEntry> newEntries = new ArrayList<>(entries);

            Optional<PortfolioEntry> existing = newEntries.stream()
                    .filter(entry -> entry.company().equalsIgnoreCase(company))
                    .findFirst();

            if (existing.isPresent()) {
                PortfolioEntry oldEntry = existing.get();
                newEntries.remove(oldEntry);
                newEntries.add(new PortfolioEntry(oldEntry.company(), oldEntry.quantity() + quantity));
            } else {
                newEntries.add(new PortfolioEntry(company, quantity));
            }

            List<Transaction> newTransactions = new ArrayList<>(transactions);
            newTransactions.add(new Transaction(company, date, quantity, price, new Buy()));

            return new Portfolio(newEntries, newTransactions);
        }

        Portfolio sell(String company, int quantity, double price, LocalDate date) {
            List<PortfolioEntry> newEntries = new ArrayList<>(entries);

            Optional<PortfolioEntry> existing = newEntries.stream()
                    .filter(entry -> entry.company().equalsIgnoreCase(company))
                    .findFirst();

            if (existing.isEmpty()) {
                System.out.println("Nie posiadasz akcji tej spółki.");
                return this;
            }

            PortfolioEntry oldEntry = existing.get();

            if (oldEntry.quantity() < quantity) {
                System.out.println("Nie masz wystarczającej liczby akcji do sprzedaży.");
                return this;
            }

            newEntries.remove(oldEntry);

            if (oldEntry.quantity() > quantity) {
                newEntries.add(new PortfolioEntry(oldEntry.company(), oldEntry.quantity() - quantity));
            }

            List<Transaction> newTransactions = new ArrayList<>(transactions);
            newTransactions.add(new Transaction(company, date, quantity, price, new Sell()));

            return new Portfolio(newEntries, newTransactions);
        }
    }

    static List<StockPrice> loadSampleData() {
        return List.of(
                new StockPrice("Apple", LocalDate.of(2024, 1, 2), 182.5),
                new StockPrice("Apple", LocalDate.of(2024, 1, 3), 184.2),
                new StockPrice("Apple", LocalDate.of(2024, 1, 4), 181.8),

                new StockPrice("Tesla", LocalDate.of(2024, 1, 2), 248.9),
                new StockPrice("Tesla", LocalDate.of(2024, 1, 3), 251.4),
                new StockPrice("Tesla", LocalDate.of(2024, 1, 4), 255.0),

                new StockPrice("Amazon", LocalDate.of(2024, 1, 2), 145.3),
                new StockPrice("Amazon", LocalDate.of(2024, 1, 3), 147.1),
                new StockPrice("Amazon", LocalDate.of(2024, 1, 4), 146.0),

                new StockPrice("Google", LocalDate.of(2024, 1, 2), 139.7),
                new StockPrice("Google", LocalDate.of(2024, 1, 3), 141.5),
                new StockPrice("Google", LocalDate.of(2024, 1, 4), 140.2));
    }

    static List<StockPrice> filterPrices(List<StockPrice> prices, Predicate<StockPrice> predicate) {
        return prices.stream()
                .filter(predicate)
                .toList();
    }

    static List<StockPrice> filterByCompany(List<StockPrice> prices, String company) {
        return filterPrices(prices, price -> price.company().equalsIgnoreCase(company));
    }

    static List<StockPrice> filterByDateRange(List<StockPrice> prices, LocalDate from, LocalDate to) {
        return filterPrices(prices, price -> !price.date().isBefore(from) && !price.date().isAfter(to));
    }

    static Optional<StockPrice> findLatestPrice(List<StockPrice> prices, String company) {
        return prices.stream()
                .filter(price -> price.company().equalsIgnoreCase(company))
                .max(Comparator.comparing(StockPrice::date));
    }

    static double calculatePortfolioValue(Portfolio portfolio, List<StockPrice> prices) {
        return portfolio.entries().stream()
                .map(entry -> findLatestPrice(prices, entry.company())
                        .map(stockPrice -> stockPrice.price() * entry.quantity())
                        .orElse(0.0))
                .reduce(0.0, Double::sum);
    }

    static double calculateProfitLoss(Portfolio portfolio, List<StockPrice> prices) {
        double buyCost = portfolio.transactions().stream()
                .filter(transaction -> transaction.type() instanceof Buy)
                .map(transaction -> transaction.quantity() * transaction.price())
                .reduce(0.0, Double::sum);

        double sellIncome = portfolio.transactions().stream()
                .filter(transaction -> transaction.type() instanceof Sell)
                .map(transaction -> transaction.quantity() * transaction.price())
                .reduce(0.0, Double::sum);

        double currentValue = calculatePortfolioValue(portfolio, prices);

        return currentValue + sellIncome - buyCost;
    }

    static String describeTransaction(Transaction transaction) {
        if (transaction.type() instanceof Buy) {
            return "Kupno | spółka: " + transaction.company()
                    + ", liczba akcji: " + transaction.quantity()
                    + ", cena: " + transaction.price()
                    + ", data: " + transaction.date();
        } else if (transaction.type() instanceof Sell) {
            return "Sprzedaż | spółka: " + transaction.company()
                    + ", liczba akcji: " + transaction.quantity()
                    + ", cena: " + transaction.price()
                    + ", data: " + transaction.date();
        } else {
            return "Nieznany typ transakcji";
        }
    }

    static void showAllCompanies(List<StockPrice> prices) {
        System.out.println("Dostępne spółki:");
        prices.stream()
                .map(StockPrice::company)
                .distinct()
                .sorted()
                .forEach(System.out::println);
    }

    static void showPricesForCompany(List<StockPrice> prices, String company) {
        List<StockPrice> result = filterByCompany(prices, company);

        if (result.isEmpty()) {
            System.out.println("Nie znaleziono danych dla tej spółki.");
            return;
        }

        result.forEach(System.out::println);
    }

    static void showPricesForDateRange(List<StockPrice> prices, LocalDate from, LocalDate to) {
        List<StockPrice> result = filterByDateRange(prices, from, to);

        if (result.isEmpty()) {
            System.out.println("Brak danych w podanym zakresie dat.");
            return;
        }

        result.forEach(System.out::println);
    }

    static String normalizeCompanyName(String input) {
        Function<String, String> trim = String::trim;
        Function<String, String> normalizeCase = text -> {
            if (text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        };

        return trim.andThen(normalizeCase).apply(input);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<StockPrice> prices = loadSampleData();
        Portfolio portfolio = new Portfolio(List.of(), List.of());

        while (true) {
            System.out.println("\n=== SYMULATOR GIEŁDOWY ===");
            System.out.println("1. Pokaż dostępne spółki");
            System.out.println("2. Pokaż ceny wybranej spółki");
            System.out.println("3. Filtruj dane po zakresie dat");
            System.out.println("4. Kup akcje");
            System.out.println("5. Sprzedaj akcje");
            System.out.println("6. Pokaż portfel");
            System.out.println("7. Pokaż historię transakcji");
            System.out.println("8. Oblicz wartość portfela");
            System.out.println("9. Oblicz zysk/stratę");
            System.out.println("0. Wyjście");
            System.out.print("Wybierz opcję: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Podano niepoprawną wartość.");
                continue;
            }

            switch (choice) {
                case 1 -> showAllCompanies(prices);

                case 2 -> {
                    System.out.print("Podaj nazwę spółki: ");
                    String company = normalizeCompanyName(scanner.nextLine());
                    showPricesForCompany(prices, company);
                }

                case 3 -> {
                    try {
                        System.out.print("Podaj datę początkową (rrrr-mm-dd): ");
                        LocalDate from = LocalDate.parse(scanner.nextLine());

                        System.out.print("Podaj datę końcową (rrrr-mm-dd): ");
                        LocalDate to = LocalDate.parse(scanner.nextLine());

                        showPricesForDateRange(prices, from, to);
                    } catch (Exception e) {
                        System.out.println("Błędny format daty.");
                    }
                }

                case 4 -> {
                    System.out.print("Podaj nazwę spółki: ");
                    String company = normalizeCompanyName(scanner.nextLine());

                    System.out.print("Podaj liczbę akcji do kupna: ");
                    int quantity;
                    try {
                        quantity = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Niepoprawna liczba.");
                        continue;
                    }

                    Optional<StockPrice> latest = findLatestPrice(prices, company);

                    if (latest.isPresent()) {
                        StockPrice stockPrice = latest.get();
                        portfolio = portfolio.buy(company, quantity, stockPrice.price(), LocalDate.now());
                        System.out.println("Kupiono akcje spółki " + company + " po cenie " + stockPrice.price());
                    } else {
                        System.out.println("Nie znaleziono takiej spółki.");
                    }
                }

                case 5 -> {
                    System.out.print("Podaj nazwę spółki: ");
                    String company = normalizeCompanyName(scanner.nextLine());

                    System.out.print("Podaj liczbę akcji do sprzedaży: ");
                    int quantity;
                    try {
                        quantity = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Niepoprawna liczba.");
                        continue;
                    }

                    Optional<StockPrice> latest = findLatestPrice(prices, company);

                    if (latest.isPresent()) {
                        StockPrice stockPrice = latest.get();
                        portfolio = portfolio.sell(company, quantity, stockPrice.price(), LocalDate.now());
                    } else {
                        System.out.println("Nie znaleziono takiej spółki.");
                    }
                }

                case 6 -> {
                    if (portfolio.entries().isEmpty()) {
                        System.out.println("Portfel jest pusty.");
                    } else {
                        System.out.println("Zawartość portfela:");
                        portfolio.entries().forEach(System.out::println);
                    }
                }

                case 7 -> {
                    if (portfolio.transactions().isEmpty()) {
                        System.out.println("Brak transakcji.");
                    } else {
                        System.out.println("Historia transakcji:");
                        portfolio.transactions().stream()
                                .map(Main::describeTransaction)
                                .forEach(System.out::println);
                    }
                }

                case 8 -> {
                    double value = calculatePortfolioValue(portfolio, prices);
                    System.out.println("Aktualna wartość portfela: " + value);
                }

                case 9 -> {
                    double result = calculateProfitLoss(portfolio, prices);
                    System.out.println("Zysk/strata: " + result);
                }

                case 0 -> {
                    System.out.println("Koniec programu.");
                    return;
                }

                default -> System.out.println("Niepoprawna opcja.");
            }
        }
    }
}