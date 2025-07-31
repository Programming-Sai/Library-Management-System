package org.ebenlib.book;

import java.time.LocalDate;

import org.ebenlib.borrow.BorrowRecord;
import org.ebenlib.borrow.BorrowSettings;
import org.ebenlib.borrow.BorrowStore;
import org.ebenlib.borrow.Status;


public class BookStats {
    private final String   title;
    private final String   author;
    private final String   isbn;
    private final int      copiesLeft;
    private final long     totalTimesBorrowed;
    private final long     currentlyCheckedOut;
    private final double   totalFines;
    private final LocalDate lastBorrowedDate;
    private final long     overdueCount;
    private final long     pendingApprovalCount;

    private BookStats(String isbn,
                      String title,
                      String author,
                      int copiesLeft,
                      long totalTimesBorrowed,
                      long currentlyCheckedOut,
                      double totalFines,
                      LocalDate lastBorrowedDate,
                      long overdueCount,
                      long pendingApprovalCount) {
        this.title                  = title;
        this.author                 = author;
        this.isbn                   = isbn;
        this.copiesLeft             = copiesLeft;
        this.totalTimesBorrowed     = totalTimesBorrowed;
        this.currentlyCheckedOut    = currentlyCheckedOut;
        this.totalFines             = totalFines;
        this.lastBorrowedDate       = lastBorrowedDate;
        this.overdueCount           = overdueCount;
        this.pendingApprovalCount   = pendingApprovalCount;
    }

    /** Compute all metrics for the given ISBN. */
    public static BookStats compute(String isbn, String title, String author, BookService svc, BorrowStore store) {
        Book b = svc.findByIsbn(isbn).orElseThrow(() -> new RuntimeException("No such book: " + isbn));

        int copiesLeft  = getCopiesLeft(b, store);
        long totalB     = getTotalTimesBorrowed(isbn, store);
        long currOut    = getCurrentlyCheckedOutCount(isbn, store);
        double fines    = getTotalFinesForBook(isbn, store);
        LocalDate last  = getLastBorrowedDate(isbn, store);
        long overdue    = getOverdueCount(isbn, store);
        long pending    = getPendingApprovalCount(isbn, store);

        return new BookStats(
            isbn,
            title,
            author,
            copiesLeft,
            totalB,
            currOut,
            fines,
            last,
            overdue,
            pending
        );
    }

    public String getIsbn() { return isbn; }


    @Override
 public String toString() {
        var sb = new StringBuilder();
        sb.append("Stats for ISBN ").append(isbn).append(":\n")
          .append("  Title                : ").append(title).append("\n")
          .append("  Author               : ").append(author).append("\n")
          .append("  Copies available     : ").append(copiesLeft).append("\n")
          .append("  Total times borrowed : ").append(totalTimesBorrowed).append("\n")
          .append("  Currently checked out: ").append(currentlyCheckedOut).append("\n")
          .append("  Overdue loans        : ").append(overdueCount).append("\n")
          .append("  Pending approvals    : ").append(pendingApprovalCount).append("\n")
          .append("  Total fines accrued  : ₵").append(String.format("%.2f", totalFines)).append("\n")
          .append("  Last borrowed date   : ")
            .append(lastBorrowedDate == null ? "-" : lastBorrowedDate).append("\n");
        return sb.toString();
    }

        /** 1) Copies currently available = total copies – currently checked out (approved). */
    public static int getCopiesLeft(Book b, BorrowStore borrowStore) {
        int total = b.getQuantity();
        int checkedOut = 0;
        for (BorrowRecord r : borrowStore.listAll()) {
            if (b.getIsbn().equals(r.getBookId())
                && r.getStatus() == Status.APPROVED) {
                checkedOut++;
            }
        }
        return total - checkedOut;
    }

    /** 2) Total times this ISBN was ever borrowed (approved, returned, pending). */
    public static long getTotalTimesBorrowed(String isbn, BorrowStore borrowStore) {
        long count = 0;
        for (BorrowRecord r : borrowStore.listAll()) {
            System.out.println(r.getBookId());
            if (isbn.equals(r.getBookId())) {
                count++;
            }
        }
        return count;
    }

    /** 3) Total approved-and-not-yet-returned = current outstanding loans. */
    public static long getCurrentlyCheckedOutCount(String isbn, BorrowStore borrowStore) {
        long count = 0;
        for (BorrowRecord r : borrowStore.listAll()) {
            if (isbn.equals(r.getBookId())
                && r.getStatus() == Status.APPROVED) {
                count++;
            }
        }
        return count;
    }

    /** 4) Sum of all fines ever accrued on this ISBN. */
    public static double getTotalFinesForBook(String isbn, BorrowStore borrowStore) {
        double sum = 0.0;
        for (BorrowRecord r : borrowStore.listAll()) {
            if (isbn.equals(r.getBookId())) {
                sum += r.getFineOwed();
            }
        }
        return sum;
    }

    /** 5) Last time this book was borrowed (max requestDate). */
    public static LocalDate getLastBorrowedDate(String isbn, BorrowStore borrowStore) {
        LocalDate last = null;
        for (BorrowRecord r : borrowStore.listAll()) {
            if (isbn.equals(r.getBookId())) {
                LocalDate d = r.getRequestDate();
                if (last == null || d.isAfter(last)) {
                    last = d;
                }
            }
        }
        return last;
    }

    /** 6) How many current APPROVED loans are overdue (i.e. past due date). */
    public static long getOverdueCount(String isbn, BorrowStore borrowStore) {
        long count = 0;
        LocalDate today = LocalDate.now();
        for (BorrowRecord r : borrowStore.listAll()) {
            if (isbn.equals(r.getBookId())
                && r.getStatus() == Status.APPROVED
                && r.getDecisionDate() != null) {
                LocalDate due = r.getDecisionDate().plusDays(BorrowSettings.loanPeriodDays);
                if (due.isBefore(today)) {
                    count++;
                }
            }
        }
        return count;
    }

    /** 7) How many loans are PENDING approval (waiting to be approved). */
    public static long getPendingApprovalCount(String isbn, BorrowStore borrowStore) {
        long count = 0;
        for (BorrowRecord r : borrowStore.listAll()) {
            if (isbn.equals(r.getBookId())
                && r.getStatus() == Status.PENDING) {
                count++;
            }
        }
        return count;
    }


    
}
