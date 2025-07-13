package org.ebenlib.book;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.cli.TablePrinter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BookHandler {

    public static final BookService svc = new BookService(Paths.get("app", "src", "main", "resources", "books.csv"));

    public static void handle(String[] args, Map<String,String> opts) {
        if (args.length < 2) {
            printHelp();
            return;
        }
        switch (args[1]) {
            case "add":     handleAdd(opts);    break;
            case "update":  handleUpdate(opts); break;
            case "delete":  handleDelete(opts); break;
            case "list":    handleList();       break;
            case "search":  handleSearch(opts); break;
            case "stats":   handleStats(opts);  break;
            // case "interactive":
                // interactiveMenu();
                // break;
            default:
                printHelp();
        }
    }

    public static void printHelp() {
        ConsoleUI.header("Book commands");
        ConsoleUI.println("  book add       --isbn ... --title ... --author ... --year ... --publisher ... --category ... --shelf ... --qty ...", ConsoleUI.WHITE);
        ConsoleUI.println("  book update    --isbn ... [--title ...] [--author ...] [...]", ConsoleUI.WHITE);
        ConsoleUI.println("  book delete    --isbn ...", ConsoleUI.WHITE);
        ConsoleUI.println("  book list", ConsoleUI.WHITE);
        ConsoleUI.println("  book search    --title ... | --author ... | --category ...", ConsoleUI.WHITE);
        ConsoleUI.println("  book stats     --isbn ...", ConsoleUI.WHITE);
    }

    // — Non‑interactive handlers —

    public static void handleAdd(Map<String, String> o) {
        try {
            String isbn = o.get("isbn");
            String title = o.get("title");
            String author = o.get("author");

            // Validate required fields
            if (isbn == null || title == null || author == null) {
                ConsoleUI.error("Missing required fields: --isbn, --title, --author");
                return;
            }

            // Check for duplicates
            if (svc.findByIsbn(isbn).isPresent()) {
                ConsoleUI.error("Book with this ISBN already exists: " + isbn);
                return;
            }

            int year = safeParseInt(o.get("year"), 1000, "Invalid year format. Defaulting to 1000");
            int qty = safeParseInt(o.get("qty"), 1, "Invalid quantity format. Defaulting to 1");

            Book b = new Book(
                isbn,
                title,
                author,
                o.getOrDefault("category", ""),
                year,
                o.getOrDefault("publisher", ""),
                o.getOrDefault("shelf", ""),
                qty
            );
            // If book exists, increase qty instead of duplicating
            if (svc.existsByIsbn(isbn)) {
                svc.incrementStock(title, qty);
                ConsoleUI.success("Stock updated.");
            } else {
                svc.add(b);
            }
            ConsoleUI.success("Book added: " + b.getTitle());
        } catch (Exception e) {
            ConsoleUI.error("Failed to add book: " + e.getMessage());
        }
    }

    public static void handleUpdate(Map<String,String> o) {
        String isbn = o.get("isbn");
        if (isbn == null) {
            ConsoleUI.error("Missing --isbn for update.");
            return;
        }

        svc.findByIsbn(isbn).ifPresentOrElse(existing -> {
            try {
                Book updated = new Book(
                    isbn,
                    o.getOrDefault("title", existing.getTitle()),
                    o.getOrDefault("author", existing.getAuthor()),
                    o.getOrDefault("category", existing.getCategory()),
                    safeParseInt(o.get("year"), existing.getYear(), "Invalid year."),
                    o.getOrDefault("publisher", existing.getPublisher()),
                    o.getOrDefault("shelf", existing.getShelf()),
                    safeParseInt(o.get("qty"), existing.getQuantity(), "Invalid quantity.")
                );
                svc.update(updated);
                ConsoleUI.success("Book updated: " + isbn);
            } catch (Exception e) {
                ConsoleUI.error("Failed to update: " + e.getMessage());
            }
        }, () -> ConsoleUI.error("No book found with ISBN " + isbn));
    }

    public static void handleDelete(Map<String,String> o) {
        String isbn = o.get("isbn");
        if (isbn == null) {
            ConsoleUI.error("Missing --isbn for delete.");
            return;
        }
        if (svc.delete(isbn)) {
            ConsoleUI.success("Deleted book: " + isbn);
        } else {
            ConsoleUI.error("No book found with ISBN " + isbn);
        }
    }

    public static void handleList() {
        List<Book> all = svc.listAll();
        if (all.isEmpty()) {
            ConsoleUI.info("No books in catalog.");
            return;
        }
        // build rows: isbn, title, author, year, qty
        List<String[]> rows = all.stream()
            .map(b -> new String[]{
                b.getIsbn(),
                b.getTitle(),
                b.getAuthor(),
                String.valueOf(b.getYear()),
                String.valueOf(b.getQuantity()),
                b.getShelf()
            })
            .collect(Collectors.toList());
        String[] headers = {"ISBN","Title","Author","Year","Qty", "Shelf"};
        int[] widths = {15, 30, 20, 6, 4, 8};
        TablePrinter.printHeader(headers, widths);
        TablePrinter.printTable(rows, 10, widths);
    }

    private static void handleSearch(Map<String,String> o) {
        List<Book> found = Collections.emptyList();
        if (o.containsKey("title")) {
            found = svc.searchByTitle(o.get("title"));
        } else if (o.containsKey("author")) {
            found = svc.searchByAuthor(o.get("author"));
        } else if (o.containsKey("category")) {
            found = svc.searchByCategory(o.get("category"));
        } else {
            ConsoleUI.error("Specify --title, --author or --category");
            return;
        }
        if (found.isEmpty()) {
            ConsoleUI.info("No matches found.");
        } else {
            String[] headers = {"ISBN","Title","Author","Shelf", "Category", "Publisher","Year","Qty"};
            int[]   widths  = {15,30,20,8, 15, 20, 6,4};
            TablePrinter.printHeader(headers, widths);
            List<String[]> rows = found.stream()
            .map(b -> new String[]{
                b.getIsbn(), b.getTitle(), b.getAuthor(),
                b.getShelf(), b.getCategory(), b.getPublisher(), String.valueOf(b.getYear()),
                String.valueOf(b.getQuantity())
            }).collect(Collectors.toList());
            TablePrinter.printTable(rows, widths.length*2, widths);
        }
    }

    private static void handleStats(Map<String,String> o) {
        String isbn = o.get("isbn");
        if (isbn == null) {
            ConsoleUI.error("Missing --isbn for stats.");
            return;
        }
        BookStats stats = svc.stats(isbn);
        ConsoleUI.println(stats.toString(), ConsoleUI.CYAN);
    }

    public static void interactiveAdd() {
        try {
            String isbn = ConsoleUI.prompt("ISBN:");
            if (svc.findByIsbn(isbn).isPresent()) {
                ConsoleUI.error("A book with this ISBN already exists.");
                return;
            }

            String title = ConsoleUI.prompt("Title:");
            String author = ConsoleUI.prompt("Author:");
            String category = ConsoleUI.prompt("Category:");
            int year = safePromptInt("Year:", 1000);
            String pub = ConsoleUI.prompt("Publisher:");
            String shelf = ConsoleUI.prompt("Shelf:");
            int qty = safePromptInt("Quantity:", 1);

            var b = new Book(isbn, title, author, category, year, pub, shelf, qty);
            // If book exists, increase qty instead of duplicating
            if (svc.existsByIsbn(isbn)) {
                svc.incrementStock(title, qty);
                ConsoleUI.success("Stock updated.");
            } else {
                svc.add(b);
            }
            ConsoleUI.success("Added.");
        } catch (Exception e) {
            ConsoleUI.error("Failed to add book: " + e.getMessage());
        }
    }

    public static void interactiveSearch() {
        String field = ConsoleUI.prompt("Search by (title/author/category):").toLowerCase();
        String q     = ConsoleUI.prompt("Query:");
        switch (field) {
            case "title"   -> handleSearch(Map.of("title", q));
            case "author"  -> handleSearch(Map.of("author", q));
            case "category"-> handleSearch(Map.of("category", q));
            default        -> ConsoleUI.error("Unknown field.");
        }
        ConsoleUI.pressEnterToContinue();
    }

    public static void interactiveUpdate() {
        String isbn = ConsoleUI.prompt("ISBN to update:");
        handleUpdate(Map.of("isbn", isbn));  // will prompt for missing fields?
        ConsoleUI.pressEnterToContinue();
    }

    public static void interactiveDelete() {
        String isbn = ConsoleUI.prompt("ISBN to delete:");
        handleDelete(Map.of("isbn", isbn));
        ConsoleUI.pressEnterToContinue();
    }

    public static void interactiveStats() {
        String isbn = ConsoleUI.prompt("ISBN for stats:");
        handleStats(Map.of("isbn", isbn));
        ConsoleUI.pressEnterToContinue();
    }

    private static int safeParseInt(String val, int fallback, String errMsg) {
        try {
            return val != null ? Integer.parseInt(val) : fallback;
        } catch (NumberFormatException e) {
            ConsoleUI.warning(errMsg);
            return fallback;
        }
    }

    private static int safePromptInt(String prompt, int fallback) {
        String input = ConsoleUI.prompt(prompt);
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            ConsoleUI.warning("Invalid number. Using default: " + fallback);
            return fallback;
        }
    }

}
