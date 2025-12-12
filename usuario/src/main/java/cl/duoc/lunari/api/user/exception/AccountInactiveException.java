package cl.duoc.lunari.api.user.exception;

/**
 * Exception thrown when an inactive user attempts to login.
 * This exception should result in a 403 Forbidden HTTP response.
 */
public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException(String message) {
        super(message);
    }

    public AccountInactiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
