package org.ebenlib.book;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int year;
    private String publisher;
    private String shelf;
    private int quantity;

    public Book(String isbn, String title, String author, String category, int year, String publisher, String shelf, int quantity) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.year = year;
        this.publisher = publisher;
        this.shelf = shelf;
        this.quantity = quantity;
    }

    // --- Getters ---
    public String getIsbn()       { return isbn; }
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public String getCategory()   { return category; }
    public int getYear()          { return year; }
    public String getPublisher()  { return publisher; }
    public String getShelf()      { return shelf; }
    public int getQuantity()      { return quantity; }

    // --- Setters ---
    public void setIsbn(String isbn)             { this.isbn = isbn; }
    public void setTitle(String title)           { this.title = title; }
    public void setAuthor(String author)         { this.author = author; }
    public void setCategory(String category)     { this.category = category; }
    public void setYear(int year)                { this.year = year; }
    public void setPublisher(String publisher)   { this.publisher = publisher; }
    public void setShelf(String shelf)           { this.shelf = shelf; }
    public void setQuantity(int quantity)        { this.quantity = quantity; }

    @Override
    public String toString() {
        return String.format("Book[ISBN=%s, Title=%s, Author=%s, Category=%s, Year=%d, Publisher=%s, Shelf=%s, Quantity=%d]",
                isbn, title, author, category, year, publisher, shelf, quantity);
    }

    // Optional: CSV serialization
    public String toCSV() {
        return String.join(",",
            escape(isbn), escape(title), escape(author), escape(category),
            String.valueOf(year), escape(publisher), escape(shelf),
            String.valueOf(quantity));
    }

    public static Book fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        if (parts.length < 8) throw new IllegalArgumentException("Invalid CSV line: " + csvLine);
        return new Book(
            unescape(parts[0]), unescape(parts[1]), unescape(parts[2]), unescape(parts[3]),
            Integer.parseInt(parts[4]), unescape(parts[5]), unescape(parts[6]),
            Integer.parseInt(parts[7])
        );
    }

    private static String escape(String s) {
        return s.replace(",", "\\,");
    }

    private static String unescape(String s) {
        return s.replace("\\,", ",");
    }
}
