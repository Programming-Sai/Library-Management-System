import java.io.*;
import java.util.*;

public class BookInventoryManager {
    private HashMap<String, List<Book>> inventory = new HashMap<>();
    private final String FILE_NAME = "books.txt";
    private Scanner scanner = new Scanner(System.in);

    public BookInventoryManager() {
        loadFromFile();
    }

    public void addBook() {
        String title = promptNonEmpty("Title");
        String author = promptValidated("Author", "^[A-Za-z\\s]+$", "Author name must contain only letters and spaces.");
        String isbn = promptValidated("ISBN", "^[0-9]{10,13}$", "ISBN must be 10 to 13 digits.");

        if (bookExists(isbn)) {
            System.out.println(" Book with this ISBN already exists.");
            return;
        }

        String category = promptNonEmpty("Category");
        String year = promptValidated("Year", "^(19|20)\\d{2}$", "Enter a valid year (e.g., 1999, 2022).");

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (Integer.parseInt(year) > currentYear) {
            System.out.println(" Year cannot be in the future.");
            return;
        }

        String publisher = promptNonEmpty("Publisher");
        String shelf = promptValidated("Shelf Location", "^Shelf\\s\\d+$", "Shelf must follow format like 'Shelf 1'.");

        Book book = new Book(title, author, isbn, category, year, publisher, shelf);

        inventory.putIfAbsent(category, new ArrayList<>());
        inventory.get(category).add(book);

        System.out.println(" Book added successfully.");
    }

    public void removeBook() {
        System.out.print("Enter ISBN of book to remove: ");
        String isbn = scanner.nextLine().trim();
        boolean found = false;

        for (String category : inventory.keySet()) {
            List<Book> books = inventory.get(category);
            Iterator<Book> iterator = books.iterator();

            while (iterator.hasNext()) {
                Book book = iterator.next();
                if (book.getIsbn().equals(isbn)) {
                    iterator.remove();
                    System.out.println(" Book removed successfully.");
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        if (!found) {
            System.out.println(" Book not found.");
        }
    }

    public void listBooks() {
        if (inventory.isEmpty()) {
            System.out.println("No books in the inventory.");
            return;
        }

        for (String category : inventory.keySet()) {
            System.out.println("\n Category: " + category);
            for (Book book : inventory.get(category)) {
                System.out.println("   - " + book);
            }
        }
    }

    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (String category : inventory.keySet()) {
                for (Book book : inventory.get(category)) {
                    writer.println(book.toFileString());
                }
            }
            System.out.println(" Books saved to file.");
        } catch (IOException e) {
            System.out.println(" Error saving to file: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println(" No existing data. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    Book book = new Book(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                    inventory.putIfAbsent(parts[3], new ArrayList<>());
                    inventory.get(parts[3]).add(book);
                }
            }
            System.out.println(" Books loaded from file.");
        } catch (IOException e) {
            System.out.println(" Error loading from file: " + e.getMessage());
        }
    }

    public void inventoryMenu() {
        while (true) {
            System.out.println("\n===== Book Inventory Menu =====");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. List Books");
            System.out.println("4. Save & Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addBook(); break;
                case "2": removeBook(); break;
                case "3": listBooks(); break;
                case "4": saveToFile(); return;
                default: System.out.println(" Invalid choice.");
            }
        }
    }

    // --- Helper Methods Below ---

    private String promptNonEmpty(String fieldName) {
        String input;
        do {
            System.out.print(fieldName + ": ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(" " + fieldName + " cannot be empty.");
            }
        } while (input.isEmpty());
        return input;
    }

    private String promptValidated(String fieldName, String regex, String errorMsg) {
        String input;
        while (true) {
            System.out.print(fieldName + ": ");
            input = scanner.nextLine().trim();
            if (input.matches(regex)) {
                return input;
            }
            System.out.println(" Invalid " + fieldName + ". " + errorMsg);
        }
    }

    private boolean bookExists(String isbn) {
        for (List<Book> books : inventory.values()) {
            for (Book book : books) {
                if (book.getIsbn().equals(isbn)) {
                    return true;
                }
            }
        }
        return false;
    }
}
