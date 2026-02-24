package utmn.trifonov.cli.commands.dir;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utmn.trifonov.HibernateUtil;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;
import utmn.trifonov.file.FileSystemScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MakeFileCommand extends Command {

    private final String fileName;

    public MakeFileCommand(User executor, File location, String fileName) {
        super(executor, location);
        this.fileName = fileName;
    }

    @Override
    public void process() throws CommandExecutionException {
        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        if(fileName == null || fileName.isEmpty() || fileName.isBlank())
            throw new CommandExecutionException("Введено пустое или отсутствующее имя директории. Попробуйте ещё раз.");

        Path path = Path.of(directory.getLocation().toString(), fileName);
        try{
            Files.createFile(path);
        }catch (IOException e){ throw new RuntimeException(e); }

        FileSystemScanner.syncDir(directory.getLocation(), directory, executor);

        Main.getCommandHandler().setLocation(Directory.get(location.getLocation()));
    }

    @Override
    public boolean hasAccess() {
        return ((location.getAccessValue(executor) >> 2) & 1) == 1;
    }
}
