package models;

import dataStructure.queue.MyQueue;

/**
 * Book model class representing a book in the library system.
 * 
 * DATA STRUCTURE USAGE:
 * - MyQueue<Member> waitList: Used for managing waitlist (FIFO - First Come First Served)
 *   This ensures fair distribution when books become available.
 * 
 * COMPLEXITY:
 * - Waitlist operations: O(1) enqueue/dequeue
 * - Popularity comparison: O(1) for heap operations
 */
public class Book implements Comparable<Book> {

    /**
     * Auto-incrementing counter for generating unique book IDs.
     * Ensures each book has a unique identifier.
     */
    private static int idCounter = 1;

    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int publishYear;
    private int pageCount;

    private int totalCopies;
    private int borrowedCopies;
    private int popularityCount;

    /**
     * DATA STRUCTURE: Queue for Waitlist Management
     * Purpose: Maintain FIFO order for members waiting for this book
     * Why Queue: Ensures fair first-come-first-served access when book becomes available
     */
    private MyQueue<Member> waitList;

    /**
     * Constructor: Creates a new book with auto-generated ID.
     * 
     * @param isbn ISBN identifier
     * @param title Book title
     * @param author Author name
     * @param category Book category
     * @param publishYear Publication year
     * @param pageCount Number of pages
     * @param totalCopies Total copies available in library
     */
    public Book(String isbn, String title, String author, String category,
                int publishYear, int pageCount, int totalCopies) {

        // Auto-generate unique book ID
        this.bookId = idCounter++;

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishYear = publishYear;
        this.pageCount = pageCount;
        this.totalCopies = totalCopies;

        this.borrowedCopies = 0;
        this.popularityCount = 0;
        this.waitList = new MyQueue<>();
    }

    // --- Eğer eski constructor'ı da istersen (ID elle verilen) ---
    public Book(int bookId, String isbn, String title, String author,
                String category, int publishYear, int pageCount, int totalCopies) {

        this.bookId = bookId;
        // idCounter’ın geride kalmaması için güncelliyoruz:
        if (bookId >= idCounter) {
            idCounter = bookId + 1;
        }

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishYear = publishYear;
        this.pageCount = pageCount;
        this.totalCopies = totalCopies;

        this.borrowedCopies = 0;
        this.popularityCount = 0;
        this.waitList = new MyQueue<>();
    }

    // --- Getters ---
    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getPublishYear() { return publishYear; }
    public int getPageCount() { return pageCount; }

    public int getTotalCopies() { return totalCopies; }
    public int getBorrowedCopies() { return borrowedCopies; }
    public int getAvailableCopies() { return totalCopies - borrowedCopies; }
    public int getPopularityCount() { return popularityCount; }

    // --- Borrow / Return operations ---
    public boolean canBeBorrowed() {
        return getAvailableCopies() > 0;
    }

    public boolean borrowCopy() {
        if (canBeBorrowed()) {
            borrowedCopies++;
            popularityCount++;
            return true;
        }
        return false;
    }

    public boolean returnCopy() {
        if (borrowedCopies > 0) {
            borrowedCopies--;
            return true;
        }
        return false;
    }

    // --- Waitlist helpers (Queue Operations) ---
    
    /**
     * Add member to waitlist queue (FIFO).
     * Time Complexity: O(1)
     * 
     * @param m Member to add to waitlist
     */
    public void addToWaitList(Member m) {
        if (m != null) waitList.enqueue(m);
    }

    /**
     * Get next member from waitlist (FIFO - first come first served).
     * Time Complexity: O(1)
     * 
     * @return Next member in queue, or null if waitlist is empty
     */
    public Member getNextWaitingMember() {
        if (waitList == null || waitList.isEmpty()) return null;
        return waitList.dequeue();
    }

    public boolean hasWaitList() {
        return waitList != null && !waitList.isEmpty();
    }

    public MyQueue<Member> getWaitList() { return waitList; }

    // --- Search helper ---
    public boolean matches(String query) {
        if (query == null) return false;
        String q = query.toLowerCase();
        return (title != null && title.toLowerCase().contains(q))
            || (author != null && author.toLowerCase().contains(q))
            || (isbn != null && isbn.toLowerCase().contains(q))
            || (category != null && category.toLowerCase().contains(q));
    }

    /**
     * Compare books by popularity count (for MaxHeap).
     * Time Complexity: O(1)
     * 
     * Used by MaxHeap to maintain heap property based on popularity.
     * Higher popularity count = higher priority in heap.
     * 
     * @param other Other book to compare with
     * @return Negative if this is less popular, positive if more popular, 0 if equal
     */
    @Override
    public int compareTo(Book other) {
        return Integer.compare(this.popularityCount, other.popularityCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book b = (Book) o;
        return this.bookId == b.bookId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(bookId);
    }

    @Override
    public String toString() {
        return "Book{" +
                "ID=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", publishYear=" + publishYear +
                ", pageCount=" + pageCount +
                ", totalCopies=" + totalCopies +
                ", borrowedCopies=" + borrowedCopies +
                ", popularityCount=" + popularityCount +
                '}';
    }
}
