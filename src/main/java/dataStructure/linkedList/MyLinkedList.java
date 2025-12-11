package dataStructure.linkedList;

/**
 * MyLinkedList - Singly linked list implementation.
 * 
 * PURPOSE: Dynamic collection that efficiently handles frequent additions/removals.
 * Used in LibrarySystem for:
 * - Member's active books list (frequently changes)
 * - Member's loan history (grows dynamically)
 * - Global loan history tracking
 * 
 * DATA STRUCTURE: Singly Linked List
 * Why LinkedList: O(1) add/remove at ends, efficient for dynamic collections.
 * Better than ArrayList when frequent insertions/deletions occur.
 * 
 * COMPLEXITY ANALYSIS:
 * - add (at end): O(1)
 * - add (at index): O(n) worst case
 * - get: O(n) - must traverse from head
 * - remove: O(n) worst case
 * - contains: O(n)
 * 
 * TRADE-OFF: Slower random access O(n) vs faster insertions O(1) compared to ArrayList.
 */
public class MyLinkedList<T> {

    /**
     * Inner class representing a node in the linked list.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    /**
     * Head of the linked list (first node).
     */
    private Node<T> head;
    
    /**
     * Current size of the list.
     */
    private int size;

    public MyLinkedList() {
        head = null;
        size = 0;
    }

    // ------------------------------
    // Add element at end
    // ------------------------------
    public void add(T element) {
        Node<T> newNode = new Node<>(element);

        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }

        size++;
    }

    // ------------------------------
    // Add element at index
    // ------------------------------
    public void add(int index, T element) {
        if (index < 0 || index > size) 
            throw new IndexOutOfBoundsException("Index: " + index);

        Node<T> newNode = new Node<>(element);

        if (index == 0) {
            newNode.next = head;
            head = newNode;
        } else {
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }

        size++;
    }

    // ------------------------------
    // Get element by index
    // ------------------------------
    public T get(int index) {
        if (index < 0 || index >= size) 
            throw new IndexOutOfBoundsException("Index: " + index);

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.data;
    }

    // ------------------------------
    // Remove element BY VALUE
    // ------------------------------
    public boolean remove(T element) {
        if (head == null) return false;

        if (head.data.equals(element)) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(element)) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }

    // ------------------------------
    // Remove element BY INDEX
    // ------------------------------
    public T removeByIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index);

        if (index == 0) {
            T removed = head.data;
            head = head.next;
            size--;
            return removed;
        }

        Node<T> current = head;

        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        T removed = current.next.data;
        current.next = current.next.next;
        size--;

        return removed;
    }

    // ------------------------------
    // Contains check
    // ------------------------------
    public boolean contains(T element) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(element)) return true;
            current = current.next;
        }
        return false;
    }

    // ------------------------------
    // Size & empty
    // ------------------------------
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // ------------------------------
    // Clear list
    // ------------------------------
    public void clear() {
        head = null;
        size = 0;
    }

    // ------------------------------
    // toString (debug)
    // ------------------------------
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
