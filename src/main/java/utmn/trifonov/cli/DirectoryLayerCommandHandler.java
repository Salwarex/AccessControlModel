package utmn.trifonov.cli;

import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.cli.commands.WhoAmICommand;
import utmn.trifonov.cli.commands.dir.*;
import utmn.trifonov.file.Directory;

public class DirectoryLayerCommandHandler extends CommandHandler{
    public DirectoryLayerCommandHandler(Directory location, CommandHandler parent) {
        super(location, parent);
    }

    @Override
    protected Command commandPreProcessor(String command, String[] args) throws CommandExecutionException, CommandEmptyException {
        User executor = Main.getSession().getUser();
        return switch(command){
            case "whoami" -> new WhoAmICommand(executor, location);
            case "cd" -> {
                String target = null;
                if(args.length != 0) target = args[0];
                yield new ChangeDirectoryCommand(executor, location, target);
            }
            case "ls" -> new ListCommand(executor, location);
            case "mkdir" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: mkdir <имя директории>");

                yield new MakeDirectoryCommand(executor, location, args[0]);
            }
            case "mkfile" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: mkfile <имя файла>");

                yield new MakeFileCommand(executor, location, args[0]);
            }
            case "mkuser" -> {
                if(args.length < 2)
                    throw new CommandExecutionException("Данная команда предполагает наличие двух аргумента: mkuser <имя пользователя> <пароль>");

                yield new MakeUserCommand(executor, location, args[0], args[1]);
            }
            case "delete" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: delete <имя файла>");

                yield new DeleteCommand(executor, location, args[0]);
            }
            case "info" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: info <имя файла>");

                yield new InfoCommand(executor, location, args[0]);
            }
            case "read" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: read <имя файла>");

                yield new ReadCommand(executor, location, args[0]);
            }
            case "acl" -> {
                if(args.length < 3)
                    throw new CommandExecutionException("Данная команда предполагает наличие трёх аргументов: acl <целевой файл> <имя пользователя> <значение>");

                yield new AccessListCommand(executor, location, args[0], args[1], args[2]);
            }
            case "tg" -> {
                if(args.length < 2)
                    throw new CommandExecutionException("Данная команда предполагает наличие двух аргумента: tg <целевой файл> <имя пользователя>");

                yield new TransferGrantsCommand(executor, location, args[0], args[1]);
            }
            case null -> {
                throw new CommandEmptyException();
            }
            default -> {
                if(parent == null){
                    throw new CommandExecutionException("Команда неизвестна или невыполнима в данном контексте. Попробуйте ещё раз.");
                }
                else{
                    yield parent.commandPreProcessor(command, args);
                }
            }
        };
    }
}
