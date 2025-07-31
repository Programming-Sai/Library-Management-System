package org.ebenlib.book;

import org.ebenlib.borrow.BorrowStore;
import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.ds.EbenLibComparator;
import org.ebenlib.ds.EbenLibList;
import org.ebenlib.searchsort.Searcher;
import org.ebenlib.searchsort.Sorter;
import org.ebenlib.utils.FileUtil;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public class BookService {

    private final Path csvPath;
    private final BorrowStore borrowStore;

    public BookService(Path csvPath) {
        this.csvPath     = csvPath;
        this.borrowStore = new BorrowStore(Paths.get("app","src","main","resources","borrows.csv"));
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

    public EbenLibList<Book> listAll() {
        return FileUtil.readCSV(csvPath, Book::fromCSV);
    }

    public Optional<Book> findByIsbn(String isbn) {
        EbenLibList<Book> books = new EbenLibList<>(listAll());
        EbenLibComparator<Book> comparator = EbenLibComparator.comparing(Book::getIsbn, String.CASE_INSENSITIVE_ORDER);
        Sorter.mergeSort(books, comparator);
        Book key = new Book(isbn, "", "", "", 0, "", "", 0); 
        int index = Searcher.binarySearch(books, key, comparator);
        return index >= 0 ? Optional.of(books.get(index)) : Optional.empty();
    }


    public void add(Book book) {
        EbenLibList<Book> books = listAll();
        Optional<Book> existing = findByIsbn(book.getIsbn());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Book with ISBN already exists: " + book.getIsbn());
        }
        books.add(book);
        FileUtil.writeCSV(csvPath, books, Book::toCSV);
    }

    public void update(Book updatedBook) {
        EbenLibList<Book> books = listAll().map(book ->
        book.getIsbn().equalsIgnoreCase(updatedBook.getIsbn())
            ? updatedBook
            : book
        );
        FileUtil.writeCSV(csvPath, books, Book::toCSV);
    }

    public boolean delete(String isbn) {
        EbenLibList<Book> books = listAll();
        boolean removed = books.removeIf(book -> book.getIsbn().equalsIgnoreCase(isbn));
        if (removed) {
            FileUtil.writeCSV(csvPath, books, Book::toCSV);
        }
        return removed;
    }

    // -------- Modular Search & Sort System --------

    public EbenLibList<Book> search(Function<Book, String> fieldExtractor, String query) {
        EbenLibList<Book> all = new EbenLibList<>(listAll());
        EbenLibList<Book> result = new EbenLibList<>();

        for (Book book : all) {
            String fieldValue = fieldExtractor.apply(book);
            if (fieldValue != null && fieldValue.toLowerCase().contains(query.toLowerCase())) {
                result.add(book);
            }
        }

        // Optional: sort results alphabetically by title (for deterministic ordering)
        Sorter.mergeSort(result, EbenLibComparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return result;
    }


    public EbenLibList<Book> sort(EbenLibComparator<Book> comparator, boolean ascending) {
        EbenLibList<Book> books = new EbenLibList<>(listAll());
        EbenLibComparator<Book> actualComparator = ascending ? comparator : comparator.reversed();
        Sorter.mergeSort(books, actualComparator);
        return books;
    }


    // Specific searches using the modular system
    public EbenLibList<Book> searchByTitle(String q) {
        return search(Book::getTitle, q);
    }

    public EbenLibList<Book> searchByAuthor(String q) {
        return search(Book::getAuthor, q);
    }

    public EbenLibList<Book> searchByCategory(String q) {
        return search(Book::getCategory, q);
    }

    // Example stat computation (stub)
    public BookStats stats(String isbn, String title, String author) {
        return BookStats.compute(isbn, title, author, this, borrowStore);
    }

    public boolean isInStock(String title) {
        return findByTitle(title).map(Book::getQuantity).orElse(0) > 0;
    }

    public Optional<Book> findByTitle(String title) {
        EbenLibList<Book> books = new EbenLibList<>(listAll());
        EbenLibComparator<Book> comparator = EbenLibComparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        Sorter.mergeSort(books, comparator);
        Book key = new Book("", title, "", "", 0, "", "", 0); // dummy book with only title
        int index = Searcher.binarySearch(books, key, comparator);
        return index >= 0 ? Optional.of(books.get(index)) : Optional.empty();
    }



    private void save(EbenLibList<Book> books) {
        FileUtil.writeCSV(csvPath, books, Book::toCSV);
    }


    public boolean decrementStock(String title) {
        EbenLibList<Book> books = listAll();
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
        EbenLibList<Book> books = listAll();
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
