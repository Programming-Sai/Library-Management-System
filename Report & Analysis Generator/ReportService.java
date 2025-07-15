import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ReportService {
    private final String transactionsFile = "transactions.csv";

    // Minimal Transaction class clone for compatibility
    static class Transaction {
        private String transactionId;
        private String borrowerId;
        private String isbn;
        private LocalDate dateBorrowed;
        private LocalDate dueDate;
        private LocalDate dateReturned;
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

        public String getTransactionId() { return transactionId; }
        public String getBorrowerId() { return borrowerId; }
        public String getIsbn() { return isbn; }
        public LocalDate getDateBorrowed() { return dateBorrowed; }
        public LocalDate getDueDate() { return dueDate; }
        public LocalDate getDateReturned() { return dateReturned; }
        public double getFinePaid() { return finePaid; }

        public static Transaction fromCSVRow(String line) {
            String[] parts = line.split(",", -1);
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

    // Load all transactions from the CSV file
    private List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(transactionsFile);
        if (!file.exists()) return transactions;

        try (BufferedReader reader = new BufferedReader(new FileReader(transactionsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Transaction tx = Transaction.fromCSVRow(line);
                transactions.add(tx);
            }
        } catch (IOException e) {
            System.out.println("Error reading transactions.csv: " + e.getMessage());
        }
        return transactions;
    }

    // Save report as a text file and open it
    private void saveReport(String fileName, String content) {
        try {
            File file = new File(fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();

            System.out.println("✅ Report saved: " + file.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            System.out.println("Error saving report: " + e.getMessage());
        }
    }

    // Generate the "Most Borrowed Books" report
    public void generateMostBorrowedBooksReport() {
        List<Transaction> transactions = loadTransactions();
        Map<String, Integer> borrowCounts = new HashMap<>();

        for (Transaction tx : transactions) {
            String isbn = tx.getIsbn();
            borrowCounts.put(isbn, borrowCounts.getOrDefault(isbn, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(borrowCounts.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        StringBuilder report = new StringBuilder();
        report.append("📘 Most Borrowed Books Report\n");
        report.append("================================\n");
        report.append(String.format("%-20s | %s\n", "ISBN", "Times Borrowed"));
        report.append("----------------------------------------\n");

        for (Map.Entry<String, Integer> entry : sorted) {
            report.append(String.format("%-20s | %d\n", entry.getKey(), entry.getValue()));
        }

        saveReport("most_borrowed_books.txt", report.toString());
    }

    // 📊 Report: Top Borrowers
    public void generateTopBorrowersReport() {
        List<Transaction> transactions = loadTransactions();
        Map<String, Integer> borrowerCounts = new HashMap<>();

        for (Transaction tx : transactions) {
            String borrowerId = tx.getBorrowerId();
            borrowerCounts.put(borrowerId, borrowerCounts.getOrDefault(borrowerId, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(borrowerCounts.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        StringBuilder report = new StringBuilder();
        report.append("👤 Top Borrowers Report\n");
        report.append("=======================\n\n");
        report.append(String.format("%-15s | %s\n", "Borrower ID", "Books Borrowed"));
        report.append("--------------------------------\n");

        for (Map.Entry<String, Integer> entry : sorted) {
            report.append(String.format("%-15s | %d\n", entry.getKey(), entry.getValue()));
        }

        saveReport("top_borrowers.txt", report.toString());
    }

    // 💰 Report: Highest Fines Paid
    public void generateHighestFinesPaidReport() {
        List<Transaction> transactions = loadTransactions();
        Map<String, Double> fineTotals = new HashMap<>();

        for (Transaction tx : transactions) {
            String borrowerId = tx.getBorrowerId();
            double fine = tx.getFinePaid();
            fineTotals.put(borrowerId, fineTotals.getOrDefault(borrowerId, 0.0) + fine);
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(fineTotals.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        StringBuilder report = new StringBuilder();
        report.append("💰 Highest Fines Paid Report\n");
        report.append("==================================\n\n");
        report.append(String.format("%-15s | %s\n", "Borrower ID", "Total Fines (GHS)"));
        report.append("----------------------------------------\n");

        for (Map.Entry<String, Double> entry : sorted) {
            report.append(String.format("%-15s | %.2f\n", entry.getKey(), entry.getValue()));
        }

        saveReport("highest_fines_paid.txt", report.toString());
    }

    // 📅 Report: Borrowing Trends Per Month
    public void generateBorrowingTrendsReport() {
        List<Transaction> transactions = loadTransactions();
        Map<String, Integer> borrowCounts = new TreeMap<>(); // Keeps months in order

        for (Transaction tx : transactions) {
            LocalDate date = tx.getDateBorrowed();
            if (date != null) {
                String yearMonth = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                borrowCounts.put(yearMonth, borrowCounts.getOrDefault(yearMonth, 0) + 1);
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("📅 Borrowing Trends Report (Monthly)\n");
        report.append("====================================\n\n");
        report.append(String.format("%-10s | %s\n", "Month", "Books Borrowed"));
        report.append("-------------------------------\n");

        for (Map.Entry<String, Integer> entry : borrowCounts.entrySet()) {
            report.append(String.format("%-10s | %d\n", entry.getKey(), entry.getValue()));
        }

        saveReport("borrowing_trends.txt", report.toString());
    }

}
