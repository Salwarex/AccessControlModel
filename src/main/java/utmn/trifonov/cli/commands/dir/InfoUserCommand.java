package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class InfoUserCommand extends Command {
    private final User target;

    public InfoUserCommand(User executor, File location, String username) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        target = User.get(username);
    }

    @Override
    public void process() throws CommandExecutionException {
        Logger.pos("ID: %d | Имя пользователя: %s | Особый статус: %s | Уровень доступа (M): %d"
                .formatted(target.getId(), target.getUsername(),
                        (target.isAdmin() ? (target.isRoot() ? "Root" : "Admin") : "Отсутствует"), target.getMandatoryLevel()));
        //в будущем добавить атрибуты RBAC
    }

    @Override
    protected void accessSet() throws CommandExecutionException {
        accessNeedAdmin();
    }
}
