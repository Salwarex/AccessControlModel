package utmn.trifonov.cli;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.commands.Command;
import utmn.trifonov.cli.commands.file.*;
import utmn.trifonov.file.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileLayerCommandHandler extends CommandHandler{
    private final List<String> content;

    private int chosenLineIdx = -1;
    private String chosenLineContent;

    public FileLayerCommandHandler(File location, CommandHandler parent) {
        super(location, parent);

        Logger.pos("Вы редактируете файл %s".formatted(location.getLocation().getFileName()));

        try{
            content = Files.readAllLines(location.getLocation());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileOutput();
    }

    public void fileOutput(){
        System.out.println(Logger.Color.YELLOW + ">>>" + Logger.Color.RESET);

        int i = 0;
        for(String s : content){
            System.out.printf(Logger.Color.YELLOW + "%3d." + Logger.Color.RESET + "%s%n", i, s);
            i++;
        }

        System.out.println(Logger.Color.YELLOW + "<<<" + Logger.Color.RESET);
    }

    public void lineChoose(int chosenId) throws CommandExecutionException{
        if(chosenId >= content.size() || chosenId < 0)
            throw new CommandExecutionException("Строки с указанным индексом не найдено!");
        this.chosenLineIdx = chosenId;
        this.chosenLineContent = content.get(chosenLineIdx);
        Logger.pos("Вы редактируете строку %d".formatted(chosenLineIdx));
        Logger.pos(Logger.Color.YELLOW + ">>>" + Logger.Color.WHITE + " %s".formatted(chosenLineContent));
    }

    public void lineStop() throws CommandExecutionException{
        if(chosenLineIdx == -1)
            throw new CommandExecutionException("На данный момент ни одна строка не выбрана!");
        this.chosenLineIdx = -1;
        this.chosenLineContent = null;
        Logger.pos("Вы закончили редактирование строки");
    }

    public void setLine(String value) throws CommandExecutionException{
        if(chosenLineIdx == -1)
            throw new CommandExecutionException("На данный момент ни одна строка не выбрана!");
        if(value == null)
            throw new CommandExecutionException("Предоставлена отсутствующая строка. Попробуйте ещё раз.");

        this.chosenLineContent = value;
        this.content.set(chosenLineIdx, chosenLineContent);
        Logger.pos("Вы установили новое значение для строки %d: %s".formatted(chosenLineIdx, chosenLineContent));
        lineStop();
    }

    public void newLine(String value) throws CommandExecutionException{
        if(chosenLineIdx != -1)
            throw new CommandExecutionException("На данный момент вы редактируете строку %d. Выйдите из режима редактирования: exit-line"
                    .formatted(chosenLineIdx));
        if(value == null)
            throw new CommandExecutionException("Предоставлена отсутствующая строка. Попробуйте ещё раз.");

        this.content.add(value);

        Logger.pos("Добавлена новая строка: %s".formatted(value));
    }

    public void write() throws CommandExecutionException{
        Path path = location.getLocation();
        if(!Files.isWritable(path))
            throw new CommandExecutionException("Ошибка записи: Отсутствие прав на уровне системы.");
        try{
            Files.write(path, content, StandardOpenOption.TRUNCATE_EXISTING);
        }catch (IOException e){
            throw new CommandExecutionException("Возникла ошибка записи: %s".formatted(e.getMessage()));
        }
    }

    @Override
    protected Command commandPreProcessor(String command, String[] args) throws CommandExecutionException, CommandEmptyException {
        User executor = Main.getSession().getUser();
        return switch(command){
            case "exit" -> new ExitCommand(executor, location);
            case "ls" -> new OutCommand(executor, location);
            case "line" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: line <индекс строки>");
                int id;
                try{
                    id = Integer.parseInt(args[0]);
                }catch (Exception e){
                    throw new CommandExecutionException("Команда ожидает на вход числовой аргумент!");
                }

                yield new SetLineCommand(executor, location, id);
            }
            case "exit-line" -> new ExitLineCommand(executor, location);
            case "new-line" -> {
                if(args.length == 0)
                    throw new CommandExecutionException("Данная команда предполагает наличие одного аргумента: new-line <содержание>");
                yield new NewLineCommand(executor, location, String.join(" ", args));
            }
            case "write" -> new WriteCommand(executor, location);
            case null -> {
                throw new CommandEmptyException();
            }
            default -> {
                if(hasChosenLine()) yield new SetLineProcess(executor, location, command + " " + String.join(" ", args));
                throw new CommandExecutionException("Команда неизвестна или невыполнима в данном контексте. Попробуйте ещё раз.");
            }
        };
    }

    public boolean hasChosenLine(){
        return chosenLineIdx != -1;
    }

    public int getChosenLineIdx(){
        return chosenLineIdx;
    }
}
