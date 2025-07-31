package org.ebenlib.ds;

import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * A simple hash-table based map implementation from scratch.
 *
 * @param <K> key type
 * @param <V> value type
 */
@SuppressWarnings("unchecked")
public class EbenLibMap<K, V> implements Iterable<EbenLibMapEntry<K, V>> {
    private static final int INITIAL_CAPACITY = 16;
    private final boolean ordered;
    private EbenLibList<K> insertionOrder;
    private static final double LOAD_FACTOR = 0.75;

    private EbenLibList<EbenLibMapEntry<K, V>>[] buckets;
    private int size = 0;


    public EbenLibMap() {
        this(false, null);
    }



    /** Constructs a map that uses the given EbenLibComparator for ordering keys if ordered=true. */
    public EbenLibMap(boolean ordered, EbenLibComparator<K> cmp) {
        this.ordered = ordered;
        if (ordered) {
            insertionOrder = new EbenLibList<>();
        }
        initBuckets(INITIAL_CAPACITY);
    }

    private void initBuckets(int capacity) {
        buckets = new EbenLibList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new EbenLibList<>();
        }
    }

    private int bucketIndex(Object key) {
        return (key == null ? 0 : Math.abs(key.hashCode())) % buckets.length;
    }

    // --- Static factories ---

    /** Creates a map with a single key/value pair. */
    public static <K, V> EbenLibMap<K, V> of(K k1, V v1) {
        EbenLibMap<K, V> m = new EbenLibMap<>();
        m.put(k1, v1);
        return m;
    }

    /** Creates an empty map. */
    public static <K, V> EbenLibMap<K, V> empty() {
        return new EbenLibMap<>();
    }

    // --- Mutators ---

    /**
     * Associates the specified value with the specified key.
     * Returns the old value, or null if none.
     */
    public V put(K key, V value) {
        maybeResize();
        int idx = bucketIndex(key);
        EbenLibList<EbenLibMapEntry<K, V>> bucket = buckets[idx];
        for (EbenLibMapEntry<K, V> entry : bucket) {
            if (key == null ? entry.getKey() == null : key.equals(entry.getKey())) {
                V old = entry.getValue();
                entry.setValue(value);
                return old;
            }
        }
        bucket.add(new EbenLibMapEntry<>(key, value));
        size++;
        if (ordered) {
            insertionOrder.add(key);
        }
        return null;
    }

    private void maybeResize() {
        if (size + 1 > buckets.length * LOAD_FACTOR) {
            // double capacity
            EbenLibList<EbenLibMapEntry<K, V>>[] old = buckets;
            initBuckets(buckets.length * 2);
            size = 0;
            for (EbenLibList<EbenLibMapEntry<K, V>> bucket : old) {
                for (EbenLibMapEntry<K, V> e : bucket) {
                    put(e.getKey(), e.getValue());
                }
            }
        }
    }

    public void merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        int index = Math.abs(key.hashCode()) % buckets.length;
        for (EbenLibMapEntry<K, V> entry : buckets[index]) {
            if (entry.getKey().equals(key)) {
                V newValue = remappingFunction.apply(entry.getValue(), value);
                entry.setValue(newValue);
                return;
            }
        }
        // key not found — insert new entry
        buckets[index].add(new EbenLibMapEntry<>(key, value));
        size++;
    }


    // --- Accessors ---

    /** Returns the value to which the specified key is mapped, or null if none. */
    public V get(K key) {
        int idx = bucketIndex(key);
        for (EbenLibMapEntry<K, V> entry : buckets[idx]) {
            if (key == null ? entry.getKey() == null : key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** Returns the value or default if key not present. */
    public V getOrDefault(K key, V defaultValue) {
        V v = get(key);
        return (v == null) ? defaultValue : v;
    }

    /** Returns true if this map contains a mapping for the key. */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    // --- Views ---

    /** Returns a list of keys in this map. */
    public EbenLibList<K> keySet() {
        if (ordered) {
            return new EbenLibList<>(insertionOrder);
        }
        EbenLibList<K> keys = new EbenLibList<>();
        for (EbenLibMapEntry<K, V> e : this) {
            keys.add(e.getKey());
        }
        return keys;
    }

    /** Returns a list of values in this map. */
    public EbenLibList<V> values() {
        EbenLibList<V> vals = new EbenLibList<>();
        for (EbenLibMapEntry<K, V> e : this) {
            vals.add(e.getValue());
        }
        return vals;
    }

    /** Returns a list of entries in this map. */
    public EbenLibList<EbenLibMapEntry<K, V>> entrySet() {
        EbenLibList<EbenLibMapEntry<K, V>> entries = new EbenLibList<>();
        for (EbenLibMapEntry<K, V> e : this) {
            entries.add(e);
        }
        return entries;
    }

    // --- Iterable ---

    /**
     * Iterates over all entries in all buckets.
     */
    @Override
    public Iterator<EbenLibMapEntry<K, V>> iterator() {
        if (!ordered){
            return new Iterator<>() {
                private int bucketIndex = 0;
                private Iterator<EbenLibMapEntry<K, V>> listIter = buckets[0].iterator();

                public boolean hasNext() {
                    while (!listIter.hasNext() && bucketIndex < buckets.length - 1) {
                        bucketIndex++;
                        listIter = buckets[bucketIndex].iterator();
                    }
                    return listIter.hasNext();
                }

                public EbenLibMapEntry<K, V> next() {
                    return listIter.next();
                }
            };
        } else{
            return new Iterator<>() {
            private int pos = 0;
            @Override public boolean hasNext() { return pos < insertionOrder.size(); }
            @Override public EbenLibMapEntry<K, V> next() {
                K key = insertionOrder.get(pos++);
                return new EbenLibMapEntry<>(key, get(key));
            }
        };
        }
    }

    // --- Removal ---

    /**
     * Removes the mapping for the specified key if present.
     * Returns the previous value, or null if none.
     */
    public V remove(K key) {
        int idx = bucketIndex(key);
        EbenLibList<EbenLibMapEntry<K, V>> bucket = buckets[idx];
        for (int i = 0; i < bucket.size(); i++) {
            EbenLibMapEntry<K, V> entry = bucket.get(i);
            if (key == null ? entry.getKey() == null : key.equals(entry.getKey())) {
                V old = entry.getValue();
                bucket.remove(i);
                size--;
                if (ordered) {
                    insertionOrder.remove(key);
                }
                return old;
            }
        }
        return null;
    }

    /** Removes all mappings. */
    public void clear() {
        initBuckets(INITIAL_CAPACITY);
        size = 0;
    }

    /** @return number of key-value mappings. */
    public int size() {
        return size;
    }

    /** @return true if no mappings. */
    public boolean isEmpty() {
        return size == 0;
    }
}
