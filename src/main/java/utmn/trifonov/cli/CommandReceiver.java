package utmn.trifonov.cli;

import utmn.trifonov.Logger;

import java.util.Arrays;
import java.util.Scanner;

public class CommandReceiver implements Runnable{
    private CommandHandler mainHandler;
    private final Scanner scanner;
    private boolean stop;

    public CommandReceiver(Scanner scanner) {
        this.scanner = scanner;
    }

    public void stop(){
        this.stop = true;
    }

    public CommandHandler getCurrentHandler() {
        if(mainHandler == null){
            mainHandler = new CommandHandler(null, null);
            mainHandler.setDelegate(new DirectoryLayerCommandHandler(null, mainHandler));
        }
        return mainHandler.getActor();
    }

    public CommandHandler getMainHandler(){
        return mainHandler;
    }

    @Override
    public void run() {
        while (!stop){
            String next = scanner.nextLine();
            String[] parts = next.split(" ");

            userCommandOutput(parts[0], Arrays.copyOfRange(parts, 1, parts.length));

            if(next.isEmpty() || next.isBlank()) { continue; }

            try{
                getCurrentHandler().handle(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
            }catch (CommandExecutionException e){
                Logger.pos(Logger.Color.RED + e.getMessage());
            }catch (CommandEmptyException ignored){}
        }
    }

    protected void userCommandOutput(String command, String[] args){
        String fullCommand = (args != null && args.length > 0)
                ? command + " " + String.join(" ", args)
                : command;

        Logger.usr(fullCommand);
    }
}
