package name.velikodniy.vitaliy.fixedlength;

/**
 * Runtime exception thrown by the fixed-length library when
 * a parsing, formatting, or configuration error occurs.
 *
 * <p>This is an unchecked exception so that it can propagate
 * through stream and lambda operations without requiring
 * explicit catches at every call site.
 */
public class FixedLengthException extends RuntimeException {

    /**
     * Creates an exception with no message or cause.
     */
    public FixedLengthException() {
        super();
    }

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public FixedLengthException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public FixedLengthException(
            String message, Throwable cause) {
        super(message, cause);
    }
}
