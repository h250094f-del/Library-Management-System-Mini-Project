package library;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Main library logic: catalog data, members, and loan rules.
public final class Library {
    private final Map<String, LibraryItem> items = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();

    // Adds a new item unless its ID already exists.
    public void addItem(LibraryItem item) {
        Objects.requireNonNull(item, "Item cannot be null");
        if (items.putIfAbsent(item.getId(), item) != null) {
            throw new IllegalArgumentException("An item with ID " + item.getId() + " already exists.");
        }
    }

    // Registers a member unless that ID is already taken.
    public void registerMember(Member member) {
        Objects.requireNonNull(member, "Member cannot be null");
        if (members.putIfAbsent(member.getId(), member) != null) {
            throw new IllegalArgumentException(
                    "A member with ID " + member.getId() + " already exists.");
        }
    }

    // Creates a loan when the item is available and the member has space.
    public void borrow(String memberId, String itemId)
            throws ItemNotAvailableException, MemberLimitExceededException {
        Member member = requireMember(memberId);
        LibraryItem item = requireItem(itemId);
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Item \"" + item.getTitle() + "\" is already on loan.");
        }
        if (member.getActiveLoans().size() >= Member.MAX_ACTIVE_LOANS) {
            throw new MemberLimitExceededException(
                    "Member \"" + member.getName() + "\" already has the maximum of "
                            + Member.MAX_ACTIVE_LOANS + " active loans.");
        }

        Loan loan = new Loan(item, member, LocalDate.now());
        member.addLoan(loan);
        item.borrow(member);
    }

    // Returns an item and removes its active loan record.
    public void giveBack(String itemId) {
        LibraryItem item = requireItem(itemId);
        if (item.isAvailable()) {
            throw new IllegalArgumentException("Item \"" + item.getTitle() + "\" is not currently on loan.");
        }
        Member borrower = members.values().stream()
                .filter(member -> member.getActiveLoans().stream()
                        .anyMatch(loan -> loan.getItem().getId().equals(itemId)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No active loan record exists for item " + itemId + "."));
        borrower.removeLoan(itemId);
        item.giveBack();
    }

    // Finds items whose titles contain the search text, ignoring case.
    public List<LibraryItem> searchByTitle(String text) {
        String query = LibraryItem.requireText(text, "Search text").toLowerCase(Locale.ROOT);
        return items.values().stream()
                .filter(item -> item.getTitle().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    // Lists all loans due before the supplied date.
    public List<Loan> listOverdueItems(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return members.values().stream()
                .flatMap(member -> member.getActiveLoans().stream())
                .filter(loan -> loan.isOverdue(date))
                .sorted(Comparator.comparing(Loan::getDueDate))
                .collect(Collectors.toList());
    }

    public List<LibraryItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public List<Member> getMembers() {
        return new ArrayList<>(members.values());
    }

    private Member requireMember(String memberId) {
        Member member = members.get(memberId);
        if (member == null) {
            throw new IllegalArgumentException("No member found with ID " + memberId + ".");
        }
        return member;
    }

    private LibraryItem requireItem(String itemId) {
        LibraryItem item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("No item found with ID " + itemId + ".");
        }
        return item;
    }

    // Loads catalog, member, and loan data from CSV files in a folder.
    public void load(Path directory) throws IOException {
        items.clear();
        members.clear();
        Path itemsFile = directory.resolve("items.csv");
        Path membersFile = directory.resolve("members.csv");
        Path loansFile = directory.resolve("loans.csv");

        if (Files.exists(itemsFile)) {
            readRows(itemsFile, 4, fields -> {
                String id = fields.get(1);
                String title = fields.get(2);
                LibraryItem item;
                if ("BOOK".equals(fields.get(0))) {
                    item = new Book(id, title, fields.get(3));
                } else if ("DVD".equals(fields.get(0))) {
                    item = new Dvd(id, title, Integer.parseInt(fields.get(3)));
                } else {
                    throw new IllegalArgumentException("Unknown item type: " + fields.get(0));
                }
                addItem(item);
            });
        }
        if (Files.exists(membersFile)) {
            readRows(membersFile, 2, fields -> registerMember(new Member(fields.get(0), fields.get(1))));
        }
        if (Files.exists(loansFile)) {
            readRows(loansFile, 3, fields -> {
                LibraryItem item = requireItem(fields.get(0));
                Member member = requireMember(fields.get(1));
                if (!item.isAvailable()) {
                    throw new IllegalArgumentException("Item " + item.getId() + " has multiple active loans.");
                }
                Loan loan = new Loan(item, member, LocalDate.parse(fields.get(2)));
                try {
                    member.addLoan(loan);
                    item.borrow(member);
                } catch (MemberLimitExceededException | ItemNotAvailableException exception) {
                    throw new IllegalArgumentException(
                            "Invalid active loan for item " + item.getId() + ": "
                                    + exception.getMessage(),
                            exception);
                }
            });
        }
    }

    // Saves the current state to CSV files in the target folder.
    public void save(Path directory) throws IOException {
        Files.createDirectories(directory);
        List<String> itemRows = new ArrayList<>();
        itemRows.add(Csv.row("type", "id", "title", "detail"));
        for (LibraryItem item : items.values()) {
            if (item instanceof Book) {
                itemRows.add(Csv.row("BOOK", item.getId(), item.getTitle(), ((Book) item).getAuthor()));
            } else if (item instanceof Dvd) {
                itemRows.add(Csv.row("DVD", item.getId(), item.getTitle(),
                        Integer.toString(((Dvd) item).getRuntimeMinutes())));
            } else {
                throw new IOException("Cannot persist unsupported item type: "
                        + item.getClass().getName());
            }
        }

        List<String> memberRows = new ArrayList<>();
        memberRows.add(Csv.row("id", "name"));
        for (Member member : members.values()) {
            memberRows.add(Csv.row(member.getId(), member.getName()));
        }

        List<String> loanRows = new ArrayList<>();
        loanRows.add(Csv.row("itemId", "memberId", "loanDate"));
        for (Member member : members.values()) {
            for (Loan loan : member.getActiveLoans()) {
                loanRows.add(Csv.row(loan.getItem().getId(), member.getId(),
                        loan.getLoanDate().toString()));
            }
        }

        writeRows(directory.resolve("items.csv"), itemRows);
        writeRows(directory.resolve("members.csv"), memberRows);
        writeRows(directory.resolve("loans.csv"), loanRows);
    }

    // Writes one CSV file with each row already formatted.
    private static void writeRows(Path file, List<String> rows) throws IOException {
        Files.write(file, rows, StandardCharsets.UTF_8);
    }

    // Reads one CSV file and validates each row before processing it.
    private static void readRows(Path file, int expectedFields, RowConsumer consumer) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            try {
                List<String> fields = Csv.parse(lines.get(i));
                if (fields.size() != expectedFields) {
                    throw new IllegalArgumentException(
                            "Expected " + expectedFields + " fields but found " + fields.size() + ".");
                }
                consumer.accept(fields);
            } catch (RuntimeException exception) {
                throw new IOException("Invalid data in " + file + " at line " + (i + 1)
                        + ": " + exception.getMessage(), exception);
            }
        }
    }

    @FunctionalInterface
    private interface RowConsumer {
        void accept(List<String> fields);
    }
}
