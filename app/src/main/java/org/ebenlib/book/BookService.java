package org.ebenlib.book;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.utils.FileUtil;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookService {

    private final Path csvPath;

    public BookService(Path csvPath) {
        this.csvPath = csvPath;
        try {
            if (Files.notExists(csvPath)) {
                Files.createDirectories(csvPath.getParent());
                Files.createFile(csvPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize book storage: " + e.getMessage());
        }
    }

    // -------- Core Operations --------

    public List<Book> listAll() {
        return FileUtil.readCSV(csvPath, Book::fromCSV);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return listAll().stream()
                .filter(book -> book.getIsbn().equalsIgnoreCase(isbn))
                .findFirst();
    }

    public void add(Book book) {
        List<Book> books = listAll();
        Optional<Book> existing = findByIsbn(book.getIsbn());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Book with ISBN already exists: " + book.getIsbn());
        }
        books.add(book);
        FileUtil.writeCSV(csvPath, books, Book::toCSV);
    }

    public void update(Book updatedBook) {
        List<Book> books = listAll().stream()
                .map(book -> book.getIsbn().equalsIgnoreCase(updatedBook.getIsbn()) ? updatedBook : book)
                .collect(Collectors.toList());
        FileUtil.writeCSV(csvPath, books, Book::toCSV);
    }

    public boolean delete(String isbn) {
        List<Book> books = listAll();
        boolean removed = books.removeIf(book -> book.getIsbn().equalsIgnoreCase(isbn));
        if (removed) {
            FileUtil.writeCSV(csvPath, books, Book::toCSV);
        }
        return removed;
    }

    // -------- Modular Search & Sort System --------

    public List<Book> search(Function<Book, String> fieldExtractor, String query) {
        return listAll().stream()
                .filter(book -> fieldExtractor.apply(book).toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> sort(Comparator<Book> comparator, boolean ascending) {
        return listAll().stream()
                .sorted(ascending ? comparator : comparator.reversed())
                .collect(Collectors.toList());
    }

    // Specific searches using the modular system
    public List<Book> searchByTitle(String q) {
        return search(Book::getTitle, q);
    }

    public List<Book> searchByAuthor(String q) {
        return search(Book::getAuthor, q);
    }

    public List<Book> searchByCategory(String q) {
        return search(Book::getCategory, q);
    }

    // Example stat computation (stub)
    public BookStats stats(String isbn) {
        // This would involve borrowing history, which lives elsewhere
        return new BookStats(isbn, 10, 5);  // Stub data
    }

    public boolean isInStock(String title) {
        return findByTitle(title).map(Book::getQuantity).orElse(0) > 0;
    }

    public Optional<Book> findByTitle(String title) {
        return listAll().stream()
            .filter(book -> book.getTitle().equalsIgnoreCase(title))
            .findFirst();
    }


    private void save(List<Book> books) {
        FileUtil.writeCSV(csvPath, books, Book::toCSV);
    }


    public boolean decrementStock(String title) {
        List<Book> books = listAll();
        Optional<Book> opt = findByTitle(title);
        if (opt.isPresent()) {
            Book b = opt.get();
            if (b.getQuantity() > 0) {
                b.setQuantity(b.getQuantity() - 1);
                save(books);
                return true;
            }
        }
        return false;
    }

    public void incrementStock(String title, int qty) {
        List<Book> books = listAll();
        Optional<Book> opt = findByTitle(title);
        if (opt.isPresent()) {
            Book b = opt.get();
            b.setQuantity(b.getQuantity() + qty);
        } else {
            ConsoleUI.error("Book with title not found: " + title);
        }
        save(books);
    }

    public boolean existsByIsbn(String isbn) {
        return findByIsbn(isbn).isPresent();
    }

}
