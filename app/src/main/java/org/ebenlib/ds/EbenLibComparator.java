package org.ebenlib.ds;

import java.util.Comparator;

/**
 * Functional interface for comparing two objects of type T.
 */
@FunctionalInterface
public interface EbenLibComparator<T> {
    /** Core abstract method. */
    int compare(T a, T b);

    /** Natural‑order comparator (requires T extends Comparable). */
    static <T extends Comparable<? super T>> EbenLibComparator<T> naturalOrder() {
        return (a, b) -> a.compareTo(b);
    }

    /** Reverse a given comparator’s order. */
    static <T> EbenLibComparator<T> reverseOrder(EbenLibComparator<T> cmp) {
        return (a, b) -> cmp.compare(b, a);
    }

    /** Chain this comparator with another to break ties. */
    default EbenLibComparator<T> thenComparing(EbenLibComparator<T> other) {
        return (a, b) -> {
            int res = this.compare(a, b);
            return (res != 0) ? res : other.compare(a, b);
        };
    }

    /** Extract a key and compare by its natural order. */
    static <T, U extends Comparable<? super U>> 
           EbenLibComparator<T> comparing(EbenLibFunction<T, U> keyExtractor) {
        return (a, b) -> keyExtractor.apply(a)
                                     .compareTo(keyExtractor.apply(b));
    }

    /** Extract a key and compare by a custom key comparator. */
    static <T, U> 
           EbenLibComparator<T> comparing(EbenLibFunction<T, U> keyExtractor,
                                          EbenLibComparator<U> keyComparator) {
        return (a, b) -> keyComparator.compare(
                               keyExtractor.apply(a),
                               keyExtractor.apply(b)
                           );
    }

        /**
     * Like comparing(keyExtractor, keyComparator), 
     * but accepts a java.util.Comparator for the key type.
     */
    static <T, U> EbenLibComparator<T> comparing(EbenLibFunction<T, U> keyExtractor,
                                                 Comparator<? super U> javaCmp) {
        return (a, b) -> javaCmp.compare(
            keyExtractor.apply(a),
            keyExtractor.apply(b)
        );
    }

    /** Reverse the order of _this_ comparator. */
    default EbenLibComparator<T> reversed() {
        return (a, b) -> this.compare(b, a);
    }
}
