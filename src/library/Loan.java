package library;

import java.time.LocalDate;
import java.util.Objects;

public final class Loan {
    private final LibraryItem item;
    private final Member member;
    private final LocalDate loanDate;
    private final LocalDate dueDate;

    public Loan(LibraryItem item, Member member, LocalDate loanDate) {
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        this.member = Objects.requireNonNull(member, "Member cannot be null");
        this.loanDate = Objects.requireNonNull(loanDate, "Loan date cannot be null");
        this.dueDate = loanDate.plusDays(item.loanPeriodDays());
    }

    public LibraryItem getItem() {
        return item;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isOverdue(LocalDate date) {
        return dueDate.isBefore(Objects.requireNonNull(date, "Date cannot be null"));
    }

    @Override
    public String toString() {
        return "Loan{item='" + item.getTitle() + "', member='" + member.getName()
                + "', loanDate=" + loanDate + ", dueDate=" + dueDate + "}";
    }
}
