/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;
import java.time.LocalDate;

public class LoanRecord {
    private Member member;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private boolean isReturned;

    public LoanRecord(Member member, Book book) {
        this.member = member;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.returnDate = null;
        this.isReturned = false;
    }

    public Member getMember() {
        return member;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }
    public boolean isReturned(){
    return isReturned;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public void markReturned(){
            if(!isReturned){
        this.isReturned=true;
        this.returnDate=LocalDate.now();}
            
        book.markReturned();
        member.returnBook(book);
                
    }    
}
