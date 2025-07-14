public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Simulated book list (replace later with real inventory)
            List<Book> sampleBooks = new ArrayList<>();
            sampleBooks.add(new Book("Java Programming", "Alice", "111", "CS", 2019, "Pearson", "A1"));
            sampleBooks.add(new Book("Data Structures", "Bob", "222", "CS", 2021, "O'Reilly", "A2"));
            sampleBooks.add(new Book("Operating Systems", "Charlie", "333", "CS", 2020, "McGraw-Hill", "B1"));

            System.out.println("\n--- Welcome to Book Inventory Operations ---");
            BookOperations.handleUserInteraction(sampleBooks);

            while (true) {
                System.out.print("\nWould you like to start over? (yes/no): ");
                String again = scanner.nextLine().trim().toLowerCase();

                if (again.equals("yes")) {
                    break; // restart outer loop
                } else if (again.equals("no")) {
                    System.out.println("Thank you. Exiting program.");
                    scanner.close();
                    return;
                } else {
                    System.out.println("Invalid input. Please type 'yes' or 'no'.");
                }
            }
        }
    }
}


