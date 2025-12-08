package models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LoanRecord {
    private Member member;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean isReturned;

    // default loan period in days (configurable if needed)
    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final double DAILY_FINE = 2.5; // currency per day

    public LoanRecord(Member member, Book book) {
        this(member, book, DEFAULT_LOAN_DAYS);
    }

    public LoanRecord(Member member, Book book, int loanDays) {
        this.member = member;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(loanDays);
        this.returnDate = null;
        this.isReturned = false;
    }

    // --- getters ---
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isReturned() { return isReturned; }

    // --- overdue / fine helpers ---
    public boolean isOverdue() {
        if (isReturned) {
            return returnDate.isAfter(dueDate);
        } else {
            return LocalDate.now().isAfter(dueDate);
        }
    }

    public int calculateLateDays() {
        LocalDate end = isReturned ? returnDate : LocalDate.now();
        if (end.isAfter(dueDate)) {
            return (int) ChronoUnit.DAYS.between(dueDate, end);
        }
        return 0;
    }

    public double calculateFine() {
        return calculateLateDays() * DAILY_FINE;
    }

    // --- mark returned (update book, member and possibly assign to next waitlist member) ---
    public void markReturned() {
        if (isReturned) return;
        this.isReturned = true;
        this.returnDate = LocalDate.now();

        // book updated
        book.returnCopy();

        // member updated: remove active loan / active book
        member.returnBook(book);

        // if there is a waitlist, automatically loan to next member (if they can borrow)
        Member next = book.getNextWaitingMember();
        if (next != null) {
            // only give book to next if next can borrow (respect member limits/penalties)
            if (next.canBorrow()) {
                boolean borrowed = book.borrowCopy();
                if (borrowed) {
                    next.borrowBook(book); // creates LoanRecord inside Member.borrowBook
                } else {
                    // if borrow failed (race condition), optionally re-enqueue next
                    book.addToWaitList(next);
                }
            } else {
                // next cannot borrow — skip and possibly re-enqueue or notify
                // For simplicity, do nothing (could re-enqueue)
            }
        }
    }
}
