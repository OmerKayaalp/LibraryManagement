package ui;

import core.LibrarySystem;
import models.Book;
import models.Member;
import models.LoanRecord;
import dataStructure.linkedList.MyLinkedList;
import dataStructure.queue.MyQueue;

import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

// Main console UI for the Library Management System.
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    
    /**
     * Random generator seeded with fixed value for reproducible user IDs.
     * User IDs will always be generated from this seeded source.
     */
    private static final Random rng = new Random();
    // Deterministic member ID counter (school-like incremental)
    private static int memberIdCounter = 230315035;
    
    private static final LibrarySystem library = new LibrarySystem();

    public static void main(String[] args) {
        System.out.println("Welcome to the Library Management System");
        
        initializeSampleData();

        boolean success = true;
        while (success) {
            printMenu();
            int choice = readInt("Your choice: ");

            switch (choice) {
                case 1 -> handleUserOperations();
                case 2 -> handleBookOperations();
                case 3 -> borrowBook();
                case 4 -> returnBook();
                case 5 -> undo();
                case 6 -> {
                    System.out.println("Exiting... Goodbye!");
                    success = false;
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
            if (success) {
                System.out.println("\nPress Enter to continue...");
                sc.nextLine();
            }

        }
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY SYSTEM MENU =====");
        System.out.println("1. Member Actions");
        System.out.println("2. Book Actions");
        System.out.println("3. Borrow Book (by IDs)");
        System.out.println("4. Return Book (by IDs)");
        System.out.println("5. Undo Last Action");
        System.out.println("6. Exit");
    }

    // -------------------- MENU OPERATIONS -----------------------

    private static void handleUserOperations() {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- MEMBER ACTIONS ---");
            System.out.println("1. Add Member");
            System.out.println("2. Remove Member");
            System.out.println("3. List Members");
            System.out.println("4. Show Member History");
            System.out.println("5. Member Active Loans");
            System.out.println("6. Search Member (ID/Name)");
            System.out.println("0. Back");
            int choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> addMember();
                case 2 -> removeMember();
                case 3 -> listAllMembers();
                case 4 -> showMemberHistory();
                case 5 -> showMemberActiveLoans();
                case 6 -> searchMembers();
                case 0 -> stay = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void handleBookOperations() {
        boolean stay = true;
        while (stay) {
            System.out.println("\n--- BOOK ACTIONS ---");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. List Books");
            System.out.println("4. Show Book Status");
            System.out.println("5. Show Book Waitlist");
            System.out.println("6. Search Books");
            System.out.println("7. Show Most Popular Books");
            System.out.println("0. Back");
            int choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> addBook();
                case 2 -> removeBook();
                case 3 -> listAllBooks();
                case 4 -> showBookStatus();
                case 5 -> showWaitlist();
                case 6 -> searchBooks();
                case 7 -> showMostPopularBooks();
                case 0 -> stay = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addMember() {
        String name = readString("Enter member name: ").trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        String idLine = readString("Enter member ID (or press Enter to auto-generate): ").trim();
        int id;
        if (idLine.isEmpty()) {
            id = generateMemberId();
            System.out.println("Auto-generated ID: " + id);
        } else {
            Integer parsed = parseIntValue(idLine);
            if (parsed == null) {
                System.out.println("Invalid ID format.");
                return;
            }
            id = parsed;
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
        int year = parseOptionalInt(y, 0);

        String p = readString("Enter page count (or press Enter to set 0): ").trim();
        int pageCount = parseOptionalInt(p, 0);

        String c = readString("Enter total copies (or press Enter to set 1): ").trim();
        int copies = parseOptionalInt(c, 1);
        if (copies < 1) copies = 1;

        // ISBN auto-generate if empty
        String isbn = readString("Enter ISBN (or press Enter to auto-generate): ").trim();
        if (isbn.isEmpty()) {
            isbn = "ISBN-" + (100000 + rng.nextInt(900000));
        }

        Book b = new Book(isbn, title, author, category, year, pageCount, copies);

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

    private static void showMemberActiveLoans() {
        int memberId = readInt("Enter memberId: ");
        Member m = library.getMember(memberId);
        if (m == null) {
            System.out.println("Member not found.");
            return;
        }
        MyLinkedList<Book> active = m.getActiveBooks();
        if (active == null || active.size() == 0) {
            System.out.println("This member has no active loans.");
            return;
        }
        System.out.println("\n--- Active Loans: " + m.getName() + " ---");
        for (int i = 0; i < active.size(); i++) {
            Book b = active.get(i);
            System.out.println("ID: " + b.getBookId() + " | " + b.getTitle() + " | " + b.getAuthor());
        }
    }

    private static void searchMembers() {
        System.out.println("\n--- MEMBER SEARCH ---");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Name");
        int c = readInt("Choice: ");
        if (c == 1) {
            int memberId = readInt("Member ID: ");
            Member m = library.getMember(memberId);
            if (m == null) {
                System.out.println("Member not found.");
                return;
            }
            System.out.println("ID: " + m.getMemberID() + " | Ad: " + m.getName() + " | Aktif: " + m.getActiveBooks().size());
        } else if (c == 2) {
            String query = readString("Name (partial accepted): ").trim().toLowerCase();
            if (query.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return;
            }
            MyLinkedList<Member> members = library.listAllMembers();
            boolean found = false;
            for (int i = 0; i < members.size(); i++) {
                Member m = members.get(i);
                if (m != null && m.getName() != null && m.getName().toLowerCase().contains(query)) {
                    System.out.println("ID: " + m.getMemberID() + " | Ad: " + m.getName() + " | Aktif: " + m.getActiveBooks().size());
                    found = true;
                }
            }
            if (!found) System.out.println("No matching member.");
        } else {
            System.out.println("Invalid choice.");
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

    private static void removeMember() {
        int memberId = readInt("Enter member ID to remove: ");
        Member removed = library.removeMember(memberId);
        if (removed != null) {
            System.out.println("Member removed: " + removed.getName());
        } else {
            System.out.println("Member not found or removal failed.");
        }
    }

    private static void removeBook() {
        int bookId = readInt("Enter book ID to remove: ");
        Book removed = library.removeBook(bookId);
        if (removed != null) {
            System.out.println("Book removed: " + removed.getTitle());
        } else {
            System.out.println("Book not found or removal failed.");
        }
    }

    private static void showMostPopularBooks() {
        int k = readInt("How many popular books to show? ");
        if (k <= 0) {
            System.out.println("Invalid number. Please enter a positive number.");
            return;
        }
        
        List<Book> popular = library.getTopKPopular(k);
        if (popular == null || popular.isEmpty()) {
            System.out.println("No books found in the system.");
            return;
        }
        
        System.out.println("\n--- MOST POPULAR BOOKS (Top " + k + ") ---");
        for (int i = 0; i < popular.size(); i++) {
            Book b = popular.get(i);
            System.out.println((i + 1) + ". " + b.getTitle() + 
                             " by " + b.getAuthor() + 
                             " | Popularity: " + b.getPopularityCount() + 
                             " | Available: " + b.getAvailableCopies());
        }
    }

    private static void undo() {
        String message = library.undo();
        if (message == null) {
            System.out.println("Nothing to undo.");
        } else {
            System.out.println(message);
        }
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
            Integer parsed = parseIntValue(line);
            if (parsed != null) {
                return parsed;
            }
            System.out.println("Enter a valid number.");
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static int generateMemberId() {
        // Sequential member IDs starting from a fixed base (e.g., 230315035, 230315036, ...)
        return memberIdCounter++;
    }

    private static Integer parseIntValue(String text) {
        if (!isNumeric(text)) return null;
        boolean negative = text.startsWith("-");
        int start = negative ? 1 : 0;
        int value = 0;
        for (int i = start; i < text.length(); i++) {
            value = value * 10 + (text.charAt(i) - '0');
        }
        return negative ? -value : value;
    }

    private static int parseOptionalInt(String text, int defaultValue) {
        if (text == null || text.isEmpty()) return defaultValue;
        Integer parsed = parseIntValue(text);
        return parsed != null ? parsed : defaultValue;
    }

    private static boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) return false;
        int start = text.charAt(0) == '-' ? 1 : 0;
        if (start == text.length()) return false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    // Create initial demo data based on STUDENT_ID
    private static void initializeSampleData() {
        int studentIdMod = LibrarySystem.STUDENT_ID % 10;
        int numBooks = 5 + studentIdMod; // 5 to 14 books based on ID
        int numMembers = 3 + (studentIdMod % 5); // 3 to 7 members based on ID
        
        System.out.println("\n=== Initializing Sample Data ===");
        System.out.println("Student ID: " + LibrarySystem.STUDENT_ID);
        System.out.println("Adding " + numBooks + " books and " + numMembers + " members...\n");

        // Sample book data arrays
        String[] bookTitles = {
            "The Great Gatsby", "1984", "To Kill a Mockingbird", "Pride and Prejudice",
            "The Catcher in the Rye", "Lord of the Flies", "Animal Farm", "Brave New World",
            "The Hobbit", "Fahrenheit 451", "Moby Dick", "War and Peace",
            "Crime and Punishment", "The Odyssey", "Hamlet"
        };
        
        String[] bookAuthors = {
            "F. Scott Fitzgerald", "George Orwell", "Harper Lee", "Jane Austen",
            "J.D. Salinger", "William Golding", "George Orwell", "Aldous Huxley",
            "J.R.R. Tolkien", "Ray Bradbury", "Herman Melville", "Leo Tolstoy",
            "Fyodor Dostoevsky", "Homer", "William Shakespeare"
        };
        
        String[] bookCategories = {
            "Fiction", "Dystopian", "Classic", "Romance", "Coming-of-Age",
            "Adventure", "Political", "Science Fiction", "Fantasy", "Philosophy"
        };
        
        String[] memberNames = {
            "Ahmet Yılmaz", "Ayşe Demir", "Mehmet Kaya", "Fatma Şahin",
            "Ali Çelik", "Zeynep Arslan", "Ömer Kayaalp", "Çağatay Karadağ",
            "Elif Gülseren", "Selin Kılıç"
        };

        // Add books using STUDENT_ID seeded random
        for (int i = 0; i < numBooks; i++) {
            int titleIndex = (i + studentIdMod) % bookTitles.length;
            // Keep author aligned with title using the same index
            int authorIndex = titleIndex % bookAuthors.length;
            int categoryIndex = (i + studentIdMod * 2) % bookCategories.length;
            
            String isbn = "ISBN-" + (100000 + LibrarySystem.STUDENT_ID + i * 100);
            String title = bookTitles[titleIndex];
            String author = bookAuthors[authorIndex];
            String category = bookCategories[categoryIndex];
            
            // Use STUDENT_ID to vary publish year and page count
            int publishYear = 1900 + (LibrarySystem.STUDENT_ID % 100) + (i * 5);
            int pageCount = 200 + (LibrarySystem.STUDENT_ID % 500) + (i * 50);
            int copies = 1 + (LibrarySystem.STUDENT_ID % 3); // 1-3 copies
            
            Book book = new Book(isbn, title, author, category, publishYear, pageCount, copies);
            library.addBook(book);
            System.out.println("Added book: " + title + " by " + author);
        }

        // Add members using STUDENT_ID seeded random
        for (int i = 0; i < numMembers; i++) {
            int nameIndex = (i + studentIdMod * 3) % memberNames.length;
            Member member = new Member(generateMemberId(), memberNames[nameIndex]);
            library.addMember(member);
            System.out.println("Added member: " + memberNames[nameIndex] + " (ID: " + member.getMemberID() + ")");
        }

        System.out.println("\n=== Sample Data Initialization Complete ===\n");
    }
}
