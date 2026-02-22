package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class ListCommand extends Command {

    public ListCommand(User executor, File location) {
        super(executor, location);
    }

    @Override
    public void execute() throws CommandExecutionException {
        super.execute();

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        for(File file : directory.getChildList()){
            System.out.println(((file.getAccessValue(executor) >> 3 & 1) == 1 ? "" : Logger.Color.RED_BACKGROUND) +
                    (file instanceof Directory ? Logger.Color.BLUE : Logger.Color.GREEN).toString()
                    + file.getLocation().getFileName() + Logger.Color.RESET);
        }
    }

    @Override
    public boolean hasAccess() {
        return ((location.getAccessValue(executor) >> 3) & 1) == 1;
    }
}
