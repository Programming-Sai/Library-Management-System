package org.ebenlib.book;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.cli.TablePrinter;
import org.ebenlib.ds.EbenLibList;
import org.ebenlib.ds.EbenLibMap;

import java.nio.file.Paths;

public class BookHandler {

    public static final BookService svc = new BookService(Paths.get("app", "src", "main", "resources", "books.csv"));

    public static void handle(String[] args, EbenLibMap<String,String> opts) {
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

    public static void handleAdd(EbenLibMap<String, String> o) {
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

    public static void handleUpdate(EbenLibMap<String,String> o) {
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

    public static void handleDelete(EbenLibMap<String,String> o) {
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
        EbenLibList<Book> all = svc.listAll();
        if (all.isEmpty()) {
            ConsoleUI.info("No books in catalog.");
            return;
        }
        // build rows: isbn, title, author, year, qty
        EbenLibList<String[]> rows = all.map(b -> new String[]{
                b.getIsbn(),
                b.getTitle(),
                b.getAuthor(),
                String.valueOf(b.getYear()),
                String.valueOf(b.getQuantity()),
                b.getShelf()
            });
        String[] headers = {"ISBN","Title","Author","Year","Qty", "Shelf"};
        int[] widths = {15, 30, 20, 6, 4, 8};
        TablePrinter.printHeader(headers, widths);
        TablePrinter.printTable(rows, 10, widths);
    }

    private static void handleSearch(EbenLibMap<String,String> o) {
        EbenLibList<Book> found = new EbenLibList<>();
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
            EbenLibList<String[]> rows = found.map(b -> new String[]{
                b.getIsbn(),
                b.getTitle(),
                b.getAuthor(),
                b.getShelf(),
                b.getCategory(),
                b.getPublisher(),
                String.valueOf(b.getYear()),
                String.valueOf(b.getQuantity())
            });
            TablePrinter.printTable(rows, widths.length*2, widths);
        }
    }

    private static void handleStats(EbenLibMap<String,String> o) {
        String isbn = o.get("isbn");
        if (isbn == null) {
            ConsoleUI.error("Missing --isbn for stats.");
            return;
        }
        svc.findByIsbn(isbn).ifPresentOrElse(book -> {
            // 'book' is a Book, so you can call getTitle() and getAuthor()
            BookStats stats = svc.stats(isbn, book.getTitle(), book.getAuthor());
            ConsoleUI.println(stats.toString(), ConsoleUI.CYAN);
        }, () -> {
            ConsoleUI.error("No book found with ISBN " + isbn);
        });
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
            case "title"   -> handleSearch(EbenLibMap.of("title", q));
            case "author"  -> handleSearch(EbenLibMap.of("author", q));
            case "category"-> handleSearch(EbenLibMap.of("category", q));
            default        -> ConsoleUI.error("Unknown field.");
        }
        ConsoleUI.pressEnterToContinue();
    }

    public static void interactiveUpdate() {
        String isbn = ConsoleUI.prompt("ISBN to update:");
        if (isbn == null || isbn.isBlank()) {
            ConsoleUI.error("ISBN is required.");
            return;
        }

        // Build a map of only the fields the user wants to change
        EbenLibMap<String, String> updates = EbenLibMap.empty();
        updates.put("isbn", isbn);

        // For each field: prompt with instructions to hit ENTER to skip
        String title = ConsoleUI.prompt("Title (leave blank to keep current):");
        if (title != null && !title.isBlank()) {
            updates.put("title", title);
        }

        String author = ConsoleUI.prompt("Author (leave blank to keep current):");
        if (author != null && !author.isBlank()) {
            updates.put("author", author);
        }

        String category = ConsoleUI.prompt("Category (leave blank to keep current):");
        if (category != null && !category.isBlank()) {
            updates.put("category", category);
        }

        String yearStr = ConsoleUI.prompt("Year (leave blank to keep current):");
        if (yearStr != null && !yearStr.isBlank()) {
            updates.put("year", yearStr);
        }

        String publisher = ConsoleUI.prompt("Publisher (leave blank to keep current):");
        if (publisher != null && !publisher.isBlank()) {
            updates.put("publisher", publisher);
        }

        String shelf = ConsoleUI.prompt("Shelf (leave blank to keep current):");
        if (shelf != null && !shelf.isBlank()) {
            updates.put("shelf", shelf);
        }

        String qtyStr = ConsoleUI.prompt("Quantity (leave blank to keep current):");
        if (qtyStr != null && !qtyStr.isBlank()) {
            updates.put("qty", qtyStr);
        }
        handleUpdate(updates);  // will prompt for missing fields?
        ConsoleUI.pressEnterToContinue();
    }

    public static void interactiveDelete() {
        String isbn = ConsoleUI.prompt("ISBN to delete:");
        handleDelete(EbenLibMap.of("isbn", isbn));
        ConsoleUI.pressEnterToContinue();
    }

    public static void interactiveStats() {
        String isbn = ConsoleUI.prompt("ISBN for stats:");
        handleStats(EbenLibMap.of("isbn", isbn));
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
