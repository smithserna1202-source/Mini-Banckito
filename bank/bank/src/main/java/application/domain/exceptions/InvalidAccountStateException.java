package application.domain.exceptions;

public class InvalidAccountStateException extends RuntimeException {
    public InvalidAccountStateException(String message) { super(message); }
}
