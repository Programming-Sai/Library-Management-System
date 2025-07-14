import java.util.*;

public class BookSearcher {
    public static int binarySearch(List<Book> books, Book target, Comparator<Book> comparator) {
        int low = 0;
        int high = books.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = comparator.compare(books.get(mid), target);

            if (cmp == 0) return mid;
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }

        return -1;
    }
}
