
package dataStructure.hashtable;

import dataStructure.linkedList.MyLinkedList;

public class HashTable<K, V> {

    private HashNode<K, V>[] buckets;
    private int capacity;
    private int size;
    private final double loadFactorThreshold = 0.7;

    @SuppressWarnings("unchecked")
    public HashTable() {
        this.capacity = 11; 
        this.buckets = new HashNode[capacity];
        this.size = 0;
    }

    private void validateKey(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
    }

    private int getIndex(K key) {
        return Math.abs(key.hashCode()) % capacity;
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

