package org.ebenlib.report;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.searchsort.Sorter;
import org.ebenlib.user.UserStore;
import org.ebenlib.book.BookService;
import org.ebenlib.book.Book;
import org.ebenlib.borrow.BorrowStore;
import org.ebenlib.borrow.BorrowRecord;
import org.ebenlib.borrow.Status;
import org.ebenlib.borrow.BorrowSettings;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ReportHandler {
    private static final UserStore   userStore   = new UserStore();
    private static final BookService bookService = new BookService(Paths.get("app","src","main","resources","books.csv"));
    private static final BorrowStore borrowStore= new BorrowStore(Paths.get("app","src","main","resources","borrows.csv"));

    static {
        userStore.load();
        borrowStore.load();
    }

    /** Entry point for CLI: report [view|users|books|borrows] */
    public static void handle(String[] args, Map<String,String> opts) {
        if (args.length < 2) {
            printHelp(); return;
        }
        switch(args[1].toLowerCase()) {
          case "view"    -> fullReport();
          case "users"   -> usersReport();
          case "books"   -> booksReport();
          case "borrows" -> borrowsReport();
          default         -> printHelp();
        }
    }

    private static void printHelp() {
        ConsoleUI.header("Report commands");
        ConsoleUI.println("  report view       Full summary report", ConsoleUI.WHITE);
        ConsoleUI.println("  report users      User statistics",    ConsoleUI.WHITE);
        ConsoleUI.println("  report books      Book inventory stats",ConsoleUI.WHITE);
        ConsoleUI.println("  report borrows    Borrowing activity",  ConsoleUI.WHITE);
    }

    // ── FULL REPORT ───────────────────────────────────────────────────────────────
    public static void fullReport() {
        ConsoleUI.clearScreen();
        ConsoleUI.header("📊 Library Full Report");

        usersReport();
        ConsoleUI.println("", ConsoleUI.RESET);
        booksReport();
        ConsoleUI.println("", ConsoleUI.RESET);
        borrowsReport();
    }

    // ── USERS ─────────────────────────────────────────────────────────────────────
    public static void usersReport() {
        List<org.ebenlib.user.User> all = userStore.listAll();
        long total     = all.size();
        long active    = all.stream().filter(u->u.isActive()).count();
        long suspended = total - active;

        ConsoleUI.header("👥 User Statistics");
        ConsoleUI.println(String.format("  Total users    : %d", total), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Active users   : %d", active), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Suspended users: %d", suspended), ConsoleUI.WHITE);

        // Recent 5 users
        List<String> recent = all.stream()
            .map(u->u.getUsername())
            .skip(Math.max(0, total-5))
            .collect(Collectors.toList());
        if (!recent.isEmpty()) {
            ConsoleUI.println("  Recent users   : " + String.join(", ", recent), ConsoleUI.WHITE);
        }
    }

    // ── BOOKS ─────────────────────────────────────────────────────────────────────  
    public static void booksReport() {
        List<Book> books = bookService.listAll();
        long totalBooks = books.size();

        int totalStock = 0;
        for (Book b : books) {
            totalStock += b.getQuantity();
        }

        // low‑stock threshold
        int threshold = BorrowSettings.lowStockThreshold;
        List<Book> lowStock = new ArrayList<>();
        for (Book b : books) {
            if (b.getQuantity() < threshold) {
                lowStock.add(b);
            }
        }

        // by category
        Map<String, Long> byCat = new HashMap<>();
        for (Book b : books) {
            String cat = b.getCategory();
            byCat.put(cat, byCat.getOrDefault(cat, 0L) + 1);
        }

        // most borrowed
        Map<String, Long> borrowCounts = new HashMap<>();
        List<BorrowRecord> borrows = borrowStore.listAll();
        for (BorrowRecord r : borrows) {
            String isbn = r.getBookId();
            borrowCounts.put(isbn, borrowCounts.getOrDefault(isbn, 0L) + 1);
        }

        List<Map.Entry<String, Long>> topBooks = new ArrayList<>(borrowCounts.entrySet());
        Comparator<Map.Entry<String, Long>> cmp = Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder());
        Sorter.mergeSort(topBooks, cmp);

        if (topBooks.size() > 5) {
            topBooks = topBooks.subList(0, 5);
        }

        ConsoleUI.header("📚 Book Statistics");
        ConsoleUI.println(String.format("  Total Books              : %d", totalBooks), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Total copies in stock     : %d", totalStock), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Low-stock (< %d copies)  : %d", threshold, lowStock.size()), ConsoleUI.WHITE);

        ConsoleUI.println("\n  By Category:", ConsoleUI.BOLD);
        for (Map.Entry<String, Long> entry : byCat.entrySet()) {
            ConsoleUI.println(String.format("    %-15s : %d", entry.getKey(), entry.getValue()), ConsoleUI.WHITE);
        }

        ConsoleUI.println("\n  Top‑Borrowed Titles:", ConsoleUI.BOLD);
        if (topBooks.isEmpty()) {
            ConsoleUI.println("    (no borrow records)", ConsoleUI.DIM);
        } else {
            for (int i = 0; i < topBooks.size(); i++) {
                var e = topBooks.get(i);
                String isbn = e.getKey();
                String title = bookService.findByIsbn(isbn)
                        .map(Book::getTitle)
                        .orElse("(unknown title)");
                ConsoleUI.println(
                    String.format("    %d. %-10s — %dx", i + 1, title, e.getValue()),
                    ConsoleUI.WHITE
                );
            }
        }
    }


    // ── BORROWS ────────────────────────────────────────────────────────────────────  
    public static void borrowsReport() {
        List<BorrowRecord> recs = borrowStore.listAll();
        long total = recs.size();
        long returned = 0, pending = 0, approved = 0, overdue = 0;

        for (BorrowRecord r : recs) {
            Status status = r.getStatus();
            if (status == Status.RETURNED) returned++;
            else if (status == Status.PENDING) pending++;
            else if (status == Status.APPROVED) {
                approved++;
                LocalDate due = r.getDecisionDate().plusDays(BorrowSettings.loanPeriodDays);
                long days = ChronoUnit.DAYS.between(due, LocalDate.now());
                if (days > 0) overdue++;
            }
        }

        // top borrowers
        Map<String, Long> userCounts = new HashMap<>();
        for (BorrowRecord r : recs) {
            if (r.getStatus() == Status.APPROVED || r.getStatus() == Status.RETURNED) {
                String user = r.getUser();
                userCounts.put(user, userCounts.getOrDefault(user, 0L) + 1);
            }
        }

        List<Map.Entry<String, Long>> topUsers = new ArrayList<>(userCounts.entrySet());
        Comparator<Map.Entry<String, Long>> cmp = Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder());
        Sorter.mergeSort(topUsers, cmp);
        if (topUsers.size() > 5) {
            topUsers = topUsers.subList(0, 5);
        }

        ConsoleUI.header("📦 Borrowing Activity");
        ConsoleUI.println(String.format("  Total requests        : %d", total), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Returned              : %d", returned), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Pending               : %d", pending), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Approved (outstanding): %d", approved), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Overdue               : %d", overdue), ConsoleUI.WHITE);

        ConsoleUI.println("\n  Top‑Borrowers:", ConsoleUI.BOLD);
        if (topUsers.isEmpty()) {
            ConsoleUI.println("    (no borrow records)", ConsoleUI.DIM);
        } else {
            for (int i = 0; i < topUsers.size(); i++) {
                var e = topUsers.get(i);
                ConsoleUI.println(
                    String.format("    %d. %-12s — %d borrows", i + 1, e.getKey(), e.getValue()),
                    ConsoleUI.WHITE
                );
            }
        }
        // ✅ HIGHEST TOTAL FINES
        Map<String, Double> fines = new HashMap<>();
        for (BorrowRecord r : recs) {
            String user = r.getUser();
            double f = r.getFineOwed();
            if (f > 0) fines.put(user, fines.getOrDefault(user, 0.0) + f);
        }

        List<Map.Entry<String, Double>> topFines = new ArrayList<>(fines.entrySet());
        Comparator<Map.Entry<String, Double>> comparator = Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder());
        Sorter.mergeSort(topFines, comparator);
        if (topFines.size() > 5) topFines = topFines.subList(0, 5); 

        ConsoleUI.println("\n  Top Outstanding Fines:", ConsoleUI.BOLD);
        if (topFines.isEmpty()) {
            ConsoleUI.println("    (no outstanding fines)", ConsoleUI.DIM);
        } else {
            for (int i = 0; i < topFines.size(); i++) {
                var e = topFines.get(i);
                ConsoleUI.println(
                    String.format("    %d. %-12s — ₵%.2f", i + 1, e.getKey(), e.getValue()),
                    ConsoleUI.WHITE
                );
            }
        }

        // ✅ BORROWING TRENDS (by request date)
        Map<LocalDate, Long> byDate = recs.stream()
            .collect(Collectors.groupingBy(BorrowRecord::getRequestDate, TreeMap::new, Collectors.counting()));

        ConsoleUI.println("\n  Borrowing Trends (by Date):", ConsoleUI.BOLD);
        if (byDate.isEmpty()) {
            ConsoleUI.println("    (no borrowing activity)", ConsoleUI.DIM);
        } else {
            for (Map.Entry<LocalDate, Long> entry : byDate.entrySet()) {
                ConsoleUI.println(
                    String.format("    %s — %d request(s)", entry.getKey(), entry.getValue()),
                    ConsoleUI.WHITE
                );
            }
        }
    }

}
