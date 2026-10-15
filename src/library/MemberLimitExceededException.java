package library;

// Raised when a member exceeds the active-loan limit.
public class MemberLimitExceededException extends Exception {
    public MemberLimitExceededException(String message) {
        super(message);
    }
}
