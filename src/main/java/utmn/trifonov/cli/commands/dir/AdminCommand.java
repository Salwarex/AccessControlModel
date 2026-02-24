package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class AdminCommand extends Command {
    private final User targetUser;

    public AdminCommand(User executor, File location, String username) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        targetUser = User.get(username);
    }

    @Override
    public void process() throws CommandExecutionException {
        if(targetUser == null)
            throw new CommandExecutionException("Указанный пользователь не найден! Перепроверьте ввод.");

        User.change(targetUser.getId(), User.UserVariables.ADMIN, true);
        Logger.pos("Пользователю %s выданы права администратора!".formatted(targetUser.getUsername()));
    }

    @Override
    public boolean hasAccess() {
        return executor.isRoot();
    }
}
