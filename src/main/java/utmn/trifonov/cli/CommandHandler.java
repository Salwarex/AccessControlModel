package utmn.trifonov.cli;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.cli.commands.WhoAmICommand;
import utmn.trifonov.file.File;

public class CommandHandler {
    protected File location;
    protected CommandHandler delegate;
    protected CommandHandler parent;

    public CommandHandler(File location, CommandHandler parent){
        this.location = location == null ? Main.getRepository() : location;
        this.parent = parent;
    }

    public final void handle(String command, String[] args) throws CommandExecutionException, CommandEmptyException {
        if(delegate != null) {
            delegate.handle(command, args);
            return;
        }

        Command commandObject = commandPreProcessor(command, args);

        commandProcessor(commandObject);
    }

    protected Command commandPreProcessor(String command, String[] args) throws CommandExecutionException, CommandEmptyException {
        User sender = Main.getSession().getUser();
        return switch(command){
            case "whoami" -> new WhoAmICommand(sender, location);
            case null -> {
                throw new CommandEmptyException();
            }
            default -> {
                if(parent == null){
                    throw new CommandExecutionException("Команда неизвестна или невыполнима в данном контексте. Попробуйте ещё раз.");
                }
                else{
                    yield parent.commandPreProcessor(command, args);
                }
            }
        };
    }

    private void commandProcessor(Command command){
        try{
            command.execute();
        } catch (CommandExecutionException e) {
            Logger.err(e.getMessage());
        }
    }

    public File getLocation() {
        return (delegate == null ? location : delegate.getLocation());
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public CommandHandler getActor(){
        return (delegate == null ? this : delegate.getActor());
    }

    public void setDelegate(CommandHandler delegate){
        this.delegate = delegate;
    }

    public void clearDelegate(){
        this.delegate = null;
    }

    public void stopDelegation(){
        if(parent != null) parent.clearDelegate();
        else Logger.wrn("Обработчик попытался прекратить делегацию, однако он не имеет родительского обработчика.");
    }
}
