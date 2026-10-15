package library;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

// Console menu for managing the library catalog and loans.
public final class LibraryApp {
    private static final Path DATA_DIRECTORY = Paths.get("data");

    private LibraryApp() {
    }

    // Runs the interactive loop and saves data on exit.
    public static void main(String[] args) {
        Library library = new Library();
        try {
            library.load(DATA_DIRECTORY);
        } catch (IOException exception) {
            System.err.println("Could not load library data: " + exception.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = prompt(scanner, "Choose an option: ");
                try {
                    switch (choice) {
                        case "1":
                            addItem(scanner, library);
                            break;
                        case "2":
                            registerMember(scanner, library);
                            break;
                        case "3":
                            borrowItem(scanner, library);
                            break;
                        case "4":
                            returnItem(scanner, library);
                            break;
                        case "5":
                            listOverdue(library);
                            break;
                        case "6":
                            search(scanner, library);
                            break;
                        case "7":
                            listItems(library);
                            break;
                        case "8":
                            listMembers(library);
                            break;
                        case "0":
                            running = false;
                            break;
                        default:
                            System.out.println("Enter one of the listed menu numbers.");
                    }
                } catch (ItemNotAvailableException | MemberLimitExceededException
                         | IllegalArgumentException exception) {
                    System.out.println("Unable to complete that operation: " + exception.getMessage());
                } catch (DateTimeParseException exception) {
                    System.out.println("Enter the date in YYYY-MM-DD format.");
                }
            }
        }

        try {
            library.save(DATA_DIRECTORY);
            System.out.println("Library data saved to " + DATA_DIRECTORY.toAbsolutePath() + ".");
        } catch (IOException exception) {
            System.err.println("Could not save library data: " + exception.getMessage());
        }
    }

    // Prints all menu choices for the user.
    private static void printMenu() {
        System.out.println("\n=== Library Management System ===");
        System.out.println("1. Add item");
        System.out.println("2. Register member");
        System.out.println("3. Borrow item");
        System.out.println("4. Return item");
        System.out.println("5. List overdue items");
        System.out.println("6. Search by title");
        System.out.println("7. List all items");
        System.out.println("8. List all members");
        System.out.println("0. Save and exit");
    }

    // Adds a book or DVD from the console input.
    private static void addItem(Scanner scanner, Library library) {
        String type = prompt(scanner, "Item type (book/dvd): ").trim().toLowerCase();
        String id = prompt(scanner, "Item ID: ");
        String title = prompt(scanner, "Title: ");
        if ("book".equals(type)) {
            library.addItem(new Book(id, title, prompt(scanner, "Author: ")));
        } else if ("dvd".equals(type)) {
            library.addItem(new Dvd(id, title, readPositiveInt(scanner, "Runtime in minutes: ")));
        } else {
            throw new IllegalArgumentException("Item type must be book or dvd.");
        }
        System.out.println("Item added.");
    }

    // Registers a new member from the console input.
    private static void registerMember(Scanner scanner, Library library) {
        String id = prompt(scanner, "Member ID: ");
        String name = prompt(scanner, "Member name: ");
        library.registerMember(new Member(id, name));
        System.out.println("Member registered.");
    }

    // Borrows an item for a member and prints the result.
    private static void borrowItem(Scanner scanner, Library library)
            throws ItemNotAvailableException, MemberLimitExceededException {
        library.borrow(prompt(scanner, "Member ID: "), prompt(scanner, "Item ID: "));
        System.out.println("Item borrowed. The due date is based on the item's loan period.");
    }

    // Returns a borrowed item to the library.
    private static void returnItem(Scanner scanner, Library library) {
        library.giveBack(prompt(scanner, "Item ID to return: "));
        System.out.println("Item returned.");
    }

    // Lists all loans whose due date has passed.
    private static void listOverdue(Library library) {
        LocalDate today = LocalDate.now();
        List<Loan> overdue = library.listOverdueItems(today);
        if (overdue.isEmpty()) {
            System.out.println("There are no overdue items.");
            return;
        }
        overdue.forEach(loan -> System.out.println(loan + " ("
                + (today.toEpochDay() - loan.getDueDate().toEpochDay()) + " days overdue)"));
    }

    // Searches catalog entries by title text.
    private static void search(Scanner scanner, Library library) {
        List<LibraryItem> results = library.searchByTitle(prompt(scanner, "Title contains: "));
        if (results.isEmpty()) {
            System.out.println("No matching items found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    // Shows every current item in the catalog.
    private static void listItems(Library library) {
        if (library.getItems().isEmpty()) {
            System.out.println("No items registered.");
            return;
        }
        library.getItems().forEach(System.out::println);
    }

    // Shows every member and their active loans.
    private static void listMembers(Library library) {
        if (library.getMembers().isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        library.getMembers().forEach(member -> {
            System.out.println(member);
            member.getActiveLoans().forEach(loan -> System.out.println("  " + loan));
        });
    }

    // Reads a positive integer from the user.
    private static int readPositiveInt(Scanner scanner, String message) {
        String input = prompt(scanner, message);
        try {
            int value = Integer.parseInt(input);
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a positive whole number.");
        }
    }

    // Reads a trimmed line of input from the console.
    private static String prompt(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
