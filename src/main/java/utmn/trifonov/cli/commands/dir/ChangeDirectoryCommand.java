package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class ChangeDirectoryCommand extends Command {
    private final Directory targetDir;

    public ChangeDirectoryCommand(User executor, File location, String target) throws CommandExecutionException{
        super(executor, location);
        target = target == null ? "/home/%s".formatted(executor.getUsername()) : target;
        targetDir = Directory.find(location, target);
    }

    @Override
    public void process() throws CommandExecutionException {
        Main.getCommandHandler().getActor().setLocation(targetDir);
        Logger.pos("%s -> %s".formatted(location.getFormattedPath(), targetDir.getFormattedPath()));
    }

    @Override
    public boolean hasAccess() {
        return (targetDir.getAccessValue(executor) >> 3 & 1) == 1;
    }
}
