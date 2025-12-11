package dataStructure.hashtable;

import dataStructure.linkedList.MyLinkedList;

/**
 * HashTable - Hash table implementation with chaining for collision resolution.
 * 
 * PURPOSE: Fast O(1) average lookup, insertion, and deletion by key.
 * Used in LibrarySystem for:
 * - Book lookup by ID (O(1) average)
 * - Member lookup by ID (O(1) average)
 * 
 * COMPLEXITY ANALYSIS:
 * - get/put/remove: O(1) average case, O(n) worst case (all collisions)
 * - Resize: O(n) when load factor threshold exceeded
 * 
 * UNIQUE STUDENT ID INTEGRATION:
 * Uses hashSalt (student ID) to modify hash function, ensuring unique hash distribution
 * across different submissions. This prevents identical hash patterns.
 */
public class HashTable<K, V> {

    /**
     * Hash salt (student ID) used to modify hash function.
     * Ensures unique hash distribution per submission.
     */
    private int hashSalt = 0;

    private HashNode<K, V>[] buckets;
    private int capacity;
    private int size;
    
    /**
     * Load factor threshold for resizing.
     * When size/capacity >= 0.7, table is resized to maintain O(1) performance.
     */
    private final double loadFactorThreshold = 0.7;

    @SuppressWarnings("unchecked")
    public HashTable() {
        this.capacity = 11; 
        this.buckets = new HashNode[capacity];
        this.size = 0;
    }
    
    @SuppressWarnings("unchecked")
    public HashTable(int salt) {
    this.capacity = 11;
    this.buckets = new HashNode[capacity];
    this.size = 0;
    this.hashSalt = salt;
    }

    private void validateKey(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
    }

   /**
    * Calculate bucket index for a key using hash function with salt.
    * UNIQUE ID USAGE: XOR with hashSalt (student ID) ensures unique hash distribution.
    * 
    * Time Complexity: O(1)
    * 
    * @param key The key to hash
    * @return Bucket index (0 to capacity-1)
    */
   private int getIndex(K key) {
    int h = key.hashCode();
    // XOR with student ID salt to ensure unique hash patterns per submission
    h = h ^ hashSalt;
    return Math.abs(h) % capacity;
}

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void put(K key, V value) {
        validateKey(key);

        int index = getIndex(key);
        HashNode<K, V> head = buckets[index];

        while (head != null) {
            if (head.getKey().equals(key)) {
                head.setValue(value);
                return;
            }
            head = head.getNext();
        }

        HashNode<K, V> newNode = new HashNode<>(key, value);
        newNode.setNext(buckets[index]);
        buckets[index] = newNode;

        size++;

        if ((1.0 * size) / capacity >= loadFactorThreshold) {
            resize();
        }
    }
    

    public V get(K key) {
        validateKey(key);

        int index = getIndex(key);
        HashNode<K, V> head = buckets[index];

        while (head != null) {
            if (head.getKey().equals(key)) {
                return head.getValue();
            }
            head = head.getNext();
        }

        return null;
    }
    public MyLinkedList<V> values() {
    MyLinkedList<V> list = new MyLinkedList<>();

    for (int i = 0; i < capacity; i++) {
        HashNode<K, V> head = buckets[i];
        while (head != null) {
            list.add(head.getValue());
            head = head.getNext();
        }
    }
    return list;
    }
    public MyLinkedList<K> keySet() {
    MyLinkedList<K> keys = new MyLinkedList<>();

    for (int i = 0; i < capacity; i++) {
        HashNode<K, V> head = buckets[i];
        while (head != null) {
            keys.add(head.getKey());
            head = head.getNext();
        }
    }
    return keys;
}

   public V remove(K key) {
        validateKey(key);

        int index = getIndex(key);
        HashNode<K, V> head = buckets[index];
        HashNode<K, V> prev = null;

        while (head != null) {
            if (head.getKey().equals(key)) {
                if (prev == null) {
                    buckets[index] = head.getNext();
                } else {
                    prev.setNext(head.getNext());
                }
                size--;
                return head.getValue();
            }
            prev = head;
            head = head.getNext();
        }

        return null;
    }


   @SuppressWarnings("unchecked")
    private void resize() {
        int oldCapacity = capacity;
        capacity = nextPrime(2 * oldCapacity);

        HashNode<K, V>[] oldBuckets = buckets;
        buckets = new HashNode[capacity];
        size = 0;

        for (HashNode<K, V> headNode : oldBuckets) {
            while (headNode != null) {
                put(headNode.getKey(), headNode.getValue());
                headNode = headNode.getNext();
            }
        }
    }

     private boolean isPrime(int num) {
        if (num <= 1) return false;
        if (num <= 3) return true;

        if (num % 2 == 0 || num % 3 == 0) return false;

        for (int i = 5; i * i <= num; i += 6) {
            if (num % i == 0 || num % (i + 2) == 0) return false;
        }

        return true;
    }

    private int nextPrime(int num) {
        while (!isPrime(num)) {
            num++;
        }
        return num;
    }
}

