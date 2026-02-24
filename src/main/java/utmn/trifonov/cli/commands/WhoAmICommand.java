package utmn.trifonov.cli.commands;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.file.File;

public class WhoAmICommand extends Command {
    public WhoAmICommand(User executor, File location) {
        super(executor, location);
    }

    @Override
    public void process() throws CommandExecutionException {
        Logger.pos(executor.getUsername());
    }

    @Override
    public boolean hasAccess() {
        return true;
    }
}
