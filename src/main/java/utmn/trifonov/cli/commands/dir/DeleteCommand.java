package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.CommandHandler;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

import java.util.Objects;

public class DeleteCommand extends Command {
    private final File target;

    public DeleteCommand(User executor, File location, String fileName) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        target = File.find(location, fileName);
    }

    @Override
    public void execute() throws CommandExecutionException {
        super.execute();
        if(Objects.equals(location.getId(), target.getId())){
            Main.getCommandHandler().setLocation(File.find(location, "/"));
        }

        File.delete(target.getLocation());
    }

    @Override
    public boolean hasAccess() {
        return ((target.getAccessValue(executor) >> 1) & 1) == 1;
    }
}
