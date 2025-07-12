package org.ebenlib.borrow;

import org.ebenlib.book.Book;
import org.ebenlib.book.BookService;
import org.ebenlib.cli.AuthHandler;
import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.cli.TablePrinter;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BorrowHandler {

    public static final BorrowStore store = new BorrowStore(Paths.get("app", "src","main","resources","borrows.csv"));
    private static final BookService bookService = new BookService(Paths.get("app", "src", "main", "resources", "books.csv"));
    

    static {
        store.load();
        store.refreshAllFines();
    }

    public static void handle(String[] args, Map<String,String> opts) {
        if (args.length < 2) {
            printHelp();
            return;
        }
        switch (args[1]) {
            case "request":      handleRequest(opts);      break;
            case "approve":      handleApprove(opts);      break;
            case "reject":       handleReject(opts);       break;
            case "return":       handleReturn(opts);       break;
            case "list":         handleList(opts);         break;
            case "history":      handleHistory(opts, false);break;
            case "all-history":  handleHistory(opts, true); break;
            // case "interactive":  interactiveMenu();        break;
            default: printHelp();
        }
        store.save();
    }

    public static void printHelp() {
        ConsoleUI.header("Borrow commands");
        ConsoleUI.println("  borrow request --book-id ...          Request to borrow a book", ConsoleUI.WHITE);
        ConsoleUI.println("  borrow approve --id ...               Approve a pending request (librarian)", ConsoleUI.WHITE);
        ConsoleUI.println("  borrow reject --id ...                Reject a pending request (librarian)", ConsoleUI.WHITE);
        ConsoleUI.println("  borrow return --id ...                Mark an approved borrow as returned", ConsoleUI.WHITE);
        ConsoleUI.println("  borrow list [--status PENDING|APPROVED]   List requests by status", ConsoleUI.WHITE);
        ConsoleUI.println("  borrow history                        View your borrow history", ConsoleUI.WHITE);
        ConsoleUI.println("  borrow all-history                    View all users’ history (librarian)", ConsoleUI.WHITE);
        // ConsoleUI.println("  borrow interactive                    Enter interactive borrow menu", ConsoleUI.WHITE);
    }

    // — Non‑interactive —

    public static void handleRequest(Map<String,String> o) {
        String user   = AuthHandler.getCurrentUser().getUsername(); 
        double outstanding = store.getTotalFineForUser(user);
        if (outstanding > BorrowSettings.fineBlockThreshold) {
            ConsoleUI.error(String.format(
                "Outstanding fines $%.2f exceed allowed $%.2f — request denied",
                outstanding, BorrowSettings.fineBlockThreshold
            ));
            return;
        }
        String bookId = o.get("book-id");
        if (bookId == null) {
            ConsoleUI.error("Missing --book-id");
            return;
        }
        Optional<Book> book = bookService.findByIsbn(bookId);
        if (book.isEmpty()) {
            ConsoleUI.error("No such book with ID " + bookId);
            return;
        }
        int reqId = store.addRequest(user, bookId);
        ConsoleUI.success("Request submitted (ID=" + reqId + ")");
    }

    public static void handleApprove(Map<String,String> o) {
        int id = parseId(o.get("id"), "approve");
        if (store.updateStatus(id, Status.APPROVED)) {
            ConsoleUI.success("Request #" + id + " approved");
        } else {
            ConsoleUI.error("No pending request with ID " + id);
        }
    }

    public static void handleReject(Map<String,String> o) {
        int id = parseId(o.get("id"), "reject");
        if (store.updateStatus(id, Status.REJECTED)) {
            ConsoleUI.success("Request #" + id + " rejected");
        } else {
            ConsoleUI.error("No pending request with ID " + id);
        }
    }

    public static void handleReturn(Map<String,String> o) {
        int id = parseId(o.get("id"), "return");
        if (store.updateStatus(id, Status.RETURNED)) {
            ConsoleUI.success("Request #" + id + " marked returned");
        } else {
            ConsoleUI.error("No approved borrow with ID " + id);
        }
    }

    public static void handleList(Map<String,String> o) {
        Status st = o.containsKey("status")
            ? Status.valueOf(o.get("status").toUpperCase())
            : Status.PENDING;
        List<BorrowRecord> recs = store.listByStatus(st);
        renderTable(recs);
    }

    public static void handleHistory(Map<String,String> o, boolean allUsers) {
        List<BorrowRecord> recs = allUsers
            ? store.listAll()
            : store.listByUser(AuthHandler.getCurrentUser().getUsername());
        renderTable(recs);
    }

    public static int parseId(String raw, String cmd) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing or invalid --id for " + cmd);
        }
    }

    public static void renderTable(List<BorrowRecord> recs) {
        if (recs.isEmpty()) {
            ConsoleUI.info("No records to display.");
            return;
        }
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
        List<String[]> rows = recs.stream()
            .map(r -> new String[] {
                String.valueOf(r.getId()),
                r.getUser(),
                r.getBookId(),
                r.getDecisionDate() == null ? "" : r.getDecisionDate().format(df),
                r.getStatus().name(),
                String.format("%.2f", r.getFineOwed())
            }).collect(Collectors.toList());
        String[] hdr = {"ID","User","Book","Decided","Status","Fine"};
        int[] widths = {4,15,10,12,10,6};
        TablePrinter.printHeader(hdr, widths);
        TablePrinter.printTable(rows, 10, widths);
    }


    public static void interactiveRequest() {
        ConsoleUI.header("Search and request a book");

        String query = ConsoleUI.prompt("Search book by title (leave blank to list all): ");
        List<Book> books = query.isBlank()
            ? bookService.listAll()
            : bookService.searchByTitle(query);

        if (books.isEmpty()) {
            ConsoleUI.warning("No books found.");
            return;
        }

        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            ConsoleUI.println("  " + (i + 1) + ". " + b.getTitle() + " (ISBN: " + b.getIsbn() + ", Qty: " + b.getQuantity() + ")", ConsoleUI.WHITE);
        }
        ConsoleUI.println("  0. Cancel", ConsoleUI.DIM);

        int choice = ConsoleUI.promptInt("Select a book to request:", 0, books.size());
        if (choice == 0) {
            ConsoleUI.info("Request cancelled.");
            return;
        }

        Book selected = books.get(choice - 1);
        String bookId = selected.getIsbn();

        handleRequest(Map.of("book-id", bookId));
        store.save();
        ConsoleUI.pressEnterToContinue();
    }


    public static void interactiveApproveReject() {
        List<BorrowRecord> pending = store.listByStatus(Status.PENDING);

        if (pending.isEmpty()) {
            ConsoleUI.warning("No pending requests.");
            return;
        }

        ConsoleUI.header("Pending Requests");
        for (int i = 0; i < pending.size(); i++) {
            BorrowRecord r = pending.get(i);
            Book b = bookService.findByIsbn(r.getBookId()).orElse(null);
            String title = (b != null) ? b.getTitle() : "(Unknown Title)";
            ConsoleUI.println("  " + (i + 1) + ". ID: " + r.getId() + " | " + title + " | ISBN: " + r.getBookId() + " | Date: " + r.getRequestDate(), ConsoleUI.WHITE);

        }

        int choice = ConsoleUI.promptInt("Select request to approve/reject (0 to cancel): ");
        if (choice < 1 || choice > pending.size()) return;

        BorrowRecord selected = pending.get(choice - 1);

        String action = ConsoleUI.prompt("Approve (a) or Reject (r)? ");
        if (action.equalsIgnoreCase("a")) {
            // Optional: check book exists and quantity > 0
            boolean ok = store.updateStatus(selected.getId(), Status.APPROVED);
            if (ok) ConsoleUI.success("Request approved.");
        } else if (action.equalsIgnoreCase("r")) {
            store.updateStatus(selected.getId(), Status.REJECTED);
            ConsoleUI.success("Request rejected.");
        }
        store.save();
        ConsoleUI.pressEnterToContinue();
    }

    public static void handleReturnInteractive() {
        String currentUser = AuthHandler.getCurrentUser().getUsername();

        List<BorrowRecord> borrowed = store.listByUser(currentUser).stream()
            .filter(r -> r.getStatus() == Status.APPROVED)
            .collect(Collectors.toList());

        if (borrowed.isEmpty()) {
            ConsoleUI.info("You have no approved books to return.");
            return;
        }

        for (int i = 0; i < borrowed.size(); i++) {
            BorrowRecord r = borrowed.get(i);
            Book b = bookService.findByIsbn(r.getBookId()).orElse(null);
            String title = (b != null) ? b.getTitle() : "(Unknown Title)";
            ConsoleUI.println("  " + (i + 1) + ". ID: " + r.getId() + " | " + title + " | ISBN: " + r.getBookId() + " | Date: " + r.getRequestDate(), ConsoleUI.WHITE);
        }
        ConsoleUI.println("  0. Cancel", ConsoleUI.DIM);

        int choice = ConsoleUI.promptInt("Select a request to return:", 0, borrowed.size());
        if (choice == 0) {
            ConsoleUI.info("Return cancelled.");
            return;
        }

        BorrowRecord selected = borrowed.get(choice - 1);
        String id = String.valueOf(selected.getId());

        handleReturn(Map.of("id", id));
        store.save();
        ConsoleUI.pressEnterToContinue();
    }

    /** Show top debtors — statistically in “stats” section */
    public static void showTopDebtors(int topN) {
        Map<String, Double> fines = new HashMap<>();
        store.listAll().stream()
            .filter(r -> r.getStatus() == Status.APPROVED)
            .forEach(r -> fines.merge(r.getUser(), r.getFineOwed(), Double::sum));

        PriorityQueue<Map.Entry<String,Double>> pq = new PriorityQueue<>(
            (a,b) -> Double.compare(b.getValue(), a.getValue())
        );
        pq.addAll(fines.entrySet());

        ConsoleUI.header("📈 Top Debtors");
        for (int i = 1; i <= topN && !pq.isEmpty(); i++) {
            var e = pq.poll();
            ConsoleUI.println(String.format("%d. %s – $%.2f", i, e.getKey(), e.getValue()),
                              ConsoleUI.WHITE);
        }
    }

    
}
