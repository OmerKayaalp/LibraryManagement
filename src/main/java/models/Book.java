package models;

import dataStructure.queue.MyQueue;

public class Book {
    private int id;
    private String title;
    private String author;
    private int totalCopies;
    private int borrowedCopies;
    private int popularityCount;

    private MyQueue<Member> waitList;

    public Book(int id, String title, String author, int totalCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.borrowedCopies = 0;
        this.popularityCount = 0;
        this.waitList = new MyQueue<>();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }

    public int getAvailableCopies() {
        return totalCopies - borrowedCopies;
    }

    // ----- BORROW -----
    public void borrowCopy() {
        if (getAvailableCopies() > 0) {
            borrowedCopies++;
            popularityCount++;
        }
    }

    // ----- RETURN -----
    public void returnCopy() {
        if (borrowedCopies > 0)
            borrowedCopies--;
    }

    // ----- WAITLIST -----
    public void addToWaitList(Member m) {
        waitList.enqueue(m);
    }

    public Member getNextWaitingMember() {
        if (waitList.isEmpty()) return null;
        return waitList.dequeue();
    }

    // ----- CHECK -----
    public boolean canBeBorrowed() {
        return getAvailableCopies() > 0;
    }

    public int getPopularity() {
        return popularityCount;
    }
}
