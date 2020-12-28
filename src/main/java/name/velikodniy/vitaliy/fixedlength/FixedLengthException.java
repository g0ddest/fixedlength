package name.velikodniy.vitaliy.fixedlength;

public class FixedLengthException extends Exception {
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
