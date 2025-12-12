package cl.duoc.lunari.api.user.exception;

/**
 * Exception thrown when user authentication fails due to invalid credentials.
 * This exception should result in a 401 Unauthorized HTTP response.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
