package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

public class AccessListCommand extends Command {
    private final File targetFile;
    private final User targetUser;
    private final int newPerms;
    private boolean replace;
    private boolean add;

    private static final int READ   = 0b1000;
    private static final int WRITE  = 0b0100;
    private static final int DELETE = 0b0010;
    private static final int TG     = 0b0001;

    public AccessListCommand(User executor, File location, String fileName, String username, Object permissionValue) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        targetFile = File.find(location, fileName);
        targetUser = User.get(username);

        int providedPerms = parsePermissions(permissionValue);

        if(replace){
            newPerms = providedPerms;
        }
        else{
            int current = targetFile.getAccessValue(targetUser);
            newPerms = add ? current | providedPerms : current ^ providedPerms;
        }
    }

    private int parsePermissions(Object permissionValue) throws CommandExecutionException{
        if(permissionValue instanceof String perm){
            try{
                int i = Integer.parseInt(perm);
                return parseValue(i);
            }catch (Exception ignore){}
            return parseValue(perm);
        }
        throw new CommandExecutionException("Неверное значение аргумента");
    }

    private int parseValue(String perm) throws CommandExecutionException{
        if(perm.startsWith("+")) { add = true; perm = perm.replace("+", ""); }
        else if (perm.startsWith("-")) { add = false; perm = perm.replace("-", ""); }
        else replace = true;

        int result = 0;
        for(char c : perm.toCharArray()){
            result |= switch (c){
                case 'r' -> READ;
                case 'w' -> WRITE;
                case 'd' -> DELETE;
                case 't' -> TG;
                default -> throw new CommandExecutionException("Введено неверное значение аргумента: %s".formatted(c));
            };
        }
        return result;
    }

    private int parseValue(Integer perm) throws CommandExecutionException{
        replace = true;
        if(perm > 15 || perm < 0) throw new CommandExecutionException("Введено неверное значение. Ограничение: 0 <= x <= 15");
        return perm;
    }

    @Override
    public void process() throws CommandExecutionException {
        if(targetUser == null)
            throw new CommandExecutionException("Указанный пользователь не найден! Перепроверьте ввод.");

        File.grantAccess(targetFile, targetUser.getUsername(), newPerms);
        Logger.pos("Вы успешно изменили права пользователя %s. Новое значение: %d".formatted(targetUser.getUsername(), newPerms));
    }

    @Override
    public boolean hasAccess() {
        return ((newPerms & 0b0001) != 0b0001 ? ((targetFile.getAccessValue(executor)) & 1) == 1 :
                targetFile.getOwner().getUsername().equals(executor.getUsername()));
        //если добавляется право не TG, то смотрит на наличие TG, если TG, то на владение файлом.
    }
}
