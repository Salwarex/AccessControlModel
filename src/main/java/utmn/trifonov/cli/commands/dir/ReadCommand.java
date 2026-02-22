package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.CommandHandler;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class ReadCommand extends Command {
    private final File target;

    public ReadCommand(User executor, File location, String fileName) throws CommandExecutionException{
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        target = File.find(location, fileName);
    }

    @Override
    public void execute() throws CommandExecutionException {
        super.execute();

        if(target instanceof Directory)
            throw new CommandExecutionException("Данная команда неприменима к директории!");

        CommandHandler current = Main.getCommandHandler();
        Main.getCommandHandler().setDelegate(new FileLayerCommandHandler(target, current));
    }

    @Override
    public boolean hasAccess() {
        return ((target.getAccessValue(executor) >> 3) & 1) == 1;
    }
}
