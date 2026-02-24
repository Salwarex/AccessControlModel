package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class UnadminCommand extends Command {
    private final User targetUser;

    public UnadminCommand(User executor, File location, String username) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        targetUser = User.get(username);
    }

    @Override
    public void process() throws CommandExecutionException {
        if(targetUser == null)
            throw new CommandExecutionException("Указанный пользователь не найден! Перепроверьте ввод.");

        User.change(targetUser.getId(), User.UserVariables.ADMIN, false);
        Logger.pos("У пользователя %s отняты права администратора!".formatted(targetUser.getUsername()));
    }

    @Override
    public boolean hasAccess() {
        return executor.isRoot();
    }
}

