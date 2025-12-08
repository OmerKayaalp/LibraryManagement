package models;

import dataStructure.linkedList.MyLinkedList;

public class Member {
    private String name;
    private int memberId;

    private MyLinkedList<Book> borrowedBooks;

    public Member(String name, int memberId) {
        this.name = name;
        this.memberId = memberId;
        this.borrowedBooks = new MyLinkedList<>();
    }

    public String getName() { return name; }
    public int getMemberId() { return memberId; }

    // ----- ÜYE KİTAP ÖDÜNÇ ALIYOR -----
    public void borrowBook(Book book) {
        borrowedBooks.add(book);
    }

    // ----- ÜYE KİTABI İADE EDİYOR -----
    public void returnBook(Book book) {
        borrowedBooks.remove(book);
    }

    public boolean hasBook(Book book) {
        return borrowedBooks.contains(book);
    }
}
