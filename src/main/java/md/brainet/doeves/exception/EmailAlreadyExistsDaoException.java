package md.brainet.doeves.exception;

public class EmailAlreadyExistsDaoException extends RuntimeException {
    public EmailAlreadyExistsDaoException(String message) {
        super(message);
    }

    public EmailAlreadyExistsDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
