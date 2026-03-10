package utmn.trifonov.cli.commands;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.file.File;

public class WhoAmICommand extends Command {
    public WhoAmICommand(User executor, File location) throws CommandExecutionException {
        super(executor, location);
    }

    @Override
    public void process() throws CommandExecutionException {
        Logger.pos(executor.getUsername());
    }

    @Override
    protected void accessSet() throws CommandExecutionException {}

}
