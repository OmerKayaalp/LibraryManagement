package core;

import models.Book;
import models.Member;
import models.LoanRecord;

/**
 * UndoAction: tek bir işlemi (add/remove/borrow/return vs.) geri almak için gerekli veriyi tutar
 * ve undo() ile geri almayı gerçekleştirir.
 *
 * Bu sınıf LibrarySystem içindeki "Internal" metotları (addBookInternal, removeBookInternal, addMemberInternal, removeMemberInternal)
 * kullanır; böylece undo işlemi sırasında yeniden UndoManager'a push edilmeyi engelleriz.
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

    public UndoAction(ActionType type, LibrarySystem library, Book book, Member member, LoanRecord loanRecord) {
        this.type = type;
        this.library = library;
        this.book = book;
        this.member = member;
        this.loanRecord = loanRecord;
    }

    /**
     * Geri al (undo) işlemini gerçekleştirir.
     * Uygulanabilir en güvenli/ölçeklenebilir adımlarla objeleri manipüle eder.
     */
    public void undo() {
        if (library == null) return;

        switch (type) {
            case ADD_BOOK:
                // ADD_BOOK -> daha önce addBook() ile eklenen kitabı sil
                if (book != null) {
                    try {
                        library.removeBookInternal(book.getBookId());
                    } catch (Exception e) {
                        // toleranslı davran: hata logla ama throw etme
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
                // BORROW_BOOK -> borrow işlemini geri al: loanRecord varsa onu "iptal et"
                // - loanRecord.markReturnedWithoutMember() çağr (sadece book kopyasını geri alır)
                // - member.removeActiveLoanRecord(loanRecord) çağr (üye listelerinden kaldırır)
                // NOT: loanHistory global listeye müdahale edilmeyebilir (isteğe bağlı)
                if (loanRecord != null && member != null && book != null) {
                    try {
                        // Önce loanRecord'u returned olarak işaretle (book copy geri gelir)
                        // Bu metodun LoanRecord içinde implement edilmiş olması gerekir:
                        // markReturnedWithoutMember() veya artık markReturned() member çağrısı yapmayacak şekilde.
                        loanRecord.markReturnedWithoutMember();

                        // Üyenin aktif loan/kopya listesinden kaydı sil
                        member.removeActiveLoanRecord(loanRecord);

                        // Eğer istenirse loanHistory'den temizlenebilir - burada sadece toleranslı davranıyoruz
                    } catch (Exception e) {
                        System.err.println("Undo(BORROW_BOOK) failed: " + e.getMessage());
                    }
                } else {
                    // fallback: eğer loanRecord yoksa (nadiren) book ve member nesneleri üzerinden basit geri alma
                    if (book != null && member != null) {
                        try {
                            // Eğer üye aktif kitaba sahipse kaldır
                            if (member.hasBook(book)) {
                                // removeActiveLoanRecord metodu member içinde mevcut olmalı
                                member.removeActiveLoanRecord(findLoanForMemberBook(member, book));
                            }
                            // geri dönüş kopyasını sağla
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
