package org.ebenlib.ds;


/**
 * A simple Set<T> abstraction: unique elements, no guaranteed order.
 */
public interface EbenLibSet<T> extends Iterable<T> {
    /** Adds the item if not already present. Returns true if added. */
    boolean add(T item);

    /** Removes the item if present. Returns true if removed. */
    boolean remove(T item);

    /** True if the set contains the item. */
    boolean contains(T item);

    /** Number of elements in the set. */
    int size();

    /** True if empty. */
    boolean isEmpty();

    /** Removes all elements. */
    void clear();
}
