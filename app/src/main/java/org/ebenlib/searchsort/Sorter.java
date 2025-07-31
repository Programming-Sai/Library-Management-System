package org.ebenlib.searchsort;


import org.ebenlib.ds.EbenLibComparator;
import org.ebenlib.ds.EbenLibList;

public class Sorter {
    public static <T> void mergeSort(EbenLibList<T> list, EbenLibComparator<T> comparator) {
        if (list.size() <= 1) return;

        int mid = list.size() / 2;
        EbenLibList<T> left = new EbenLibList<>(list.subList(0, mid));
        EbenLibList<T> right = new EbenLibList<>(list.subList(mid, list.size()));

        mergeSort(left, comparator);
        mergeSort(right, comparator);

        merge(list, left, right, comparator);
    }

    private static <T> void merge(EbenLibList<T> list, EbenLibList<T> left, EbenLibList<T> right, EbenLibComparator<T> comparator) {
        int i = 0, j = 0, k = 0;

        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) list.set(k++, left.get(i++));
        while (j < right.size()) list.set(k++, right.get(j++));
    }
}
