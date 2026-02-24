package utmn.trifonov.cli.commands;

import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public abstract class Command {
    protected final User executor;
    protected File location;

    public Command(User executor, File location){
        this.executor = executor;
        this.location = location;
    }

    public final void execute() throws CommandExecutionException{
        updateLocation();
        if(!hasAccess()) throw new CommandExecutionException("У вас нет необходимого доступа к этому файлу/директории.");
        process();
    }

    protected abstract void process() throws CommandExecutionException;

    protected abstract boolean hasAccess();

    public User getExecutor() {
        return executor;
    }

    private void updateLocation(){
        if(Main.getCommandHandler() instanceof FileLayerCommandHandler) return;
        Main.getCommandHandler().setLocation(Directory.get(location.getLocation()));
        location = Main.getCommandHandler().getLocation();
    }

    public File getLocation() {
        return location;
    }
}
