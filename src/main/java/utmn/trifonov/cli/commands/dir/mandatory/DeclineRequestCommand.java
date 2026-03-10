package utmn.trifonov.cli.commands.dir.mandatory;

import utmn.trifonov.access.mandatory.MandatoryDeleteRequest;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class DeclineRequestCommand extends Command {
    private final MandatoryDeleteRequest request;

    public DeclineRequestCommand(User executor, File location, String identifier) throws CommandExecutionException {
        super(executor, location);

        long id;
        try{
            id = Long.parseLong(identifier);
        }catch (NumberFormatException e){
            throw new CommandExecutionException("Введен неккоректный id запроса!");
        }

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        request = MandatoryDeleteRequest.get(id);
    }

    @Override
    public void process() throws CommandExecutionException {
        if(request == null)
            throw new CommandExecutionException("Указанный запрос пуст. Попробуйте ещё раз.");

        MandatoryDeleteRequest.decline(request, executor);
    }

    @Override
    protected void accessSet() {
        accessNeedAdmin();
    }
}
