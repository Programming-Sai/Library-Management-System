import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LendingService {
    private List<Transaction> transactions;
    private String csvFilePath = "transactions.csv";

    public LendingService() {
        transactions = loadTransactions();
    }

    // Load existing transactions from the CSV file
    private List<Transaction> loadTransactions() {
        List<Transaction> result = new ArrayList<>();
        File file = new File(csvFilePath);
        if (!file.exists()) {
            return result; // return empty list
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(Transaction.fromCSVRow(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading transactions.csv: " + e.getMessage());
        }

        return result;
    }

    // Save all transactions to the CSV file
    public void saveTransactions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            for (Transaction tx : transactions) {
                writer.write(tx.toCSVRow());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions.csv: " + e.getMessage());
        }
    }

    // Generate the next transaction ID
    private String generateTransactionId() {
        int max = 0;
        for (Transaction tx : transactions) {
            String id = tx.getTransactionId();
            try {
                int num = Integer.parseInt(id.substring(1));
                if (num > max) {
                    max = num;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return String.format("T%03d", max + 1);
    }

    // Borrow a book
    public void borrowBook(String borrowerId, String isbn) {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14); // 2-week loan

        String newId = generateTransactionId();

        Transaction newTx = new Transaction(
            newId,
            borrowerId,
            isbn,
            today,
            dueDate,
            null,
            0.0
        );

        transactions.add(newTx);
        saveTransactions();

        System.out.println("Book borrowed successfully! Due on: " + dueDate);
    }

    // Return a book
    public void returnBook(String borrowerId, String isbn) {
        for (Transaction tx : transactions) {
            if (tx.getBorrowerId().equals(borrowerId) &&
                tx.getIsbn().equals(isbn) &&
                tx.getDateReturned() == null) {

                LocalDate today = LocalDate.now();
                tx.setDateReturned(today);

                // Simple fine calculation: GH₵0.50 per day late
                double fine = 0.0;
                if (today.isAfter(tx.getDueDate())) {
                    long daysLate = java.time.temporal.ChronoUnit.DAYS.between(tx.getDueDate(), today);
                    fine = daysLate * 0.5;
                }

                tx.setFinePaid(fine);
                saveTransactions();

                System.out.println("Book returned.");
                System.out.println("Fine: GH₵" + fine);
                return;
            }
        }

        System.out.println("No matching borrowed book found.");
    }

    // View all transactions (for debugging)
    public void printAllTransactions() {
        for (Transaction tx : transactions) {
            System.out.println(tx.toCSVRow());
        }
    }
}
