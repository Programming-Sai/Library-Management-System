import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class BookService {
    private String csvFile = "books.csv";

    public static class Book {
        String isbn;
        String title;
        boolean isBorrowed;
        String borrowerId;
        LocalDate dueDate;

        public Book(String isbn, String title, boolean isBorrowed,
                    String borrowerId, LocalDate dueDate) {
            this.isbn = isbn;
            this.title = title;
            this.isBorrowed = isBorrowed;
            this.borrowerId = borrowerId;
            this.dueDate = dueDate;
        }

        public String toCSVRow() {
            return isbn + "," + title + "," + isBorrowed + "," +
                   (borrowerId != null ? borrowerId : "") + "," +
                   (dueDate != null ? dueDate : "");
        }

        public static Book fromCSVRow(String line) {
            String[] parts = line.split(",", -1);
            String isbn = parts[0];
            String title = parts[1];
            boolean isBorrowed = Boolean.parseBoolean(parts[2]);
            String borrowerId = parts[3].isEmpty() ? null : parts[3];
            LocalDate dueDate = parts[4].isEmpty() ? null : LocalDate.parse(parts[4]);
            return new Book(isbn, title, isBorrowed, borrowerId, dueDate);
        }
    }

    // Load all books from CSV
    private List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        File file = new File(csvFile);
        if (!file.exists()) return books;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                books.add(Book.fromCSVRow(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading books.csv");
        }
        return books;
    }

    // Save all books to CSV
    private void saveBooks(List<Book> books) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            for (Book book : books) {
                writer.write(book.toCSVRow());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving books.csv");
        }
    }

    // Check availability
    public boolean isAvailable(String isbn) {
        for (Book b : loadBooks()) {
            if (b.isbn.equals(isbn)) {
                return !b.isBorrowed;
            }
        }
        return false;
    }

    // Mark book as borrowed
    public boolean markAsBorrowed(String isbn, String borrowerId, LocalDate dueDate) {
        List<Book> books = loadBooks();
        for (Book b : books) {
            if (b.isbn.equals(isbn)) {
                if (b.isBorrowed) return false;
                b.isBorrowed = true;
                b.borrowerId = borrowerId;
                b.dueDate = dueDate;
                saveBooks(books);
                return true;
            }
        }
        return false;
    }

    // Mark book as returned
    public boolean markAsReturned(String isbn) {
        List<Book> books = loadBooks();
        for (Book b : books) {
            if (b.isbn.equals(isbn)) {
                b.isBorrowed = false;
                b.borrowerId = null;
                b.dueDate = null;
                saveBooks(books);
                return true;
            }
        }
        return false;
    }
}
