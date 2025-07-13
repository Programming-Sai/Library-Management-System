
public class Book {
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String year;
    private String publisher;
    private String shelfLocation;

    public Book(String title, String author, String isbn, String category,
                String year, String publisher, String shelfLocation) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.year = year;
        this.publisher = publisher;
        this.shelfLocation = shelfLocation;
    }

    public String getIsbn() {
        return isbn;
    }

    public String toFileString() {
        return title + "," + author + "," + isbn + "," + category + "," + year + "," + publisher + "," + shelfLocation;
    }

    @Override
    public String toString() {
        return "\"" + title + "\" by " + author + " (ISBN: " + isbn + ", Year: " + year + ", Publisher: " + publisher + ", Shelf: " + shelfLocation + ")";
    }
}
