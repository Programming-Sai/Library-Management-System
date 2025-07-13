package org.ebenlib.book;

public class BookStats {
    private final String isbn;
    private final int totalBorrows;
    private final int currentlyBorrowed;

    public BookStats(String isbn, int totalBorrows, int currentlyBorrowed) {
        this.isbn = isbn;
        this.totalBorrows = totalBorrows;
        this.currentlyBorrowed = currentlyBorrowed;
    }

    public String getIsbn() { return isbn; }
    public int getTotalBorrows() { return totalBorrows; }
    public int getCurrentlyBorrowed() { return currentlyBorrowed; }

    @Override
    public String toString() {
        return "BookStats{" +
               "isbn='" + isbn + '\'' +
               ", totalBorrows=" + totalBorrows +
               ", currentlyBorrowed=" + currentlyBorrowed +
               '}';
    }
}
