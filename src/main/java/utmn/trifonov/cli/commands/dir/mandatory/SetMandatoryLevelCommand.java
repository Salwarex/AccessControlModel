package utmn.trifonov.cli.commands.dir.mandatory;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.access.mandatory.MandatoryAccessManager;
import utmn.trifonov.access.mandatory.MandatoryElement;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class SetMandatoryLevelCommand extends Command {
    private File targetFile;
    private User targetUser;
    private final String name;
    private final int level;

    public SetMandatoryLevelCommand(User executor, File location, String targetName, String levelStr) throws CommandExecutionException {
        super(executor, location);

        this.name = targetName;

        int level;
        try{
            level = Integer.parseInt(levelStr);
            if(level < 0) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new CommandExecutionException("Уровень допуска может принимать только числовые параметры, которые больше нуля");
        }

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        try {
            targetFile = File.find(location, targetName);
        }catch (CommandExecutionException e){
            targetFile = null;
        }
        if(targetFile == null) {
            targetUser = User.get(targetName);
            if(targetUser == null) throw new CommandExecutionException("Не удалось найти подходящий элемент!");
        }

        this.level = level;
    }

    @Override
    public void process() throws CommandExecutionException {
        if(!(Main.getAccessManager() instanceof MandatoryAccessManager manager))
            throw new CommandExecutionException("Данная команда исполнима только при выбранной мандатной модели доступа");

        MandatoryElement element;
        Directory userDir = null;

        if(targetFile == null){
            element = targetUser;
            userDir = Directory.find(Main.getRepository(), "/home/" + targetUser.getUsername());
        }else{
            element = targetFile;
        }

        manager.setLevel(element, level);
        if(userDir != null) manager.setLevel(userDir, level);

        Logger.pos("Для объекта %s установлен уровень допуска %d".formatted(name, level));
    }

    @Override
    protected void accessSet() {
        accessNeedAdmin();
    }
}
