package library;

// Contract for items that can be checked out and returned.
public interface Borrowable {
    void borrow(Member member) throws ItemNotAvailableException;

    void giveBack();
}
