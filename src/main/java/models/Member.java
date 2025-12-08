package models;

import dataStructure.linkedList.MyLinkedList;

public class Member {
    private int memberID;
    private String name;

    private int maxLoans = 5;             // configurable
    private double penaltyBalance = 0.0;  // money owed (fines)
    private int penaltyPoints = 0;        // optional points

    private MyLinkedList<Book> activeBooks;          // current borrowed books
    private MyLinkedList<LoanRecord> activeLoans;    // current loan records
    private MyLinkedList<LoanRecord> loanHistory;    // all loans

    public Member(int memberID, String name) {
        this.memberID = memberID;
        this.name = name;
        this.activeBooks = new MyLinkedList<>();
        this.activeLoans = new MyLinkedList<>();
        this.loanHistory = new MyLinkedList<>();
    }

    public int getMemberID() { return memberID; }
    public String getName() { return name; }
    public double getPenaltyBalance() { return penaltyBalance; }

    public int getActiveLoanCount() { return activeBooks.size(); }
    public MyLinkedList<Book> getActiveBooks() { return activeBooks; }
    public MyLinkedList<LoanRecord> getLoanHistory() { return loanHistory; }

    // can the member borrow now? respect maxLoans and unpaid balance
    public boolean canBorrow() {
        return (activeBooks.size() < maxLoans) && (penaltyBalance <= 0.0);
    }

    // Borrow helper: create new LoanRecord, update lists
    public LoanRecord borrowBook(Book book) {
        // book.borrowCopy should be called by caller (LibrarySystem) to ensure availability
        activeBooks.add(book);
        LoanRecord lr = new LoanRecord(this, book);
        activeLoans.add(lr);
        loanHistory.add(lr);
        return lr;
    }

    // Return helper: remove book and corresponding activeLoan (but do not change book here)
    public void returnBook(Book book) {
        activeBooks.remove(book);

        // find matching activeLoan and mark returned
        for (int i = 0; i < activeLoans.size(); i++) {
            LoanRecord lr = activeLoans.get(i);
            if (lr.getBook().equals(book) && !lr.isReturned()) {
                lr.markReturned(); // will update book and next waitlist
                // after lr.markReturned(), lr will remove member's active book inside markReturned
                activeLoans.remove(lr);
                break;
            }
        }
    }

    // administrative penalty methods
    public void addPenalty(double amount) {
        if (amount <= 0) return;
        this.penaltyBalance += amount;
    }

    public void payPenalty(double amount) {
        if (amount <= 0) return;
        this.penaltyBalance -= amount;
        if (this.penaltyBalance < 0) this.penaltyBalance = 0.0;
    }

    public boolean hasOverdueBooks() {
        for (int i = 0; i < activeLoans.size(); i++) {
            if (activeLoans.get(i).isOverdue()) return true;
        }
        return false;
    }

    // utility
    public boolean hasBook(Book book) { return activeBooks.contains(book); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member m = (Member) o;
        return this.memberID == m.memberID;
    }

    @Override
    public int hashCode() { return Integer.hashCode(memberID); }
}
