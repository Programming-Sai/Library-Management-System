package org.ebenlib.book;

import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.searchsort.Searcher;
import org.ebenlib.searchsort.Sorter;
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
        List<Book> books = new ArrayList<>(listAll());
        Comparator<Book> comparator = Comparator.comparing(Book::getIsbn, String.CASE_INSENSITIVE_ORDER);
        Sorter.mergeSort(books, comparator);
        Book key = new Book(isbn, "", "", "", 0, "", "", 0); 
        int index = Searcher.binarySearch(books, key, comparator);
        return index >= 0 ? Optional.of(books.get(index)) : Optional.empty();
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
        List<Book> all = new ArrayList<>(listAll());
        List<Book> result = new ArrayList<>();

        for (Book book : all) {
            String fieldValue = fieldExtractor.apply(book);
            if (fieldValue != null && fieldValue.toLowerCase().contains(query.toLowerCase())) {
                result.add(book);
            }
        }

        // Optional: sort results alphabetically by title (for deterministic ordering)
        Sorter.mergeSort(result, Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return result;
    }


    public List<Book> sort(Comparator<Book> comparator, boolean ascending) {
        List<Book> books = new ArrayList<>(listAll());
        Comparator<Book> actualComparator = ascending ? comparator : comparator.reversed();
        Sorter.mergeSort(books, actualComparator);
        return books;
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
        List<Book> books = new ArrayList<>(listAll());
        Comparator<Book> comparator = Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        Sorter.mergeSort(books, comparator);
        Book key = new Book("", title, "", "", 0, "", "", 0); // dummy book with only title
        int index = Searcher.binarySearch(books, key, comparator);
        return index >= 0 ? Optional.of(books.get(index)) : Optional.empty();
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
