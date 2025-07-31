package org.ebenlib.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A hash-based Set using separate chaining and our EbenLibList for buckets.
 */
@SuppressWarnings("unchecked")
public class EbenLibHashSet<T> implements EbenLibSet<T> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private EbenLibList<T>[] buckets;
    private int size = 0;

    public EbenLibHashSet() {
        initBuckets(DEFAULT_CAPACITY);
    }

    private void initBuckets(int capacity) {
        buckets = new EbenLibList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new EbenLibList<>();
        }
        size = 0;
    }

    private int bucketIndex(T item) {
        return (item == null ? 0 : Math.abs(item.hashCode())) % buckets.length;
    }

    private void maybeResize() {
        if (size + 1 > buckets.length * LOAD_FACTOR) {
            EbenLibList<T>[] old = buckets;
            initBuckets(buckets.length * 2);
            for (EbenLibList<T> bucket : old) {
                for (T item : bucket) {
                    add(item);
                }
            }
        }
    }

    /**
     * Creates a new set containing exactly the given items.
     */
    @SafeVarargs
    public static <T> EbenLibHashSet<T> of(T... items) {
        EbenLibHashSet<T> set = new EbenLibHashSet<>();
        for (T item : items) {
            set.add(item);
        }
        return set;
    }


    @Override
    public boolean add(T item) {
        if (contains(item)) return false;
        maybeResize();
        buckets[bucketIndex(item)].add(item);
        size++;
        return true;
    }

    @Override
    public boolean remove(T item) {
        EbenLibList<T> bucket = buckets[bucketIndex(item)];
        for (int i = 0; i < bucket.size(); i++) {
            T e = bucket.get(i);
            if ((e == null && item == null) || (e != null && e.equals(item))) {
                bucket.remove(i);
                size--;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(T item) {
        EbenLibList<T> bucket = buckets[bucketIndex(item)];
        for (T e : bucket) {
            if ((e == null && item == null) || (e != null && e.equals(item))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        initBuckets(DEFAULT_CAPACITY);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            int bucketIdx = 0;
            Iterator<T> listIter = buckets[0].iterator();

            @Override
            public boolean hasNext() {
                while (!listIter.hasNext() && bucketIdx < buckets.length - 1) {
                    bucketIdx++;
                    listIter = buckets[bucketIdx].iterator();
                }
                return listIter.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return listIter.next();
            }
        };
    }
}
