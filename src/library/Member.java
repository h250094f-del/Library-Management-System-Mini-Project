package library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A registered user and the loans they currently hold.
public final class Member {
    public static final int MAX_ACTIVE_LOANS = 3;

    private final String id;
    private final String name;
    private final List<Loan> activeLoans = new ArrayList<>();

    public Member(String id, String name) {
        this.id = LibraryItem.requireText(id, "Member ID");
        this.name = LibraryItem.requireText(name, "Member name");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Loan> getActiveLoans() {
        return Collections.unmodifiableList(activeLoans);
    }

    // Adds a loan when the member is under the borrowing cap.
    public void addLoan(Loan loan) throws MemberLimitExceededException {
        if (activeLoans.size() >= MAX_ACTIVE_LOANS) {
            throw new MemberLimitExceededException(
                    "Member \"" + name + "\" already has the maximum of "
                            + MAX_ACTIVE_LOANS + " active loans.");
        }
        activeLoans.add(loan);
    }

    // Removes the active loan matching the returned item.
    public void removeLoan(String itemId) {
        activeLoans.removeIf(loan -> loan.getItem().getId().equals(itemId));
    }

    @Override
    public String toString() {
        return "Member{id='" + id + "', name='" + name
                + "', activeLoans=" + activeLoans.size() + "}";
    }
}
