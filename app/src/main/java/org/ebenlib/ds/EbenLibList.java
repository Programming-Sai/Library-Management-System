package org.ebenlib.ds;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A growable, index‑based list implementation (ArrayList‑like).
 *
 * @param <T> the element type
 */
@SuppressWarnings("unchecked")
public class EbenLibList<T> implements Iterable<T> {
    private Object[] elements;
    private int size;

    /**
     * Constructs an empty list with initial capacity 10.
     */
    public EbenLibList() {
        elements = new Object[10];
        size = 0;
    }

    /**
     * Constructs a new list by copying all elements from {@code other}.
     *
     * @param other the list to copy
     */
    public EbenLibList(EbenLibList<T> other) {
        this.size = other.size;
        this.elements = new Object[other.elements.length];
        System.arraycopy(other.elements, 0, this.elements, 0, size);
    }

    public EbenLibList<T> skip(int n) {
        EbenLibList<T> result = new EbenLibList<>();
        for (int i = n; i < this.size(); i++) {
            result.add(this.get(i));
        }
        return result;
    }

    public static <T> EbenLibList<T> from(T[] input) {
        EbenLibList<T> out = new EbenLibList<>();
        for (T val : input) {
            out.add(val);
        }
        return out;
    }


    public void add(int index, T element) {
        if (index < 0 || index > size) 
            throw new IndexOutOfBoundsException("Index " + index);
        ensureCapacity();
        // shift right
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }

    public Stream<T> stream() {
        // cast is safe because we only ever store Ts
        return Arrays.stream((T[])elements, 0, size);
    }

    /**
     * Creates a list containing the given items, in order.
     *
     * @param items the items to include
     * @param <T>   element type
     * @return a new list containing {@code items}
     */
    @SafeVarargs
    public static <T> EbenLibList<T> of(T... items) {
        EbenLibList<T> list = new EbenLibList<>();
        for (T item : items) {
            list.add(item);
        }
        return list;
    }

    /**
     * Appends {@code item} to the end of the list.
     *
     * @param item the element to add
     */
    public void add(T item) {
        ensureCapacity();
        elements[size++] = item;
    }

    /**
     * Removes all elements from the list.
     * Resets to initial capacity.
     */
    public void clear() {
        elements = new Object[10];
        size = 0;
    }

    /**
     * Removes all elements matching {@code pred}.
     *
     * @param pred predicate to test elements
     * @return true if any element was removed
     */
    public boolean removeIf(Predicate<T> pred) {
        boolean removed = false;
        int writeIndex = 0;
        for (int readIndex = 0; readIndex < size; readIndex++) {
            T item = (T) elements[readIndex];
            if (!pred.test(item)) {
                elements[writeIndex++] = item;
            } else {
                removed = true;
            }
        }
        size = writeIndex;
        return removed;
    }


    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        T removed = (T) elements[index];

        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }

        elements[--size] = null; // Help GC
        return removed;
    }


    public boolean remove(T item) {
    for (int i = 0; i < size; i++) {
        T current = (T) elements[i];
        if ((current == null && item == null) || (current != null && current.equals(item))) {
            // shift left
            for (int j = i; j < size - 1; j++) {
                elements[j] = elements[j + 1];
            }
            elements[--size] = null; // help GC
            return true;
        }
    }
    return false;
}



    /**
     * Returns the element at position {@code index}.
     *
     * @param index position (0‑based)
     * @return the element
     * @throws IndexOutOfBoundsException if index is out of [0, size)
     */
    public T get(int index) {
        checkIndex(index);
        return (T) elements[index];
    }

    /**
     * Replaces the element at {@code index} with {@code element}.
     *
     * @param index   position to set
     * @param element new element
     * @return the old element
     * @throws IndexOutOfBoundsException if index is out of [0, size)
     */
    public T set(int index, T element) {
        checkIndex(index);
        T old = (T) elements[index];
        elements[index] = element;
        return old;
    }

    /**
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * @return true if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns a new list containing elements from {@code fromIndex} (inclusive)
     * to {@code toIndex} (exclusive).
     *
     * @param fromIndex start (inclusive)
     * @param toIndex   end (exclusive)
     * @return the sublist
     * @throws IndexOutOfBoundsException if indices are invalid
     */
    public EbenLibList<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException(
                "fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", size=" + size
            );
        }
        EbenLibList<T> sub = new EbenLibList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            sub.add(get(i));
        }
        return sub;
    }

    /**
     * Returns a new list containing only elements that satisfy {@code pred}.
     *
     * @param pred predicate to filter
     * @return the filtered list
     */
    public EbenLibList<T> filter(Predicate<T> pred) {
        EbenLibList<T> result = new EbenLibList<>();
        for (T item : this) {
            if (pred.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Transforms each element by {@code mapper} and returns a list of results.
     *
     * @param mapper function to apply
     * @param <R>    result type
     * @return a new list of mapped values
     */
    public <R> EbenLibList<R> map(EbenLibFunction<T, R> mapper) {
        EbenLibList<R> result = new EbenLibList<>();
        for (T item : this) {
            result.add(mapper.apply(item));
        }
        return result;
    }

    /**
     * Returns an iterator over the elements in this list, in order.
     *
     * @return iterator
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pos = 0;
            public boolean hasNext() {
                return pos < size;
            }
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return (T) elements[pos++];
            }
        };
    }

    /**
     * Copies this list into {@code a}. If {@code a} is too small,
     * a new array of the same runtime type is allocated.
     *
     * @param a   destination array
     * @return {@code a} filled with list elements (with a trailing null if larger)
     */
    public T[] toArray(T[] a) {
        if (a.length < size) {
            // note: java.util.Arrays is used only here, inside implementation
            return (T[]) java.util.Arrays.copyOf(elements, size, a.getClass());
        }
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size) a[size] = null;
        return a;
    }

    // ─── Internal helpers ───────────────────────────────────────────────────

    /** Double capacity when array is full. */
    private void ensureCapacity() {
        if (size == elements.length) {
            Object[] newArr = new Object[elements.length * 2];
            System.arraycopy(elements, 0, newArr, 0, elements.length);
            elements = newArr;
        }
    }

    /**
     * Validates {@code index} is in [0, size).  
     * Throws if not.
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for size " + size
            );
        }
    }

  /**
   * Returns an empty list.
   */
  public static <T> EbenLibList<T> empty() {
      return new EbenLibList<>();
  }

}
