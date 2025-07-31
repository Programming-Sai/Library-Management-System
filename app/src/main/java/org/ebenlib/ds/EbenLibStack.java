package org.ebenlib.ds;

public class EbenLibStack<T> {
    private EbenLibList<T> data;

    public EbenLibStack() {
        data = new EbenLibList<>();
    }

    public void push(T item) {
        data.add(item);
    }

    public T pop() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        return data.remove(data.size() - 1);
    }

    public T peek() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        return data.get(data.size() - 1);
    }

    public boolean isEmpty() {
        return data.size() == 0;
    }

    public int size() {
        return data.size();
    }

    public void clear() {
        data.clear();
    }

    @Override
    public String toString() {
        return data.toString(); // Or customize as Stack-like output
    }
}
