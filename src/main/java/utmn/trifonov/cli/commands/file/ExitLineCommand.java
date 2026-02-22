package utmn.trifonov.cli.commands.file;

import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class ExitLineCommand extends Command {
    public ExitLineCommand(User executor, File location) {
        super(executor, location);
    }

    @Override
    public void execute() throws CommandExecutionException {
        super.execute();
        if(location instanceof Directory directory)
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования директории.");

        if(!(Main.getCommandHandler() instanceof FileLayerCommandHandler flch))
            throw new CommandExecutionException("Ошибка делегата.");

        flch.lineStop();
    }

    @Override
    public boolean hasAccess() {
        return true;
    }
}
