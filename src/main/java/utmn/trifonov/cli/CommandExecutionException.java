package utmn.trifonov.cli;

public class CommandExecutionException extends Exception{
    private final String message;
    public CommandExecutionException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
