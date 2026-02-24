package utmn.trifonov.cli.commands.file;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class WriteCommand extends Command {
    public WriteCommand(User executor, File location) {
        super(executor, location);
    }

    @Override
    public void process() throws CommandExecutionException {
        if(location instanceof Directory directory)
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования директории.");

        if(!(Main.getCommandHandler() instanceof FileLayerCommandHandler flch))
            throw new CommandExecutionException("Ошибка делегата.");

        flch.write();
        Logger.pos("Файл успешно сохранён.");
    }

    @Override
    public boolean hasAccess() {
        return ((location.getAccessValue(executor) >> 2) & 1) == 1;
    }
}
