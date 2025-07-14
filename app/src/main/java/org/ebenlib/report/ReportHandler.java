package org.ebenlib.report;

import org.ebenlib.cli.ConsoleUI;
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
        int totalStock  = books.stream().mapToInt(Book::getQuantity).sum();

        // low‑stock threshold
        int threshold = BorrowSettings.lowStockThreshold; // repurposing settings or define BookSettings.lowStockThreshold
        List<Book> lowStock = books.stream()
            .filter(b->b.getQuantity() < threshold)
            .collect(Collectors.toList());

        // by category
        Map<String,Long> byCat = books.stream()
            .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));

        // most borrowed
        Map<String,Long> borrowCounts = borrowStore.listAll().stream()
            .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));
        List<Map.Entry<String,Long>> topBooks = borrowCounts.entrySet().stream()
            .sorted(Map.Entry.<String,Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        ConsoleUI.header("📚 Book Statistics");
        ConsoleUI.println(String.format("  Total Books              : %d", totalBooks), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Total copies in stock     : %d", totalStock), ConsoleUI.WHITE);
        ConsoleUI.println(String.format("  Low‑stock (< %d copies)  : %d", threshold, lowStock.size()), ConsoleUI.WHITE);

        ConsoleUI.println("\n  By Category:", ConsoleUI.BOLD);
        byCat.forEach((cat,cnt) ->
            ConsoleUI.println(String.format("    %-15s : %d", cat, cnt), ConsoleUI.WHITE)
        );

        ConsoleUI.println("\n  Top‑Borrowed Titles:", ConsoleUI.BOLD);
        if (topBooks.isEmpty()) {
            ConsoleUI.println("    (no borrow records)", ConsoleUI.DIM);
        } else {
            for (int i=0;i<topBooks.size();i++) {
                var e = topBooks.get(i);
                String isbn = e.getKey();
                String title = bookService.findByIsbn(isbn)
                          .map(Book::getTitle)
                          .orElse("(unknown title)");
                ConsoleUI.println(
                    String.format("    %d. %-10s — %dx", i+1, title, e.getValue()),
                    ConsoleUI.WHITE
                );
            }
        }
    }

    // ── BORROWS ────────────────────────────────────────────────────────────────────
    public static void borrowsReport() {
        List<BorrowRecord> recs = borrowStore.listAll();
        long total   = recs.size();
        long returned= recs.stream().filter(r->r.getStatus()==Status.RETURNED).count();
        long pending = recs.stream().filter(r->r.getStatus()==Status.PENDING).count();
        long approved= recs.stream().filter(r->r.getStatus()==Status.APPROVED && r.getReturnDate()==null).count();
        long overdue = recs.stream()
            .filter(r->r.getStatus()==Status.APPROVED)
            .filter(r-> {
                long days = ChronoUnit.DAYS.between(
                  r.getDecisionDate().plusDays(BorrowSettings.loanPeriodDays),
                  LocalDate.now());
                return days>0;
            })
            .count();

        // top borrowers
        Map<String,Long> byUser = recs.stream()
            .filter(r->r.getStatus()==Status.APPROVED || r.getStatus()==Status.RETURNED)
            .collect(Collectors.groupingBy(BorrowRecord::getUser, Collectors.counting()));
        List<Map.Entry<String,Long>> topUsers = byUser.entrySet().stream()
            .sorted(Map.Entry.<String,Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

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
            for (int i=0;i<topUsers.size();i++) {
                var e=topUsers.get(i);
                ConsoleUI.println(
                  String.format("    %d. %-12s — %d borrows", i+1, e.getKey(), e.getValue()),
                  ConsoleUI.WHITE
                );
            }
        }
    }
}
