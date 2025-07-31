package org.ebenlib.searchsort;



import org.ebenlib.ds.EbenLibComparator;
import org.ebenlib.ds.EbenLibList;




public class Searcher {
    public static <T> int binarySearch(EbenLibList<T> list, T target, EbenLibComparator<T> comparator) {
        int low = 0, high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = comparator.compare(list.get(mid), target);

            if (cmp == 0) return mid;
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }

        return -1;
    }
}

