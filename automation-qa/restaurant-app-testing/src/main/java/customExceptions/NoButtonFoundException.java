package customExceptions;

public class NoButtonFoundException extends RuntimeException{

    public NoButtonFoundException(String message)
    {
        super(message);
    }
}
