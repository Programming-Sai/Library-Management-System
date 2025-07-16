import java.time.LocalDate;

public class Transaction {
    private String transactionId;
    private String borrowerId;
    private String isbn;
    private LocalDate dateBorrowed;
    private LocalDate dueDate;
    private LocalDate dateReturned; // null if not returned yet
    private double finePaid;

    public Transaction(String transactionId, String borrowerId, String isbn,
                       LocalDate dateBorrowed, LocalDate dueDate,
                       LocalDate dateReturned, double finePaid) {
        this.transactionId = transactionId;
        this.borrowerId = borrowerId;
        this.isbn = isbn;
        this.dateBorrowed = dateBorrowed;
        this.dueDate = dueDate;
        this.dateReturned = dateReturned;
        this.finePaid = finePaid;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getBorrowerId() { return borrowerId; }
    public String getIsbn() { return isbn; }
    public LocalDate getDateBorrowed() { return dateBorrowed; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getDateReturned() { return dateReturned; }
    public double getFinePaid() { return finePaid; }

    public void setDateReturned(LocalDate dateReturned) {
        this.dateReturned = dateReturned;
    }

    public void setFinePaid(double finePaid) {
        this.finePaid = finePaid;
    }

    // Convert object to CSV line
    public String toCSVRow() {
        return transactionId + "," +
               borrowerId + "," +
               isbn + "," +
               dateBorrowed + "," +
               dueDate + "," +
               (dateReturned != null ? dateReturned.toString() : "") + "," +
               finePaid;
    }

    // Load from CSV line
    public static Transaction fromCSVRow(String row) {
        String[] parts = row.split(",", -1); // keep empty fields
        String transactionId = parts[0];
        String borrowerId = parts[1];
        String isbn = parts[2];
        LocalDate dateBorrowed = LocalDate.parse(parts[3]);
        LocalDate dueDate = LocalDate.parse(parts[4]);
        LocalDate dateReturned = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
        double finePaid = Double.parseDouble(parts[6]);

        return new Transaction(transactionId, borrowerId, isbn,
                               dateBorrowed, dueDate, dateReturned, finePaid);
    }
}
