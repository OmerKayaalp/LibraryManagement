package dataStructure.queue;

import dataStructure.linkedList.MyLinkedList;

public class MyQueue<T> {
    private MyLinkedList<T> list;

    public MyQueue() {
        list = new MyLinkedList<>();
    }

    public void enqueue(T element) {
        list.add(element);
    }

    public T dequeue() {
        if (isEmpty()) return null;
        T removed = list.get(0);
        list.remove(removed);
        return removed;
    }

    public T peek() {
        if (isEmpty()) return null;
        return list.get(0);
    }

    public boolean isEmpty() {
        return list.size() == 0;
    }

    public int size() {
        return list.size();
    }
}
