package top.Exceptions;

public class SelectionOutOfBoundsException extends Exception {

    protected final String message = "Number selected is not within range";
    public void SelectionOutOfBoundsException(){}

    @Override
    public String getMessage()
    {
        return message;
    }
}
