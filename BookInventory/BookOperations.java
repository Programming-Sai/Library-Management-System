import java.util.*;

public class BookOperations {
    public static void handleUserInteraction(List<Book> books) {
        if (books == null || books.isEmpty()) {
            System.out.println("No books available to operate on.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        boolean keepRunning = true;
        while (keepRunning) {
            try {
                System.out.println("\nBook Operations:");
                System.out.println("1. Sort Books");
                System.out.println("2. Search for a Book");
                System.out.println("3. Exit to Main Menu");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        sortBooks(books, scanner);
                        break;
                    case "2":
                        searchBooks(books, scanner);
                        break;
                    case "3":
                        keepRunning = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again.");
                scanner.nextLine(); // clear input buffer
            }
        }
    }

    private static void sortBooks(List<Book> books, Scanner scanner) {
        while (true) {
            try {
                System.out.println("\nSort by:");
                System.out.println("1. Title");
                System.out.println("2. Author");
                System.out.println("3. Year");
                System.out.print("Choose a field: ");
                String field = scanner.nextLine();

                Comparator<Book> comparator;
                switch (field) {
                    case "1":
                        comparator = Comparator.comparing(b -> b.title, String.CASE_INSENSITIVE_ORDER);
                        break;
                    case "2":
                        comparator = Comparator.comparing(b -> b.author, String.CASE_INSENSITIVE_ORDER);
                        break;
                    case "3":
                        comparator = Comparator.comparingInt(b -> b.year);
                        break;
                    default:
                        System.out.println("Invalid field. Please enter 1, 2, or 3.");
                        continue;
                }

                BookSorter.mergeSort(books, comparator);
                System.out.println("\nSorted Books:");
                books.forEach(System.out::println);
                break;
            } catch (Exception e) {
                System.out.println("Error during sorting. Please try again.");
                scanner.nextLine();
            }
        }
    }

    private static void searchBooks(List<Book> books, Scanner scanner) {
        while (true) {
            try {
                System.out.println("\nSearch by:");
                System.out.println("1. Title");
                System.out.println("2. ISBN");
                System.out.print("Choose a field: ");
                String field = scanner.nextLine();

                Comparator<Book> comparator;
                Book target;

                switch (field) {
                    case "1":
                        comparator = Comparator.comparing(b -> b.title, String.CASE_INSENSITIVE_ORDER);
                        System.out.print("Enter title to search: ");
                        target = new Book(scanner.nextLine(), "", "", "", 0, "", "");
                        break;
                    case "2":
                        comparator = Comparator.comparing(b -> b.isbn, String.CASE_INSENSITIVE_ORDER);
                        System.out.print("Enter ISBN to search: ");
                        target = new Book("", "", scanner.nextLine(), "", 0, "", "");
                        break;
                    default:
                        System.out.println("Invalid search field. Please enter 1 or 2.");
                        continue;
                }

                BookSorter.mergeSort(books, comparator);
                int index = BookSearcher.binarySearch(books, target, comparator);

                if (index != -1) {
                    System.out.println("\nBook Found:");
                    System.out.println(books.get(index));
                } else {
                    System.out.println("\nBook not found.");
                }
                break;
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again.");
                scanner.nextLine();
            }
        }
    }
}