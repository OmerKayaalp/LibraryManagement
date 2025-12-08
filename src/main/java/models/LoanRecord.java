package models;

import java.util.Date;

public class LoanRecord {

    private Member member;
    private Book book;
    private Date borrowDate;
    private Date returnDate;
    private boolean returned;

    public LoanRecord(Member member, Book book) {
        this.member = member;
        this.book = book;
        this.borrowDate = new Date();
        this.returned = false;

        // İşlemler
        book.borrowCopy();
        member.borrowBook(book);
    }

    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public boolean isReturned() { return returned; }

    // ----- KİTAP İADE EDİLİYOR -----
    public void markReturned() {
        if (returned) return;

        returned = true;
        returnDate = new Date();

        // 1) Üye kitabı bırakır
        member.returnBook(book);

        // 2) Kitap kopyası iade edilir
        book.returnCopy();

        // 3) Sıradaki üye varsa otomatik verir
        Member next = book.getNextWaitingMember();

        if (next != null) {
            next.borrowBook(book);
            book.borrowCopy();
        }
    }
}
