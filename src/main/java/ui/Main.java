package ui;
// Main.java
import core.LibrarySystem;
import models.Book;
import models.Member;
import models.LoanRecord;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting library system test...");

        LibrarySystem system = new LibrarySystem();

        // 1) Add members
        Member m1 = new Member(1, "Ahmet Y");
        Member m2 = new Member(2, "Mehmet D");
        Member m3 = new Member(3, "Ayse K");

        system.addMember(m1);
        system.addMember(m2);
        system.addMember(m3);

        System.out.println("Members added: 1,2,3");

        // 2) Add books (use the Book constructor signature from your code)
        Book b1 = new Book(101, "9780135166307", "Data Structures", "Mark Weiss", "CS", 2019, 928, 2);
        Book b2 = new Book(102, "9780134685991", "Clean Code", "Robert C. Martin", "Software", 2008, 464, 1);
        Book b3 = new Book(103, "9780596009205", "Head First Java", "Kathy Sierra", "Java", 2005, 720, 3);

        system.addBook(b1);
        system.addBook(b2);
        system.addBook(b3);

        System.out.println("Books added: 101,102,103");

        // 3) Borrowing tests
        System.out.println("\n-- Borrow tests --");
        boolean ok;

        ok = system.borrowBook(1, 101); // member 1 borrows book 101
        System.out.println("member1 borrow book101 -> " + ok);

        ok = system.borrowBook(2, 101); // member 2 borrows book 101 (second copy)
        System.out.println("member2 borrow book101 -> " + ok);

        ok = system.borrowBook(3, 101); // member 3 tries, should be enqueued on waitlist (returns false)
        System.out.println("member3 borrow book101 (expect false/waitlist) -> " + ok);

        // Check waitlist existence through Book method
        Book fetched = system.getBook(101);
        System.out.println("book101 available copies after borrows: " + fetched.getAvailableCopies());
        System.out.println("book101 has waitlist? " + fetched.hasWaitList());

        // 4) Return test (this should auto-assign to next waiting member)
        System.out.println("\n-- Return test --");
        ok = system.returnBook(1, 101); // member1 returns book101 -> member3 should get it automatically
        System.out.println("member1 returned book101 -> " + ok);

        // Confirm member3 now has book101
        Member member3 = system.getMember(3);
        System.out.println("member3 has book101? " + member3.hasBook(b1));

        // 5) Popularity / heap test: borrow some books to change popularity
        System.out.println("\n-- Popularity test --");
        system.borrowBook(1, 103);
        system.borrowBook(2, 103);
        system.borrowBook(1, 102); // borrow Clean Code

        List<Book> top2 = system.getTopKPopular(2);
        System.out.println("Top 2 popular books:");
        for (Book bk : top2) {
            System.out.println("  id=" + bk.getId() + " title=\"" + bk.getTitle() + "\" popularity=" + bk.getPopularityCount());
        }

        // 6) Penalty test: simulate adding penalty and paying it
        System.out.println("\n-- Penalty test --");
        Member member1 = system.getMember(1);
        System.out.println("member1 penalty before: " + member1.getPenaltyBalance());
        member1.addPenalty(15.0); // add penalty directly to member
        System.out.println("member1 penalty after add: " + member1.getPenaltyBalance());
        system.payMemberPenalty(1, 5.0);
        System.out.println("member1 penalty after paying 5.0 via system: " + member1.getPenaltyBalance());

        // 7) Search by title test (uses searchByTitle method)
        System.out.println("\n-- Search test --");
        List<Book> found = system.searchByTitle("clean");
        System.out.println("searchByTitle(\"clean\") found " + found.size() + " results");
        for (Book bk : found) {
            System.out.println("  found id=" + bk.getId() + " title=\"" + bk.getTitle() + "\"");
        }

        // 8) Remove book test
        System.out.println("\n-- Remove book test --");
        Book removed = system.removeBook(102);
        System.out.println("removed book id 102 -> " + (removed != null));
        Book checkRemoved = system.getBook(102);
        System.out.println("getBook(102) after removal -> " + (checkRemoved == null));

        // 9) Final status summary
        System.out.println("\n-- Final status --");
        System.out.println("Book101 available copies: " + system.getBook(101).getAvailableCopies());
        System.out.println("Book103 available copies: " + system.getBook(103).getAvailableCopies());
        System.out.println("Member1 active loans count: " + system.getMember(1).getActiveLoanCount());
        System.out.println("Member3 active loans count: " + system.getMember(3).getActiveLoanCount());

        System.out.println("\nLibrary system test finished.");
    }
}
