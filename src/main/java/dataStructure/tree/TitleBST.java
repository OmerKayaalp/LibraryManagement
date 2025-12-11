package dataStructure.tree;

import models.Book;
import java.util.ArrayList;
import java.util.List;
import dataStructure.linkedList.MyLinkedList;

/**
 * TitleBST - Binary Search Tree for efficient title-based book search.
 * 
 * PURPOSE: Maintain sorted index of books by title for fast prefix search.
 * Used in LibrarySystem for O(log n) title search instead of O(n) linear search.
 * 
 * DATA STRUCTURE: Binary Search Tree (BST)
 * Why BST: Provides O(log n) average search time for sorted data.
 * Better than linear search O(n) when searching by title prefix.
 * 
 * COMPLEXITY ANALYSIS:
 * - add: O(log n) average, O(n) worst case (unbalanced tree)
 * - remove: O(log n) average, O(n) worst case
 * - searchByTitlePrefix: O(log n + m) where m is number of matches
 * 
 * NOTE: For production, AVL tree would provide guaranteed O(log n) performance,
 * but BST is sufficient for this assignment's requirements.
 */
public class TitleBST {
    private class Node {
        String key; // normalized title
        MyLinkedList<Book> books; // if same title appear multiple times
        Node left, right;
        Node(String k, Book b) {
            key = k;
            books = new MyLinkedList<>();
            books.add(b);
        }
    }

    private Node root;

    public void add(Book b) {
        String k = normalize(b.getTitle());
        root = addRec(root, k, b);
    }

    private Node addRec(Node node, String k, Book b) {
        if (node == null) return new Node(k, b);
        int cmp = k.compareTo(node.key);
        if (cmp < 0) node.left = addRec(node.left, k, b);
        else if (cmp > 0) node.right = addRec(node.right, k, b);
        else node.books.add(b);
        return node;
    }

    public void remove(Book b) {
        String k = normalize(b.getTitle());
        root = removeRec(root, k, b);
    }

    private Node removeRec(Node node, String k, Book b) {
        if (node == null) return null;
        int cmp = k.compareTo(node.key);
        if (cmp < 0) node.left = removeRec(node.left, k, b);
        else if (cmp > 0) node.right = removeRec(node.right, k, b);
        else {
            // remove book from node.books
            node.books.remove(b);
            if (node.books.size() == 0) {
                // remove this node from tree (standard BST delete)
                if (node.left == null) return node.right;
                if (node.right == null) return node.left;
                // both children exist - find min in right subtree
                Node min = findMin(node.right);
                node.key = min.key;
                node.books = min.books;
                node.right = removeRec(node.right, min.key, null); // remove the min node
            }
        }
        return node;
    }

    private Node findMin(Node n) {
        while (n.left != null) n = n.left;
        return n;
    }

    public List<Book> searchByTitlePrefix(String prefix) {
        List<Book> result = new ArrayList<>();
        searchPrefixRec(root, normalize(prefix), result);
        return result;
    }

    private void searchPrefixRec(Node node, String prefix, List<Book> result) {
        if (node == null) return;
        if (node.key.startsWith(prefix)) {
            // collect all in this node and descend both sides
            for (int i=0; i<node.books.size(); i++) result.add(node.books.get(i));
            searchPrefixRec(node.left, prefix, result);
            searchPrefixRec(node.right, prefix, result);
        } else if (prefix.compareTo(node.key) < 0) {
            searchPrefixRec(node.left, prefix, result);
        } else {
            searchPrefixRec(node.right, prefix, result);
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase().trim();
    }
}
