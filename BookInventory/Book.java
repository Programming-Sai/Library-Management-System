public class Book {
    String title;
    String author;
    String isbn;
    String category;
    int year;
    String publisher;
    String shelfLocation;

    public Book(String title, String author, String isbn, String category, int year, String publisher, String shelfLocation) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.year = year;
        this.publisher = publisher;
        this.shelfLocation = shelfLocation;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + year + "), ISBN: " + isbn +
               ", Category: " + category + ", Publisher: " + publisher +
               ", Shelf: " + shelfLocation;
    }
}