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
import java.util.Random;

/**
 * LibrarySystem - Main system class that manages all library operations.
 * 
 * DATA STRUCTURES USED (as per project requirements):
 * 1. HashTable<Integer, Book> bookTable - O(1) average lookup by book ID
 * 2. HashTable<Integer, Member> memberTable - O(1) average lookup by member ID
 * 3. MyLinkedList<LoanRecord> loanHistory - Dynamic list for loan history tracking
 * 4. MaxHeap<Book> popularityHeap - O(log n) insert, O(k log n) for top-K popular books
 * 5. TitleBST titleIndex - O(log n) search by title prefix
 * 6. MyQueue<Member> waitList (inside Book) - FIFO for fair waitlist management
 * 7. MyStack<UndoAction> (in UndoManager) - LIFO for undo operations
 * 
 * COMPLEXITY ANALYSIS:
 * - Search by ID: O(1) average (HashTable)
 * - Search by Title: O(log n) average (BST)
 * - Add Book/Member: O(1) average (HashTable) + O(log n) for heap/BST
 * - Borrow/Return: O(1) average (HashTable lookup) + O(log n) heap update
 * - Get Top-K Popular: O(k log n) where k is number requested
 * - Undo: O(1) stack pop + operation-specific complexity
 */
public class LibrarySystem {

    /**
     * UNIQUE STUDENT ID INTEGRATION:
     * Used as hash salt for HashTable to ensure unique hash distribution.
     * Also used as random seed for generating test data (if needed).
     * This ensures each submission has unique behavior patterns.
     */
    public static final int STUDENT_ID = 230302002;
    
    /**
     * Random generator seeded with STUDENT_ID for unique data generation.
     * This ensures reproducible but unique test data across different submissions.
     */
    private static final Random RANDOM_GEN = new Random(STUDENT_ID);

    /**
     * DATA STRUCTURE 1: HashTable for Books
     * Purpose: Fast O(1) average lookup by book ID
     * Why: Books are frequently accessed by unique ID. HashTable provides optimal performance.
     */
    private HashTable<Integer, Book> bookTable;
    
    /**
     * DATA STRUCTURE 2: HashTable for Members
     * Purpose: Fast O(1) average lookup by member ID
     * Why: Members are frequently accessed by unique ID. HashTable provides optimal performance.
     */
    private HashTable<Integer, Member> memberTable;
    
    /**
     * DATA STRUCTURE 3: LinkedList for Loan History
     * Purpose: Maintain chronological list of all loan records
     * Why: Loan history grows dynamically and needs sequential access. LinkedList is efficient for this.
     */
    private MyLinkedList<LoanRecord> loanHistory;
    
    /**
     * DATA STRUCTURE 4: MaxHeap for Popular Books
     * Purpose: Efficiently track and retrieve most popular books
     * Why: Heap provides O(log n) insert and O(k log n) for top-K retrieval, optimal for popularity tracking.
     */
    private MaxHeap<Book> popularityHeap;
    
    /**
     * DATA STRUCTURE 5: BST for Title Search
     * Purpose: Maintain sorted index for efficient title-based search
     * Why: BST provides O(log n) search by title prefix, better than linear search O(n).
     */
    private TitleBST titleIndex;

    private int defaultLoanDays = 14;

    /**
     * Constructor: Initializes all data structures with STUDENT_ID as hash salt.
     * Time Complexity: O(1) - constant initialization
     */
    public LibrarySystem() {
        // HashTable uses STUDENT_ID as salt to ensure unique hash distribution
        this.bookTable = new HashTable<>(STUDENT_ID);
        this.memberTable = new HashTable<>(STUDENT_ID);
        this.loanHistory = new MyLinkedList<>();
        this.popularityHeap = new MaxHeap<>();
        this.titleIndex = new TitleBST();
    }

    // ---------------- Add / Remove ----------------

    /**
     * Add a book to the library catalog.
     * Time Complexity: O(1) average (HashTable put) + O(log n) (BST insert + Heap insert)
     * 
     * @param book The book to add
     */
    public void addBook(Book book) {
        if (book == null) return;
        addBookInternal(book);
        // Record undo action: if undone, this book will be removed
        UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.ADD_BOOK, this, book, null, null));
    }

    /**
     * Internal add: does not push undo (used by undo operations to avoid infinite recursion).
     * Time Complexity: O(1) average (HashTable) + O(log n) (BST + Heap)
     * 
     * @param book The book to add internally
     */
    void addBookInternal(Book book) {
        if (book == null) return;
        // Add to HashTable for O(1) ID lookup
        bookTable.put(book.getBookId(), book);
        // Add to BST for O(log n) title search
        titleIndex.add(book);
        // Add to MaxHeap for popularity tracking (O(log n))
        popularityHeap.insert(book);
    }

    /**
     * Remove a book from the library catalog.
     * Time Complexity: O(1) average (HashTable) + O(log n) (BST remove)
     * 
     * @param bookId The ID of the book to remove
     * @return The removed book, or null if not found
     */
    public Book removeBook(int bookId) {
        Book removed = removeBookInternal(bookId);
        if (removed != null) {
            // Record undo action: if undone, this book will be re-added
            UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.REMOVE_BOOK, this, removed, null, null));
        }
        return removed;
    }

    /**
     * Internal remove: does not push undo (used by undo operations).
     * Time Complexity: O(1) average (HashTable) + O(log n) (BST)
     * 
     * @param bookId The ID of the book to remove
     * @return The removed book, or null if not found
     */
    Book removeBookInternal(int bookId) {
        Book b = bookTable.get(bookId);
        if (b == null) return null;
        // Remove from HashTable
        bookTable.remove(bookId);
        // Remove from BST title index
        try {
            titleIndex.remove(b);
        } catch (Exception ignored) {}
        // Note: Heap removal not implemented (would require O(n) search). 
        // This is acceptable as heap is used for top-K queries, not exact removal.
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
     * Search book by ID using HashTable.
     * Time Complexity: O(1) average case
     * 
     * @param id The book ID to search for
     * @return The book if found, null otherwise
     */
    public Book searchById(int id) {
        return bookTable.get(id);
    }

    /**
     * Search books by title prefix using BST index.
     * Time Complexity: O(log n + m) where m is number of matches
     * 
     * @param titlePrefix The title prefix to search for
     * @return List of books matching the prefix
     */
    public List<Book> searchByTitle(String titlePrefix) {
        if (titlePrefix == null || titlePrefix.trim().isEmpty()) return new ArrayList<>();
        return titleIndex.searchByTitlePrefix(titlePrefix);
    }

    /**
     * Search books by author (substring match).
     * Time Complexity: O(n) where n is total number of books
     * Note: Could be optimized with separate BST or HashTable, but linear search is acceptable for this scale.
     * 
     * @param authorQuery The author name (substring) to search for
     * @return List of books by matching authors
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
     * Borrow a book for a member.
     * Time Complexity: O(1) average (HashTable lookup) + O(log n) (heap update)
     * 
     * If book is available: immediately borrows and creates loan record.
     * If book is unavailable: adds member to waitlist queue (FIFO).
     * 
     * @param memberId The member ID
     * @param bookId The book ID
     * @return true if borrowed immediately, false if waitlisted or failed
     */
    public boolean borrowBook(int memberId, int bookId) {
        // O(1) HashTable lookup
        Member member = memberTable.get(memberId);
        Book book = bookTable.get(bookId);

        if (member == null || book == null) return false;
        if (!member.canBorrow()) return false;

        if (book.canBeBorrowed()) {
            // Book available: borrow immediately
            boolean taken = book.borrowCopy();
            if (!taken) return false;

            // Create loan record (adds to member's activeBooks LinkedList)
            LoanRecord lr = member.borrowBook(book);
            if (lr == null) {
                // Rollback if member borrow failed
                book.returnCopy();
                return false;
            }

            // Add to global loan history (LinkedList)
            loanHistory.add(lr);

            // Update popularity heap (O(log n))
            popularityHeap.increaseKey(book);

            // Record undo action
            UndoManager.getInstance().push(new UndoAction(UndoAction.ActionType.BORROW_BOOK, this, book, member, lr));

            return true;
        } else {
            // Book unavailable: add to waitlist queue (FIFO - fair first-come-first-served)
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
                        System.out.println("Kitap sıradaki üyeye verildi: " + next.getName() + " (ID: " + next.getMemberID() + ")");
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
     * Get top-K most popular books using MaxHeap.
     * Time Complexity: O(k log n) where k is number requested, n is total books
     * 
     * Uses MaxHeap to efficiently retrieve books sorted by popularity count.
     * This is optimal because heap maintains max element at root, allowing
     * efficient extraction of top-K elements without sorting entire collection.
     * 
     * @param k Number of popular books to retrieve
     * @return List of top-K popular books (sorted by popularity descending)
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
    public String undo() {
        UndoManager um = UndoManager.getInstance();
        if (!um.hasUndo()) return null;
        return um.undo(); // returns description
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
