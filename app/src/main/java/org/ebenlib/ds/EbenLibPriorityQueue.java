package org.ebenlib.ds;

public class EbenLibPriorityQueue<T> {
    private EbenLibList<T> heap;
    private EbenLibComparator<T> comparator;

    public EbenLibPriorityQueue(EbenLibComparator<T> comparator) {
        this.comparator = comparator;
        this.heap = new EbenLibList<>();
    }

    public void offer(T item) {
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    public T poll() {
        if (isEmpty()) throw new RuntimeException("Queue is empty");
        T root = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public T peek() {
        if (isEmpty()) throw new RuntimeException("Queue is empty");
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.size() == 0;
    }

    public int size() {
        return heap.size();
    }

    public void clear() {
        heap.clear();
    }

    private void siftUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) / 2;
            if (comparator.compare(heap.get(idx), heap.get(parent)) >= 0) break;
            swap(idx, parent);
            idx = parent;
        }
    }

    private void siftDown(int idx) {
        int size = heap.size();
        while (true) {
            int left = 2 * idx + 1;
            int right = 2 * idx + 2;
            int smallest = idx;

            if (left < size && comparator.compare(heap.get(left), heap.get(smallest)) < 0)
                smallest = left;
            if (right < size && comparator.compare(heap.get(right), heap.get(smallest)) < 0)
                smallest = right;

            if (smallest == idx) break;
            swap(idx, smallest);
            idx = smallest;
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    @Override
    public String toString() {
        return heap.toString();
    }
}
