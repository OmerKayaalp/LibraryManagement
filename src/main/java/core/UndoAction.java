package core;

import models.Book;
import models.Member;
import models.LoanRecord;
import core.LibrarySystem;
import core.UndoManager;

public class UndoAction {
    public enum ActionType { ADD_BOOK, REMOVE_BOOK, ADD_MEMBER, BORROW_BOOK, RETURN_BOOK }

    private ActionType type;
    private Book book;
    private Member member;
    private LoanRecord loanRecord;
    private LibrarySystem library;

    public UndoAction(ActionType type, LibrarySystem library, Book book, Member member, LoanRecord loanRecord) {
        this.type = type;
        this.library = library;
        this.book = book;
        this.member = member;
        this.loanRecord = loanRecord;
    }

    public void undo() {
        switch (type) {
            case ADD_BOOK:
                if (book != null) library.removeBook(book.getId());
                break;
            case REMOVE_BOOK:
                if (book != null) library.addBook(book);
                break;
            case ADD_MEMBER:
                if (member != null) {
                    // implement member removal in LibrarySystem if needed
                }
                break;
            case BORROW_BOOK:
                if (loanRecord != null) {
                    // reverse borrow: force return without penalizing
                    loanRecord.markReturned();
                }
                break;
            case RETURN_BOOK:
                // Harder: re-create borrow: attempt to borrow book by member
                if (member != null && book != null) {
                    library.borrowBook(member.getMemberID(), book.getId());
                }
                break;
        }
    }
}
