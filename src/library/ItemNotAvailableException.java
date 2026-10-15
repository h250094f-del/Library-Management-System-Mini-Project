package library;

// Raised when a loan is requested for an item already checked out.
public class ItemNotAvailableException extends Exception {
    public ItemNotAvailableException(String message) {
        super(message);
    }
}
