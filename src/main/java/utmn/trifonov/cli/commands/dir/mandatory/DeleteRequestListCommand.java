package utmn.trifonov.cli.commands.dir.mandatory;

import utmn.trifonov.access.mandatory.MandatoryDeleteRequest;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

import java.util.List;

public class DeleteRequestListCommand extends Command {

    public DeleteRequestListCommand(User executor, File location) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");
    }

    @Override
    public void process() throws CommandExecutionException {
        List<MandatoryDeleteRequest> requests = MandatoryDeleteRequest.list();
        System.out.printf("| %-5s | %-15s | %-30s |%n", "ID", "Имя пользователя", "Путь до файла");
        System.out.println("|-------|-----------------|------------------------------------|");

        for(MandatoryDeleteRequest mdr : requests){
            System.out.printf("| %-5s | %-15s | %-30s |%n",
                    mdr.getId(), mdr.getRequester().getUsername(), mdr.getTarget().getFormattedPath());
        }

        System.out.println("|-------|-----------------|------------------------------------|");
    }

    @Override
    protected void accessSet() {
        accessNeedAdmin();
    }
}
