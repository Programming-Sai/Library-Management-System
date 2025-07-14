import java.util.*;

public class BookSorter {
    public static void mergeSort(List<Book> books, Comparator<Book> comparator) {
        if (books.size() <= 1) return;

        int mid = books.size() / 2;
        List<Book> left = new ArrayList<>(books.subList(0, mid));
        List<Book> right = new ArrayList<>(books.subList(mid, books.size()));

        mergeSort(left, comparator);
        mergeSort(right, comparator);

        merge(books, left, right, comparator);
    }

    private static void merge(List<Book> books, List<Book> left, List<Book> right, Comparator<Book> comparator) {
        int i = 0, j = 0, k = 0;

        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                books.set(k++, left.get(i++));
            } else {
                books.set(k++, right.get(j++));
            }
        }

        while (i < left.size()) books.set(k++, left.get(i++));
        while (j < right.size()) books.set(k++, right.get(j++));
    }
}