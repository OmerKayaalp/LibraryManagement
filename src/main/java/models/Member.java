package models;

import dataStructure.linkedList.MyLinkedList;

public class Member {

    private final int memberID;
    private String name;

    // Aktif ödünç alınan kitaplar
    private MyLinkedList<Book> activeBooks;

    // Tüm loan kayıtları
    private MyLinkedList<LoanRecord> loanHistory;

    // limit
    private int maxBorrowLimit = 5;

    // ❗ Eksik olan ceza alanı eklendi
    private double penalty = 0.0;

    public Member(int memberID, String name) {
        this.memberID = memberID;
        this.name = name;
        this.activeBooks = new MyLinkedList<>();
        this.loanHistory = new MyLinkedList<>();
    }

    public int getMemberID() {
        return memberID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyLinkedList<Book> getActiveBooks() {
        return activeBooks;
    }

    public MyLinkedList<LoanRecord> getLoanHistory() {
        return loanHistory;
    }

    public int getActiveBookCount() {
        return activeBooks.size();
    }

    public boolean canBorrow() {
        return activeBooks.size() < maxBorrowLimit && penalty < 50;
    }

    // ------------------ BORROW ------------------

    public LoanRecord borrowBook(Book book) {
        if (!canBorrow()) {
            System.out.println("Member cannot borrow more books or has too much penalty.");
            return null;
        }

        activeBooks.add(book);

        LoanRecord lr = new LoanRecord(book, this);
        loanHistory.add(lr);
        return lr;
    }

    // ------------------ RETURN ------------------

    public void returnBook(Book book) {
        if (!hasBook(book)) return;

        for (int i = 0; i < activeBooks.size(); i++) {
            if (activeBooks.get(i).equals(book)) {
                activeBooks.removeByIndex(i);
                break;
            }
        }
    }

    // Undo system için gerekli
    public void removeActiveLoanRecord(LoanRecord lr) {
        Book b = lr.getBook();

        for (int i = 0; i < activeBooks.size(); i++) {
            if (activeBooks.get(i).equals(b)) {
                activeBooks.removeByIndex(i);
                break;
            }
        }
    }

    public boolean hasBook(Book book) {
        for (int i = 0; i < activeBooks.size(); i++) {
            if (activeBooks.get(i).equals(book)) return true;
        }
        return false;
    }

    public void setMaxBorrowLimit(int limit) {
        this.maxBorrowLimit = limit;
    }

    // ------------------ PENALTY SYSTEM ------------------

    public double getPenalty() {
        return penalty;
    }

    public void addPenalty(double amount) {
        if (amount <= 0) return;
        penalty += amount;
    }

    public void payPenalty(double amount) {
        if (amount <= 0) return;
        penalty -= amount;
        if (penalty < 0) penalty = 0;
    }

    @Override
    public String toString() {
        return "Member{" +
                "ID=" + memberID +
                ", name='" + name + '\'' +
                ", activeBooks=" + activeBooks.size() +
                ", penalty=" + penalty +
                '}';
    }
}
