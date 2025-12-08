package core;

import dataStructure.hashtable.HashTable;
import dataStructure.linkedList.MyLinkedList;
import dataStructure.heap.MaxHeap;
import dataStructure.queue.MyQueue;
import models.Book;
import models.Member;
import models.LoanRecord;

import java.util.ArrayList;
import java.util.List;

public class LibrarySystem {

    private HashTable<Integer, Book> bookTable;
    private HashTable<Integer, Member> memberTable;
    private MyLinkedList<LoanRecord> loanHistory;

    private MaxHeap<Book> popularityHeap; // keep books ordered by popularity (requires Book implements Comparable)

    // configuration
    private int defaultLoanDays = 14;

    public LibrarySystem() {
        bookTable = new HashTable<>();
        memberTable = new HashTable<>();
        loanHistory = new MyLinkedList<>();
        popularityHeap = new MaxHeap<>();
    }

    // ---------------- Add / Remove ----------------
    public void addBook(Book book) {
        bookTable.put(book.getId(), book);
        popularityHeap.insert(book);
    }

    public Book removeBook(int bookId) {
        Book b = bookTable.get(bookId);
        if (b != null) {
            bookTable.remove(bookId);
            // note: heap removal not implemented; can rebuild heap if needed
        }
        return b;
    }

    public void addMember(Member m) {
        memberTable.put(m.getMemberID(), m);
    }

    // ---------------- Search ----------------
    public Book searchById(int id) {
        return bookTable.get(id);
    }

    public List<Book> searchByTitle(String title) {
        List<Book> res = new ArrayList<>();
        // iterate all books
        // use bookTable.values() if implemented; else scan buckets (here assume HashTable has values())
        // if values() not available, user can implement keySet+get or HashTable.values
        MyLinkedList<Book> all = bookTableValues();
        for (int i = 0; i < all.size(); i++) {
            Book b = all.get(i);
            if (b.matches(title)) res.add(b);
        }
        return res;
    }

    // helper to collect values from hash table (if your HashTable doesn't have values())
    private MyLinkedList<Book> bookTableValues() {
        MyLinkedList<Book> list = new MyLinkedList<>();
        // naive: iterate possible ID range not possible -> better implement HashTable.values()
        // For now, if HashTable has values() use it. Otherwise you should add values() in HashTable.
        // Here we assume HashTable has values() returning MyLinkedList<V>. If not, add it.
        try {
            list = bookTable.values();
        } catch (Exception e) {
            // fallback: not implemented
        }
        return list;
    }

    // ---------------- Borrow ----------------
    public boolean borrowBook(int memberId, int bookId) {
        Member member = memberTable.get(memberId);
        Book book = bookTable.get(bookId);

        if (member == null || book == null) return false;
        if (!member.canBorrow()) return false;

        // if available -> give, else enqueue
        if (book.canBeBorrowed()) {
            boolean taken = book.borrowCopy();
            if (!taken) return false;

            // create loan record via member
            LoanRecord lr = member.borrowBook(book); // member.borrowBook creates LoanRecord and adds to activeLoans/history
            loanHistory.add(lr);

            // update heap (increaseKey)
            popularityHeap.increaseKey(book);

            // push undo action
            //UndoManager.getInstance().push(new UndoAction(... BORROW ...));

            return true;
        } else {
            // add to waitlist
            book.addToWaitList(member);
            return false;
        }
    }

    // ---------------- Return ----------------
    public boolean returnBook(int memberId, int bookId) {
        Member member = memberTable.get(memberId);
        Book book = bookTable.get(bookId);
        if (member == null || book == null) return false;
        if (!member.hasBook(book)) return false;

        // find active LoanRecord
        LoanRecord active = findActiveLoanRecord(member, book);
        if (active == null) return false;

        // mark returned (updates book, member and handles waitlist auto-assign)
        active.markReturned();

        // after return, if book given to next member, that next member.borrowBook already called inside markReturned
        // update popularity heap if necessary:
        popularityHeap.increaseKey(book);

        // if overdue -> add penalty to member
        int lateDays = active.calculateLateDays();
        if (lateDays > 0) {
            double fine = active.calculateFine();
            member.addPenalty(fine);
        }

        // push undo action: RETURN
        //UndoManager.getInstance().push(new UndoAction(... RETURN ...));

        return true;
    }

    // find active loan record
    private LoanRecord findActiveLoanRecord(Member member, Book book) {
        for (int i = 0; i < loanHistory.size(); i++) {
            LoanRecord r = loanHistory.get(i);
            if (!r.isReturned() && r.getMember().equals(member) && r.getBook().equals(book)) return r;
        }
        return null;
    }

    // ---------------- Popularity / Reports ----------------
    // Get top-K popular books (uses heap; note: getTopK uses extractMax but restores heap inside)
    public List<Book> getTopKPopular(int k) {
        return popularityHeap.getTopK(k);
    }

    // ---------------- Penalty / Admin ----------------
    public void payMemberPenalty(int memberId, double amount) {
        Member m = memberTable.get(memberId);
        if (m != null) m.payPenalty(amount);
    }

    // ---------------- Utility ----------------
    public Member getMember(int id) { return memberTable.get(id); }
    public Book getBook(int id) { return bookTable.get(id); }
}
