package org.ebenlib.searchsort;


import java.util.*;




public class Searcher {
    public static <T> int binarySearch(List<T> list, T target, Comparator<T> comparator) {
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

