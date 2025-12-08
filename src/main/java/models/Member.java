package models;

import dataStructure.linkedList.MyLinkedList;

public class Member {
    private  int memberID;
    private  String name;
    private MyLinkedList<Book> currentLoans;

    public Member (int memberID, String name) {
        this.memberID = memberID;
        this.name = name;
        this.currentLoans = new MyLinkedList<>();

    }

    public int getMemberID() {
        return memberID;
    }

    public String getName() {
        return name;
    }
    
    public MyLinkedList<Book> getCurrentLoans() {
        return currentLoans;
    }
    public void borrowBook(Book book){
        currentLoans.add(book);
    }
    public void returnBook(Book book){
    currentLoans.remove(book);
    }
    public boolean hasBook(Book book){
    return currentLoans.contains(book);
    }
    public int getLoanCount(){
    return currentLoans.size();
    }
}
