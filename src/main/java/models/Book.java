package models;

import dataStructure.queue.MyQueue;

public class Book implements Comparable<Book> {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int publishYear;
    private int pageCount;

    private int totalCopies;
    private int borrowedCopies;
    private int popularityCount;

    private MyQueue<Member> waitList;

    public Book(int id, String isbn, String title, String author, String category, int publishYear, int pageCount, int totalCopies) {
        this.id = id;
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
    public int getId() { return id; }
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

    // --- Waitlist helpers ---
    public void addToWaitList(Member m) {
        if (m != null) waitList.enqueue(m);
    }

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

    // --- For MaxHeap (descending popularity) ---
    @Override
    public int compareTo(Book other) {
        // Higher popularity => "greater"
        return Integer.compare(this.popularityCount, other.popularityCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book b = (Book) o;
        return this.id == b.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
