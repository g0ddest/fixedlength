package name.velikodniy.vitaliy.fixedlength;

public class FixedLengthException extends RuntimeException {
    public FixedLengthException() {
        super();
    }

    public FixedLengthException(String message) {
        super(message);
    }

    public FixedLengthException(String message, Throwable cause) {
        super(message, cause);
    }
}
