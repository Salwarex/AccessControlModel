package utmn.trifonov;


import utmn.trifonov.access.AccessManager;
import utmn.trifonov.access.discrete.DiscreteAccessManager;
import utmn.trifonov.access.mandatory.MandatoryAccessManager;
import utmn.trifonov.auth.Session;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.CommandHandler;
import utmn.trifonov.cli.CommandReceiver;
import utmn.trifonov.cli.commands.dir.mandatory.DeleteRequestListCommand;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.FSUtils;
import utmn.trifonov.file.FileSystemScanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.LogManager;

public class Main {
    private static Session session;
    private static Directory repository;

    private static CommandReceiver receiver;
    public static final String REP_PATH = ".repository";
    private static final Scanner SCANNER = new Scanner(System.in);

    private static AccessManager accessManager = null;
    private static List<Class<? extends AccessManager>> managers = List.of(DiscreteAccessManager.class, MandatoryAccessManager.class);

    static {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int select = 0;
        while(select == 0){
            Logger.out("Выберите модуль с моделью доступа, которая будет применяться в ходе выполнения программы:");
            int i = 1;
            for(Class<? extends AccessManager> clazz : managers){
                System.out.printf("%d. %s%n", i, clazz.getName());
                i++;
            }

            System.out.println("Введите номер: ");

            try {
                select = Integer.parseInt(SCANNER.nextLine()) - 1;
                if(select < 0 || select >= managers.size()) throw new IllegalArgumentException();
                accessManager = managers.get(select).getDeclaredConstructor().newInstance();
            }catch (Exception e){
                Logger.err("Введено нецифровое или недопустимое значение! Попробуйте ещё раз");
            }
        }

        System.out.printf("=== %s ===%n", accessManager.getName());
        session = new Session();

        initRepository();
        showDeletionRequests();

        receiver = new CommandReceiver(SCANNER);
        try{
            receiver.getCurrentHandler().setLocation(Directory.find(repository, "/home/" + session.getUser().getUsername()));
        }catch (CommandExecutionException e){
            Logger.err(e.getMessage());
        }
        receiver.run();

        Logger.log("Выполнение программы завершено. Введите Enter для завершения.");
        SCANNER.nextLine();
    }

    private static void showDeletionRequests(){
        if(!(accessManager instanceof MandatoryAccessManager manager)) return;
        User user = session.getUser();
        if(!user.isAdmin() && user.isRoot()) return;

        try{
            new DeleteRequestListCommand(user, repository);
        }catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initRepository(){
        try{
            User rootUser = User.get("root");
            if(rootUser == null) {
                throw new IllegalStateException("Пользователя root не существует!");
            }

            Path appBase = FSUtils.getApplicationBasePath();
            Path repoPath = appBase.resolve(REP_PATH).normalize();

            Files.createDirectories(repoPath);
            Logger.out("Путь до репозитория: \"%s\"".formatted(repoPath));

            Directory root = Directory.get(repoPath);
            if(root == null){
                root = Directory.create(repoPath, rootUser, null, true);
                Directory homeDir = Directory.create(Paths.get(repoPath.toString(), "home"), rootUser, root, false);
                Directory.create(Paths.get(homeDir.getLocation().toString(), "root"), rootUser, homeDir, false);

                Logger.out(Logger.Color.GREEN + "Корневая директория создана!");
            }

            FileSystemScanner.syncDir(repoPath, root, rootUser);

            repository = root;
        }catch (Exception e){
            Logger.err("Критическая ошибка инициализации репозитория");
            throw new RuntimeException("Не удалось инициализировать репозиторий", e);
        }
    }

    public static Scanner getScanner(){
        return SCANNER;
    }

    public static Session getSession(){
        return session;
    }

    public static CommandHandler getCommandHandler(){
        return receiver.getCurrentHandler();
    }

    public static Directory getRepository(){
        return repository;
    }

    public static AccessManager getAccessManager(){
        if(accessManager == null) Logger.wrn("Access Manager is null!");
        return accessManager;
    }
}