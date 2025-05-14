package customExceptions;

public class NoSuchBrowserException extends RuntimeException{

    public NoSuchBrowserException(String message){
        super(message);
    }
}
