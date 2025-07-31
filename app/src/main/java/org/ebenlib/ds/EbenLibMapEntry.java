package org.ebenlib.ds;

import java.util.Objects;

public class EbenLibMapEntry<K, V> {
    private final K key;
    private V value;

    public EbenLibMapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    // Optional: Useful if you're using sets or maps internally
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EbenLibMapEntry<?, ?> other)) return false;
        return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
