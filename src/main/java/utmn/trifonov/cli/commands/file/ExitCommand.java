package utmn.trifonov.cli.commands.file;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class ExitCommand extends Command {
    public ExitCommand(User executor, File location) {
        super(executor, location);
    }

    @Override
    public void process() throws CommandExecutionException {
        if(location instanceof Directory directory)
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования директории.");

        Main.getCommandHandler().stopDelegation();
        Logger.pos("Вы покинули режим редактирования файла.");
    }

    @Override
    public boolean hasAccess() {
        return true;
    }
}
