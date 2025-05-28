package customExceptions;

public class NoViewFoundException extends RuntimeException {

    public NoViewFoundException(String message)
    {
        super(message);
    }
}
