package core;

import models.Book;
import models.Member;
import models.LoanRecord;

/**
 * UndoAction - Represents a single reversible operation in the library system.
 * 
 * PURPOSE: Store sufficient information to reverse an operation (add/remove/borrow/return).
 * Used by UndoManager to implement undo functionality.
 * 
 * DESIGN PATTERN: Command Pattern
 * Each action stores the operation type and necessary data to reverse it.
 * 
 * IMPORTANT: Uses "Internal" methods (addBookInternal, removeBookInternal, etc.)
 * to avoid infinite recursion - undo operations don't push new undo actions.
 * 
 * COMPLEXITY:
 * - undo(): O(1) to O(log n) depending on operation type
 * - Storage: O(1) per action
 */
public class UndoAction {

    public enum ActionType {
        ADD_BOOK,
        REMOVE_BOOK,
        ADD_MEMBER,
        REMOVE_MEMBER,
        BORROW_BOOK,
        RETURN_BOOK
    }

    private final ActionType type;
    private final LibrarySystem library;
    private final Book book;           // ilgili kitap (varsa)
    private final Member member;       // ilgili üye (varsa)
    private final LoanRecord loanRecord; // ilgili loan (BORROW/RETURN için)
    private String description;        // yapılan işlemi anlatan metin

    public UndoAction(ActionType type, LibrarySystem library, Book book, Member member, LoanRecord loanRecord) {
        this.type = type;
        this.library = library;
        this.book = book;
        this.member = member;
        this.loanRecord = loanRecord;
        this.description = buildDescription();
    }

    /**
     * Execute the undo operation to reverse the original action.
     * 
     * EDGE CASE HANDLING:
     * - Null library: Returns early
     * - Null objects: Checks before operations
     * - Exceptions: Catches and logs, doesn't crash system
     * 
     * Time Complexity: Varies by operation type:
     * - ADD_BOOK/REMOVE_BOOK: O(1) HashTable + O(log n) BST
     * - BORROW_BOOK/RETURN_BOOK: O(1) to O(n) depending on member's active books
     */
    public void undo() {
        if (library == null) return;

        switch (type) {
            case ADD_BOOK:
                // Reverse ADD_BOOK: remove the book that was added
                // EDGE CASE: Book might have been removed already
                if (book != null) {
                    try {
                        library.removeBookInternal(book.getBookId());
                    } catch (Exception e) {
                        // Graceful error handling: log but don't crash
                        System.err.println("Undo(ADD_BOOK) failed: " + e.getMessage());
                    }
                }
                break;

            case REMOVE_BOOK:
                // REMOVE_BOOK -> daha önce remove edildiği için kitabı geri ekle
                if (book != null) {
                    try {
                        library.addBookInternal(book);
                    } catch (Exception e) {
                        System.err.println("Undo(REMOVE_BOOK) failed: " + e.getMessage());
                    }
                }
                break;

            case ADD_MEMBER:
                // ADD_MEMBER -> eklenen üyeyi sil
                if (member != null) {
                    try {
                        library.removeMemberInternal(member.getMemberID());
                    } catch (Exception e) {
                        System.err.println("Undo(ADD_MEMBER) failed: " + e.getMessage());
                    }
                }
                break;

            case REMOVE_MEMBER:
                // REMOVE_MEMBER -> silinen üyeyi geri ekle
                if (member != null) {
                    try {
                        library.addMemberInternal(member);
                    } catch (Exception e) {
                        System.err.println("Undo(REMOVE_MEMBER) failed: " + e.getMessage());
                    }
                }
                break;

            case BORROW_BOOK:
                // Reverse BORROW_BOOK: cancel the loan
                // EDGE CASE: LoanRecord might be null (rare), use fallback
                if (loanRecord != null && member != null && book != null) {
                    try {
                        // Mark loan as returned (returns book copy without member update)
                        loanRecord.markReturnedWithoutMember();
                        // Remove from member's active books list
                        member.removeActiveLoanRecord(loanRecord);
                        // Note: Loan history kept for record-keeping purposes
                    } catch (Exception e) {
                        System.err.println("Undo(BORROW_BOOK) failed: " + e.getMessage());
                    }
                } else {
                    // FALLBACK: If loanRecord missing, use book/member directly
                    // EDGE CASE HANDLING: Graceful degradation
                    if (book != null && member != null) {
                        try {
                            if (member.hasBook(book)) {
                                member.removeActiveLoanRecord(findLoanForMemberBook(member, book));
                            }
                            book.returnCopy();
                        } catch (Exception e) {
                            System.err.println("Undo(BORROW_BOOK) fallback failed: " + e.getMessage());
                        }
                    }
                }
                break;

            case RETURN_BOOK:
                // RETURN_BOOK -> bir iade işlemini geri almak: kitabı tekrar ödünç ver
                // Burada dikkat: eğer kitap şu an müsaitse yeniden al, değilse bekleme listesine al.
                if (member != null && book != null) {
                    try {
                        if (book.canBeBorrowed()) {
                            boolean taken = book.borrowCopy();
                            if (taken) {
                                // üye seviyesinde borrow yap (member.borrowBook kitap için yeni LoanRecord oluşturur)
                                LoanRecord newLoan = member.borrowBook(book);
                                // yeni loan record'u library'nin loanHistory'sine ekleyebiliriz eğer erişim varsa.
                                // Örneğin library.loanHistory public değilse, bu adımı atıyoruz.
                            } else {
                                // eğer çekilemediyse, bekleme listesine koy
                                book.addToWaitList(member);
                            }
                        } else {
                            // book müsait değil -> bekleme listesine koy
                            book.addToWaitList(member);
                        }
                    } catch (Exception e) {
                        System.err.println("Undo(RETURN_BOOK) failed: " + e.getMessage());
                    }
                }
                break;

            default:
                System.err.println("Unsupported undo action: " + type);
        }
    }

    /**
     * İnsan okunur açıklama üretir.
     */
    public String getDescription() {
        if (description == null || description.isEmpty()) {
            description = buildDescription();
        }
        return description;
    }

    private String buildDescription() {
        String bookInfo = book != null ? book.getTitle() : "";
        String memberInfo = member != null ? member.getName() : "";
        return switch (type) {
            case ADD_BOOK -> "Kitap ekleme geri alındı: " + bookInfo;
            case REMOVE_BOOK -> "Kitap silme geri alındı: " + bookInfo;
            case ADD_MEMBER -> "Üye ekleme geri alındı: " + memberInfo;
            case REMOVE_MEMBER -> "Üye silme geri alındı: " + memberInfo;
            case BORROW_BOOK -> "Ödünç alma geri alındı: " + bookInfo + " <- " + memberInfo;
            case RETURN_BOOK -> "İade geri alındı: " + bookInfo + " -> " + memberInfo;
            default -> "İşlem geri alındı.";
        };
    }

    /**
     * Basit yardımcı: üye ve kitap üzerinden mevcut loan record'u tespit etmeye çalışır.
     * Eğer member.getLoanHistory() veya member.activeLoans erişilebilirse işe yarar.
     */
    private LoanRecord findLoanForMemberBook(Member member, Book book) {
        try {
            // önce aktif loanları kontrol et
            for (int i = 0; i < member.getLoanHistory().size(); i++) {
                LoanRecord lr = member.getLoanHistory().get(i);
                if (lr.getBook().equals(book) && !lr.isReturned()) return lr;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
