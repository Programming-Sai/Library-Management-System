# 📊 EbenLib Library Management System  
## 🧾 Report & Analysis Generator 

This module provides reporting and analytics for the EbenLib Library Management System. It analyzes transactions from lending and return activities and generates human-readable reports in `.txt` format using only Java’s built-in libraries. All reports are compatible with the `transactions.csv` file managed by the LendingService.

---

## Input Format: transactions.csv

Each line in the file follows the format:

transactionId,borrowerId,isbn,dateBorrowed,dueDate,dateReturned,finePaid

---

## 📦 Features – ReportService.java

The `ReportService` class contains four fully working report methods:

### 1. generateMostBorrowedBooksReport()
- Counts how many times each book (by ISBN) was borrowed.
- Output file: `most_borrowed_books.txt`

### 2. generateTopBorrowersReport()
- Identifies which borrowers (by borrower ID) borrowed the most books.
- Output file: `top_borrowers.txt`

### 3. generateHighestFinesPaidReport()
- Aggregates total fines paid by each borrower.
- Output file: `highest_fines_paid.txt`

### 4. generateBorrowingTrendsReport() (Bonus)
- Groups borrowing activity by month (e.g., 2025-07 → 3 books).
- Output file: `borrowing_trends.txt`

Each method:
- Reads from `transactions.csv`
- Analyzes the data
- Generates a formatted `.txt` report
- Automatically opens it using `Desktop.getDesktop().open(...)` if supported

---

## How to Use / Test the Reports

1. Ensure you have the following files in the same folder:
   - `ReportService.java`
   - `transactions.csv`
   - `TestReport.java` (shown below)

2. TestReport.java:

```java
public class TestReport {
    public static void main(String[] args) {
        ReportService report = new ReportService();

        report.generateMostBorrowedBooksReport();
        report.generateTopBorrowersReport();
        report.generateHighestFinesPaidReport();
        report.generateBorrowingTrendsReport();
    }
}


🧾 Summary
The ReportService module fulfills the full responsibilities of the Report & Analysis in the EbenLib Library System. It enables librarians to generate and view key operational insights — including book popularity, borrower activity, overdue fines, and borrowing trends — using offline .txt reports built from structured transaction data.