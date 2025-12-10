package models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LoanRecord {

    private final Book book;
    private final Member member;
    private final LocalDate borrowDate;
    private LocalDate returnDate;
    private boolean returned;

    // default gün sayısı (istersen constructor ile değiştirebilirsin)
    private static final int DEFAULT_LOAN_DAYS = 14;
    // günlük ceza (TL veya istediğin birim)
    private static final double FINE_PER_DAY = 2.0;

    public LoanRecord(Book book, Member member) {
        this.book = book;
        this.member = member;
        this.borrowDate = LocalDate.now();
        this.returned = false;
        this.returnDate = null;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    /** Kitabın son teslim tarihi (varsayılan 14 gün) */
    public LocalDate getDueDate() {
        return borrowDate.plusDays(DEFAULT_LOAN_DAYS);
    }

    /**
     * Normal iade: Book ve Member güncellemeleri yapılır.
     * LibrarySystem içinden çağrıldığında döngü yaratmamak için dikkatli kullan.
     */
    public void markReturned() {
        if (returned) return;

        this.returned = true;
        this.returnDate = LocalDate.now();

        // kitap kopyasını geri al
        book.returnCopy();

        // üyeyi güncelle (üye aktif kitaptan çıkarılır)
        member.returnBook(book);

        // bekleme listesine otomatik geçiş
        Member next = book.getNextWaitingMember();
        if (next != null && next.canBorrow()) {
            boolean borrowed = book.borrowCopy();
            if (borrowed) {
                next.borrowBook(book); // yeni LoanRecord oluşturur ve üyeyi günceller
            } else {
                book.addToWaitList(next);
            }
        }
    }

    /**
     * Undo için: member'ı değiştirmeden sadece book kopyasını geri alır.
     */
    public void markReturnedWithoutMember() {
        if (returned) return;

        this.returned = true;
        this.returnDate = LocalDate.now();

        // sadece book copy iade edilsin
        book.returnCopy();
    }

    /**
     * Gecikme gün sayısını hesaplar.
     * Eğer henüz iade edilmemişse 0 döner.
     */
    public int calculateLateDays() {
        if (!isReturned()) return 0;
        LocalDate due = getDueDate();
        LocalDate ret = getReturnDate();
        if (ret == null) return 0;
        if (ret.isAfter(due)) {
            return (int) ChronoUnit.DAYS.between(due, ret);
        }
        return 0;
    }

    /**
     * Ceza hesaplama: gecikme gün sayısı * günlük ceza.
     * LibrarySystem tarafındaki çağrı active.calculateFine() ile uyumludur.
     */
    public double calculateFine() {
        int late = calculateLateDays();
        if (late <= 0) return 0.0;
        return late * FINE_PER_DAY;
    }

    @Override
    public String toString() {
        return "LoanRecord{" +
                "member=" + (member != null ? member.getName() : "null") +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + getDueDate() +
                ", returnDate=" + returnDate +
                ", returned=" + returned +
                '}';
    }
}
