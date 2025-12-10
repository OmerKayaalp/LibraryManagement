package core;

import dataStructure.hashtable.HashTable;
import dataStructure.linkedList.MyLinkedList;
import dataStructure.heap.MaxHeap;
import dataStructure.queue.MyQueue;
import dataStructure.tree.TitleBST;
import models.Book;
import models.Member;
import models.LoanRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LibrarySystem {

    // --- Öğrenci ID'si (unique seed / hash salt) ---
    public static final int STUDENT_ID = 230302002; // <-- Kendi öğrenci ID'ni buraya koy

    // --- Temel veri yapıları ---
    private HashTable<Integer, Book> bookTable;
    private HashTable<Integer, Member> memberTable;
    private MyLinkedList<LoanRecord> loanHistory;
    private MaxHeap<Book> popularityHeap;
    private TitleBST titleIndex; // BST for title-based search

    // config (şu an LoanRecord içinde 14 gün kullanılıyor - istersen LoanRecord'a parametre ekleyebiliriz)
    private int defaultLoanDays = 14;

    // Constructor
    public LibrarySystem() {
        // HashTable salt ile oluşturulur (ödevde unique ID kullanımı gereği)
        this.bookTable = new HashTable<>(STUDENT_ID);
        this.memberTable = new HashTable<>(STUDENT_ID);
        this.loanHistory = new MyLinkedList<>();
        this.popularityHeap = new MaxHeap<>();
        this.titleIndex = new TitleBST();
    }

    // ---------------- Add / Remove ----------------

    /**
     * Public API: addBook -> records undo action
     */
    public void addBook(Book book) {
        if (book == null) return;
        addBookInternal(book);
        // push undo action to remove this book if undone
        UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.ADD_BOOK, this, book, null, null));
    }

    /**
     * Internal add: does not push undo (used by undo operations)
     */
    void addBookInternal(Book book) {
        if (book == null) return;
        bookTable.put(book.getBookId(), book);
        titleIndex.add(book);
        // insert into popularity heap (may create duplicates if same object inserted twice,
        // but for this assignment it's acceptable — could dedupe if needed)
        popularityHeap.insert(book);
    }

    /**
     * Public API: removeBook -> records undo action (stores removed Book)
     */
    public Book removeBook(int bookId) {
        Book removed = removeBookInternal(bookId);
        if (removed != null) {
            // push undo action to re-add the removed book
            UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.REMOVE_BOOK, this, removed, null, null));
        }
        return removed;
    }

    /**
     * Internal remove: does not push undo
     */
    Book removeBookInternal(int bookId) {
        Book b = bookTable.get(bookId);
        if (b == null) return null;
        bookTable.remove(bookId);
        try {
            titleIndex.remove(b);
        } catch (Exception ignored) {}
        // Note: popularityHeap removal is not implemented in MaxHeap; left as-is.
        return b;
    }

    /**
     * Public API: addMember -> push undo
     */
    public void addMember(Member m) {
        if (m == null) return;
        addMemberInternal(m);
        UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.ADD_MEMBER, this, null, m, null));
    }

    /**
     * Internal addMember: does not push undo
     */
    void addMemberInternal(Member m) {
        if (m == null) return;
        memberTable.put(m.getMemberID(), m);
    }

    /**
     * Public removeMember -> push undo
     */
    public Member removeMember(int memberId) {
        Member removed = removeMemberInternal(memberId);
        if (removed != null) {
            // push REMOVE_MEMBER (düzeltme: orijinalinde yanlışlıkla REMOVE_BOOK vardı)
            UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.REMOVE_MEMBER, this, null, removed, null));
        }
        return removed;
    }

    /**
     * Internal removeMember (no undo push)
     */
    Member removeMemberInternal(int memberId) {
        Member m = memberTable.get(memberId);
        if (m == null) return null;
        memberTable.remove(memberId);
        return m;
    }

    // ---------------- Search ----------------

    /**
     * Search book by ID via HashTable
     */
    public Book searchById(int id) {
        return bookTable.get(id);
    }

    /**
     * Search by title - uses TitleBST index (prefix search)
     */
    public List<Book> searchByTitle(String titlePrefix) {
        if (titlePrefix == null || titlePrefix.trim().isEmpty()) return new ArrayList<>();
        return titleIndex.searchByTitlePrefix(titlePrefix);
    }

    /**
     * Search by author (substring)
     */
    public List<Book> searchByAuthor(String authorQuery) {
        List<Book> results = new ArrayList<>();
        if (authorQuery == null || authorQuery.trim().isEmpty()) return results;
        String q = authorQuery.toLowerCase();

        MyLinkedList<Book> all = listAllBooks();
        for (int i = 0; i < all.size(); i++) {
            Book b = all.get(i);
            if (b == null) continue;
            if (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q)) {
                results.add(b);
            }
        }
        return results;
    }

    // ---------------- Borrow ----------------

    /**
     * Borrow book by memberId/bookId.
     * Returns true if borrowed immediately, false if placed on waitlist (or failure).
     */
    public boolean borrowBook(int memberId, int bookId) {
        Member member = memberTable.get(memberId);
        Book book = bookTable.get(bookId);

        if (member == null || book == null) return false;
        if (!member.canBorrow()) return false;

        if (book.canBeBorrowed()) {
            boolean taken = book.borrowCopy();
            if (!taken) return false;

            // create loan record via member (keeps member.activeBooks consistent)
            LoanRecord lr = member.borrowBook(book);
            if (lr == null) {
                // rollback book copy if member.borrowBook failed
                book.returnCopy();
                return false;
            }

            loanHistory.add(lr);

            // update popularity (heap)
            popularityHeap.increaseKey(book);

            // push undo action (BORROW) storing loanRecord
            UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.BORROW_BOOK, this, book, member, lr));

            return true;
        } else {
            // add to waitlist
            book.addToWaitList(member);
            return false;
        }
    }

    // ---------------- Return ----------------

    /**
     * Return book by memberId/bookId.
     * Returns true if successful.
     *
     * Implementation notes:
     * - To avoid recursion (LoanRecord.markReturned() calling member.returnBook() and LibrarySystem again),
     *   we use markReturnedWithoutMember() here and then explicitly update member via removeActiveLoanRecord().
     */
    public boolean returnBook(int memberId, int bookId) {
        Member member = memberTable.get(memberId);
        Book book = bookTable.get(bookId);
        if (member == null || book == null) return false;
        if (!member.hasBook(book)) return false;

        // find active LoanRecord
        LoanRecord active = findActiveLoanRecord(member, book);
        if (active == null) return false;

        // mark returned on record & update book, but DO NOT call member.returnBook() from inside LoanRecord
        // use the safe helper that only sets returned + book.returnCopy()
        try {
            active.markReturnedWithoutMember();
        } catch (Exception e) {
            // if anything unexpected happens, fail gracefully
            return false;
        }

        // now remove the active loan from member WITHOUT calling lr.markReturned() again
        member.removeActiveLoanRecord(active);

        // Update popularity
        popularityHeap.increaseKey(book);

        // calculate penalties if any
        int lateDays = active.calculateLateDays();
        if (lateDays > 0) {
            double fine = active.calculateFine();
            member.addPenalty(fine);
        }

        // After return, assign to next waiting member automatically (if any)
        Member next = book.getNextWaitingMember();
        if (next != null) {
            if (next.canBorrow()) {
                boolean borrowed = book.borrowCopy();
                if (borrowed) {
                    LoanRecord newLr = next.borrowBook(book);
                    if (newLr != null) {
                        loanHistory.add(newLr);
                        popularityHeap.increaseKey(book);
                        // push undo for this automatic borrow
                        UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.BORROW_BOOK, this, book, next, newLr));
                    } else {
                        // rollback if necessary
                        book.returnCopy();
                    }
                } else {
                    // unable to borrow (race) -> re-enqueue
                    book.addToWaitList(next);
                }
            } else {
                // next cannot borrow; skipping (could notify or re-enqueue later)
            }
        }

        // push undo for return (so we can re-borrow if needed)
        UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.RETURN_BOOK, this, book, member, active));

        return true;
    }

    // find active loan record in global loanHistory (non-returned)
    private LoanRecord findActiveLoanRecord(Member member, Book book) {
        if (member == null || book == null) return null;

        for (int i = 0; i < loanHistory.size(); i++) {
            LoanRecord r = loanHistory.get(i);
            if (!r.isReturned() && r.getMember().equals(member) && r.getBook().equals(book)) return r;
        }
        // Fallback: check member.loanHistory (if accessible)
        try {
            for (int i = 0; i < member.getLoanHistory().size(); i++) {
                LoanRecord r = member.getLoanHistory().get(i);
                if (!r.isReturned() && r.getMember().equals(member) && r.getBook().equals(book)) return r;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ---------------- Popularity / Reports ----------------

    /**
     * Get top-K popular books using heap
     */
    public List<Book> getTopKPopular(int k) {
        if (k <= 0) return new ArrayList<>();
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

    public MyLinkedList<Book> listAllBooks() {
        return bookTable.values();
    }

    public MyLinkedList<Member> listAllMembers() {
        return memberTable.values();
    }

    // ---------------- Additional helpers added ----------------

    /**
     * Return the waitlist for a book (or null if book not found)
     */
    public MyQueue<Member> getWaitList(int bookId) {
        Book b = bookTable.get(bookId);
        if (b == null) return null;
        return b.getWaitList();
    }

    /**
     * Simple undo wrapper used by UI
     */
    public boolean undo() {
        UndoManager um = UndoManager.getInstance();
        if (!um.hasUndo()) return false;
        um.undo(); // UndoManager.undo() should perform the actual undo
        return true;
    }

    /**
     * Interactive search menu for console UI.
     * Uses substring matching (case-insensitive) on title/author/isbn/category.
     */
    public void searchBookMenu() {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n--- BOOK SEARCH MENU ---");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by Author");
        System.out.println("3. Search by Any Field (title/author/isbn/category)");
        System.out.print("Select: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return;
        }

        System.out.print("Enter search text: ");
        String query = sc.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println("Search text cannot be empty!");
            return;
        }
        String q = query.toLowerCase();

        List<Book> results = new ArrayList<>();
        MyLinkedList<Book> allBooks = listAllBooks();

        for (int i = 0; i < allBooks.size(); i++) {
            Book b = allBooks.get(i);
            if (b == null) continue;

            switch (choice) {
                case 1:
                    if (b.getTitle() != null && b.getTitle().toLowerCase().contains(q)) results.add(b);
                    break;
                case 2:
                    if (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q)) results.add(b);
                    break;
                case 3:
                    if (b.matches(q)) results.add(b);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
        }

        if (results.isEmpty()) {
            System.out.println("No books found matching: " + query);
            return;
        }

        System.out.println("\n--- Search Results ---");
        for (Book r : results) {
            System.out.println(
                "ID: " + r.getBookId()
                + " | Title: " + r.getTitle()
                + " | Author: " + r.getAuthor()
                + " | Available: " + r.getAvailableCopies()
            );
        }
    }
}
