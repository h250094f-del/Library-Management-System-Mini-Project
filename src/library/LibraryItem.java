package library;

import java.util.Objects;

public abstract class LibraryItem implements Borrowable {
    private final String id;
    private final String title;
    private boolean available = true;

    protected LibraryItem(String id, String title) {
        this.id = requireText(id, "Item ID");
        this.title = requireText(title, "Title");
    }

    public abstract int loanPeriodDays();

    public final String getId() {
        return id;
    }

    public final String getTitle() {
        return title;
    }

    public final boolean isAvailable() {
        return available;
    }

    @Override
    public final void borrow(Member member) throws ItemNotAvailableException {
        Objects.requireNonNull(member, "Member cannot be null");
        if (!available) {
            throw new ItemNotAvailableException("Item \"" + title + "\" is already on loan.");
        }
        available = false;
    }

    @Override
    public final void giveBack() {
        available = true;
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        if (value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(fieldName + " cannot contain line breaks.");
        }
        return value.trim();
    }
}
