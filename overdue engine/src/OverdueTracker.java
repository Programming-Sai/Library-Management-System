import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class OverdueTracker {
    private final PriorityQueue<Transaction> transactionQueue = new PriorityQueue<>();
    private final Map<String, Borrower> borrowerMap = new HashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int GRACE_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 1.0;

    public void loadBorrowers(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length >= 3) {
                    String id = parts[0];
                    String name = parts[1];
                    double fine = Double.parseDouble(parts[2]);
                    borrowerMap.put(id, new Borrower(id, name, fine));
                }
            }
        }
    }

    public void loadTransactions(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length >= 5) {
                    String isbn = parts[0];
                    String borrowerID = parts[1];
                    LocalDate borrowDate = LocalDate.parse(parts[2], formatter);
                    LocalDate returnDate = LocalDate.parse(parts[3], formatter);
                    boolean isReturned = Boolean.parseBoolean(parts[4]);
                    if (!isReturned) {
                        transactionQueue.add(new Transaction(isbn, borrowerID, borrowDate, returnDate, false));
                    }
                }
            }
        }
    }

    public void checkOverdues(String reportFilePath) throws IOException {
        LocalDate today = LocalDate.now();
        StringBuilder report = new StringBuilder("Overdue Report - " + today + "\n\n");

        while (!transactionQueue.isEmpty()) {
            Transaction t = transactionQueue.poll();
            LocalDate dueDate = t.returnDate.plusDays(GRACE_PERIOD_DAYS);

            if (today.isAfter(dueDate)) {
                long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                double fine = overdueDays * FINE_PER_DAY;

                Borrower b = borrowerMap.get(t.borrowerID);
                if (b != null) {
                    b.addFine(fine);
                    report.append("ISBN: ").append(t.isbn)
                          .append(" | Borrower: ").append(b.name)
                          .append(" | Days Overdue: ").append(overdueDays)
                          .append(" | Fine: GHS ").append(String.format("%.2f", fine))
                          .append("\n");
                }
            }
        }

        // Show report in console
        System.out.println(report);

        // Save report to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFilePath))) {
            writer.write(report.toString());
        }
    }

    public void saveUpdatedBorrowers(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Borrower b : borrowerMap.values()) {
                writer.write(b.toString());
                writer.newLine();
            }
        }
    }
}
