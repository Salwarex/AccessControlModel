package utmn.trifonov.cli.commands.dir;

import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

import java.util.Map;

public class InfoCommand extends Command {
    private final File target;

    public InfoCommand(User executor, File location, String fileName) throws CommandExecutionException {
        super(executor, location);

        if(!(location instanceof Directory directory))
            throw new CommandExecutionException("Данная команда недоступна в контексте редактирования файла.");

        target = File.find(location, fileName);
    }

    @Override
    public void process() throws CommandExecutionException {
        Logger.pos("Имя файла: \"%s\" | Владелец: %s | Ваши права: %s"
                .formatted(target.getLocation().getFileName(), target.getOwner().getUsername(),
                        permissionsToStr(target.getAccessValue(executor))));

        Map<String, Integer> acl = target.getAccessList();
        System.out.println(Logger.Color.YELLOW + "------ACL------" + Logger.Color.RESET);
        for(String key : acl.keySet()){
            System.out.println(Logger.Color.YELLOW + key + Logger.Color.WHITE + " | "
                    + Logger.Color.YELLOW + permissionsToStr(target.getAccessValue(key)) + Logger.Color.RESET);
        }
        System.out.println(Logger.Color.YELLOW + "---------------" + Logger.Color.RESET);
    }

    private String permissionsToStr(int value){
        StringBuilder result = new StringBuilder();

        char[] chars = new char[]{'r', 'w', 'd', 't'};
        for(int i = 0; i < chars.length; i++){
            if (((value >> (chars.length - 1 - i)) & 1) == 1) {
                result.append(chars[i]);
            } else {
                result.append('-');
            }
        }

        return result.toString();
    }

    @Override
    public boolean hasAccess() {
        return ((target.getAccessValue(executor) >> 3) & 1) == 1;
    }
}
