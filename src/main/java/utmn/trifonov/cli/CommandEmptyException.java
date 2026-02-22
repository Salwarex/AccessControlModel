package utmn.trifonov.cli;

public class CommandEmptyException extends Exception{
    public CommandEmptyException() {
    }

    @Override
    public String getMessage() {
        return "";
    }
}
