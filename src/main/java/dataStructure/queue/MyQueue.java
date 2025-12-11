package dataStructure.queue;

import dataStructure.linkedList.MyLinkedList;

/**
 * MyQueue - Queue implementation using LinkedList (FIFO - First In First Out).
 * 
 * PURPOSE: Maintain fair waitlist for books (first-come-first-served).
 * Used in Book class for managing waitlist when books are unavailable.
 * 
 * DATA STRUCTURE: Queue (implemented using LinkedList)
 * Why Queue: Ensures FIFO order - first member to request gets book first.
 * Critical for fair distribution of limited resources (books).
 * 
 * COMPLEXITY ANALYSIS:
 * - enqueue: O(1) - add to end of list
 * - dequeue: O(1) - remove from front of list
 * - peek: O(1) - access first element
 * 
 * OPERATIONS:
 * - enqueue: Add element to back (FIFO)
 * - dequeue: Remove element from front (FIFO)
 */
public class MyQueue<T> {
    /**
     * Underlying LinkedList used to implement queue operations.
     */
    private MyLinkedList<T> list;

    /**
     * Constructor: Creates empty queue.
     */
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
