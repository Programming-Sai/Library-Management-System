import java.time.LocalDate;

public class Transaction implements Comparable<Transaction> {
    String isbn;
    String borrowerID;
    LocalDate borrowDate;
    LocalDate returnDate;
    boolean isReturned;

    public Transaction(String isbn, String borrowerID, LocalDate borrowDate, LocalDate returnDate, boolean isReturned) {
        this.isbn = isbn;
        this.borrowerID = borrowerID;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.isReturned = isReturned;
    }

    @Override
    public int compareTo(Transaction other) {
        return this.returnDate.compareTo(other.returnDate);
    }
}
