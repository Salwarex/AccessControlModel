package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.FSUtils;
import utmn.trifonov.file.File;
import utmn.trifonov.file.FileSystemScanner;

import java.nio.file.Path;

public class MakeUserCommand extends Command {
    private final String username;
    private final String password;

    public MakeUserCommand(User executor, File location, String username, String password) {
        super(executor, location);
        this.username = username;
        this.password = password;
    }

    @Override
    public void execute() throws CommandExecutionException {
        super.execute();
        if(username == null || username.isEmpty() || username.isBlank())
            throw new CommandExecutionException("Введено пустое или отсутствующее имя пользователя. Попробуйте ещё раз.");
        if(password == null || password.isEmpty() || password.isBlank())
            throw new CommandExecutionException("Введен пустой или отсутствующий пароль. Попробуйте ещё раз.");

        User user = User.create(username, password, false);
        Directory homeDir = Directory.find(Main.getRepository(), "/home");

        Path userDirPath = FSUtils.getApplicationBasePath()
                .resolve(Main.REP_PATH)
                .resolve("home")
                .resolve(username)
                .normalize();

        Directory.create(userDirPath, user, homeDir, false);
    }

    @Override
    public boolean hasAccess() {
        return executor.isRoot();
    }
}
