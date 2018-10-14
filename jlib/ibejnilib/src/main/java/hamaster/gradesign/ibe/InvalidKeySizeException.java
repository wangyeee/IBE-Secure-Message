package hamaster.gradesign.ibe;

public class InvalidKeySizeException extends RuntimeException {
    private static final long serialVersionUID = -2345812702342829664L;

    public InvalidKeySizeException() {
    }

    public InvalidKeySizeException(String message) {
        super(message);
    }

    public InvalidKeySizeException(Throwable cause) {
        super(cause);
    }

    public InvalidKeySizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
