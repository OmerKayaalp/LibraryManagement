package dataStructure.heap;

import java.util.ArrayList;

/**
 * MaxHeap - Maximum heap implementation using array-based binary heap.
 * 
 * PURPOSE: Efficiently track and retrieve most popular books.
 * Used in LibrarySystem for:
 * - Tracking book popularity (O(log n) insert/update)
 * - Retrieving top-K popular books (O(k log n))
 * 
 * DATA STRUCTURE: Binary Max Heap (array-based)
 * Why Heap: Provides optimal O(log n) insert and O(k log n) top-K retrieval.
 * Better than sorting entire collection O(n log n) when k << n.
 * 
 * COMPLEXITY ANALYSIS:
 * - insert: O(log n)
 * - extractMax: O(log n)
 * - getTopK: O(k log n) where k is number requested
 * - increaseKey: O(log n)
 * 
 * HEAP PROPERTY: Parent >= Children (max-heap)
 * Array representation: parent at index i, children at 2i+1 and 2i+2
 */
public class MaxHeap<T extends Comparable<T>> {

    /**
     * Array-based heap storage.
     * Index 0 is root (maximum element).
     */
    private ArrayList<T> heap;

    public MaxHeap() {
        heap = new ArrayList<>();
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public T peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    public void insert(T element) {
        heap.add(element);
        heapifyUp(heap.size() - 1);
    }

    public T extractMax() {
        if (heap.isEmpty()) return null;

        T max = heap.get(0);

        heap.set(0, heap.get(heap.size() - 1));
        heap.remove(heap.size() - 1);

        heapifyDown(0);
        return max;
    }

    public void increaseKey(T element) {
        int index = heap.indexOf(element);
        if (index == -1) return;

        heapifyUp(index);
    }

    public boolean contains(T element) {
        return heap.contains(element);
    }

    public ArrayList<T> getTopK(int k) {
        ArrayList<T> result = new ArrayList<>();
        ArrayList<T> temp = new ArrayList<>(heap);

        for (int i = 0; i < k && !heap.isEmpty(); i++) {
            result.add(extractMax());
        }

        heap = temp;
        return result;
    }


    private void heapifyUp(int index) {
        int parent = (index - 1) / 2;

        while (index > 0 && heap.get(index).compareTo(heap.get(parent)) > 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    private void heapifyDown(int index) {
        int left, right, largest;

        while (true) {
            left = 2 * index + 1;
            right = 2 * index + 2;
            largest = index;

            if (left < heap.size() && heap.get(left).compareTo(heap.get(largest)) > 0)
                largest = left;

            if (right < heap.size() && heap.get(right).compareTo(heap.get(largest)) > 0)
                largest = right;

            if (largest == index) break;

            swap(index, largest);
            index = largest;
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
