
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        OverdueTracker tracker = new OverdueTracker();

        try {
            // Load data
            tracker.loadBorrowers("data/borrowers.txt");
            tracker.loadTransactions("data/transactions.txt");

            // Process overdue books and fines
            tracker.checkOverdues("data/overdue_report.txt");

            // Save updated fines
            tracker.saveUpdatedBorrowers("data/borrowers.txt");
            System.out.println("✔ Overdue check complete. Fines updated and report saved.");

        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
