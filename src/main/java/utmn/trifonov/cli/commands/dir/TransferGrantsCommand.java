package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class TransferGrantsCommand extends Command {
    private final File target;
    private final User newOwner;

    public TransferGrantsCommand(User executor, File location, String fileName, String username) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        target = File.find(location, fileName);
        newOwner = User.get(username);
    }

    @Override
    public void process() throws CommandExecutionException {
        if(newOwner == null)
            throw new CommandExecutionException("Указанный пользователь не найден! Перепроверьте ввод.");


        File.change(target.getId(), File.FileVariables.OWNER, newOwner);
        Logger.pos("Вы успешно передали права на управление %s пользователю %s".formatted(target.getFormattedPath(), newOwner.getUsername()));
    }

    @Override
    public boolean hasAccess() {
        return ((target.getAccessValue(executor)) & 1) == 1;
    }
}
