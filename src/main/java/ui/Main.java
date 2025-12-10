package ui;

import core.LibrarySystem;
import models.Book;
import models.Member;
import models.LoanRecord;
import dataStructure.linkedList.MyLinkedList;
import dataStructure.queue.MyQueue;

import java.util.Random;1

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final Random rng = new Random();
    private static final LibrarySystem library = new LibrarySystem();

    public static void main(String[] args) {
        System.out.println("Welcome to the Library Management System");

        while (true) {
            printMenu();
            int choice = readInt("Your choice: ");

            switch (choice) {
                case 1 -> addMember();
                case 2 -> addBook();
                case 3 -> borrowBook();
                case 4 -> returnBook();
                case 5 -> showMemberHistory();
                case 6 -> showBookStatus();
                case 7 -> showWaitlist();
                case 8 -> searchBooks();
                case 9 -> undo();
                case 10 -> listAllMembers();
                case 11 -> listAllBooks();
                case 12 -> {
                    System.out.println("Exiting... Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY SYSTEM MENU =====");
        System.out.println("1. Add Member");
        System.out.println("2. Add Book");
        System.out.println("3. Borrow Book (by IDs)");
        System.out.println("4. Return Book (by IDs)");
        System.out.println("5. Show Member History (by memberId)");
        System.out.println("6. Show Book Status (by bookId)");
        System.out.println("7. Show Book Waitlist (by bookId)");
        System.out.println("8. Search Books (title/author/any)");
        System.out.println("9. UNDO Last Operation");
        System.out.println("10. List all Members");
        System.out.println("11. List all Books");
        System.out.println("12. Exit");
    }

    // -------------------- MENU OPERATIONS -----------------------

    private static void addMember() {
        String name = readString("Enter member name: ").trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        String idLine = readString("Enter member ID (or press Enter to auto-generate): ").trim();
        int id;
        if (idLine.isEmpty()) {
            id = generateId();
            System.out.println("Auto-generated ID: " + id);
        } else {
            try {
                id = Integer.parseInt(idLine);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format.");
                return;
            }
        }

        Member m = new Member(id, name);
        library.addMember(m);
        System.out.println("Member added: " + m);
    }

    private static void addBook() {
        String title = readString("Enter book title: ").trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        String author = readString("Enter author (or press Enter for Unknown): ").trim();
        if (author.isEmpty()) author = "Unknown";

        String category = readString("Enter category (or press Enter for General): ").trim();
        if (category.isEmpty()) category = "General";

        String y = readString("Enter publish year (or press Enter to set 0): ").trim();
        int year = 0;
        if (!y.isEmpty()) {
            try { year = Integer.parseInt(y); } catch (NumberFormatException ignored) {}
        }

        String p = readString("Enter page count (or press Enter to set 0): ").trim();
        int pageCount = 0;
        if (!p.isEmpty()) {
            try { pageCount = Integer.parseInt(p); } catch (NumberFormatException ignored) {}
        }

        String c = readString("Enter total copies (or press Enter to set 1): ").trim();
        int copies = 1;
        if (!c.isEmpty()) {
            try { copies = Integer.parseInt(c); } catch (NumberFormatException ignored) {}
            if (copies < 1) copies = 1;
        }

        // ISBN auto-generate if empty
        String isbn = readString("Enter ISBN (or press Enter to auto-generate): ").trim();
        if (isbn.isEmpty()) {
            isbn = "ISBN-" + (100000 + rng.nextInt(900000));
        }

        // Use the constructor without explicit id (Book will auto-assign), if available.
        // If your Book only has constructor with bookId first, replace below with appropriate constructor.
        Book b;
        try {
            // Try to use constructor (String isbn, String title, String author, String category, int publishYear, int pageCount, int totalCopies)
            b = new Book(isbn, title, author, category, year, pageCount, copies);
        } catch (NoSuchMethodError | Exception ex) {
            // Fallback: use constructor that accepts id first
            int id = generateId();
            b = new Book(id, isbn, title, author, category, year, pageCount, copies);
        }

        library.addBook(b);
        System.out.println("Book added: " + b);
    }

    private static void borrowBook() {
        System.out.println("To borrow, you need memberId and bookId.");
        int memberId = readInt("Enter memberId: ");
        int bookId = readInt("Enter bookId: ");

        boolean result = library.borrowBook(memberId, bookId);
        if (result) System.out.println("Book borrowed successfully.");
        else System.out.println("Borrow FAILED. (check member/book existence, limits, or availability)");
    }

    private static void returnBook() {
        System.out.println("To return, you need memberId and bookId.");
        int memberId = readInt("Enter memberId: ");
        int bookId = readInt("Enter bookId: ");

        boolean result = library.returnBook(memberId, bookId);
        if (result) System.out.println("Book returned.");
        else System.out.println("Return FAILED. (check member/book and active loan)");
    }

    private static void showMemberHistory() {
        int memberId = readInt("Enter memberId: ");
        Member m = library.getMember(memberId);
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }

        MyLinkedList<LoanRecord> history = m.getLoanHistory();
        if (history == null || history.size() == 0) {
            System.out.println("No loan history for member " + m.getName());
            return;
        }

        System.out.println("\n--- Loan History for " + m.getName() + " ---");
        for (int i = 0; i < history.size(); i++) {
            System.out.println(history.get(i));
        }
    }

    private static void showBookStatus() {
        int bookId = readInt("Enter bookId: ");
        Book b = library.getBook(bookId);
        if (b == null) {
            System.out.println("Book not found.");
            return;
        }

        System.out.println("Title: " + b.getTitle());
        System.out.println("Author: " + b.getAuthor());
        System.out.println("Total copies: " + b.getTotalCopies());
        System.out.println("Borrowed copies: " + b.getBorrowedCopies());
        System.out.println("Available copies: " + b.getAvailableCopies());
        System.out.println("Popularity: " + b.getPopularityCount());
        System.out.println("Waitlist size: " + (b.getWaitList() != null ? b.getWaitList().size() : 0));
    }

    private static void showWaitlist() {
        int bookId = readInt("Enter bookId: ");
        Book b = library.getBook(bookId);
        if (b == null) {
            System.out.println("Book not found.");
            return;
        }

        MyQueue<Member> wait = b.getWaitList();
        if (wait == null || wait.isEmpty()) {
            System.out.println("Waitlist empty for this book.");
            return;
        }

        // Safely print waitlist without losing it by using a temporary queue
        MyQueue<Member> temp = new MyQueue<>();
        System.out.println("\n--- WAITLIST for \"" + b.getTitle() + "\" ---");
        while (!wait.isEmpty()) {
            Member m = wait.dequeue();
            System.out.println("MemberID: " + m.getMemberID() + " | Name: " + m.getName());
            temp.enqueue(m);
        }
        // restore
        while (!temp.isEmpty()) {
            wait.enqueue(temp.dequeue());
        }
    }

    private static void searchBooks() {
        System.out.println("\n--- BOOK SEARCH ---");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by Author");
        System.out.println("3. Search by Any Field (title/author/isbn/category)");
        int c = readInt("Select: ");

        String query = readString("Enter search text: ").trim();
        if (query.isEmpty()) {
            System.out.println("Search text cannot be empty.");
            return;
        }
        String q = query.toLowerCase();

        switch (c) {
            case 1 -> {
                List<Book> res = library.searchByTitle(q);
                showSearchResults(res);
            }
            case 2 -> {
                List<Book> res = library.searchByAuthor(q);
                showSearchResults(res);
            }
            case 3 -> {
                // fallback: scan all books and use matches()
                MyLinkedList<Book> all = library.listAllBooks();
                List<Book> results = new ArrayList<>();
                for (int i = 0; i < all.size(); i++) {
                    Book b = all.get(i);
                    if (b != null && b.matches(q)) results.add(b);
                }
                showSearchResults(results);
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void showSearchResults(List<Book> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\n--- Search Results ---");
        for (Book b : results) {
            System.out.println("ID: " + b.getBookId() + " | Title: " + b.getTitle() + " | Author: " + b.getAuthor() +
                    " | Available: " + b.getAvailableCopies());
        }
    }

    private static void undo() {
        boolean ok = library.undo();
        if (ok) System.out.println("Undo successful.");
        else System.out.println("Nothing to undo.");
    }

    private static void listAllMembers() {
        MyLinkedList<Member> members = library.listAllMembers();
        if (members == null || members.size() == 0) {
            System.out.println("No members.");
            return;
        }
        System.out.println("\n--- MEMBERS ---");
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            System.out.println("ID: " + m.getMemberID() + " | Name: " + m.getName() + " | Active: " + m.getActiveBooks().size());
        }
    }

    private static void listAllBooks() {
        MyLinkedList<Book> books = library.listAllBooks();
        if (books == null || books.size() == 0) {
            System.out.println("No books.");
            return;
        }
        System.out.println("\n--- BOOKS ---");
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            System.out.println("ID: " + b.getBookId() + " | Title: " + b.getTitle() + " | Available: " + b.getAvailableCopies());
        }
    }

    // -------------------- HELPERS -----------------------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static int getInt(String prompt) {
        return readInt(prompt);
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static int generateId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }
}
