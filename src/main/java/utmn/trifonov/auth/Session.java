package utmn.trifonov.auth;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;

public class Session {
    private User user;

    public Session(){
        root();
        if(user == null) authProcess();
    }

    private void authProcess(){
        Logger.wrn("Пожалуйста, авторизуйтесь!");
        while(true){
            Logger.outL("Имя пользователя: "); String username = Main.getScanner().nextLine();
            Logger.outL("Пароль: "); String providedPass = Main.getScanner().nextLine();

            User user = User.get(username);
            if(user == null) {authError(); continue;}
            if(!user.passwordMatch(providedPass)){ authError(); continue; }

            Logger.out(
                    Logger.Color.GREEN + "Вы успешно вошли как %s!".formatted(user.getUsername())
            );
            this.user = user;
            return;
        }

    }

    private void root(){
        User root = User.get("root");
        if(root != null) {
            if(!root.isRoot()) User.change(root.getId(), User.UserVariables.ROOT, true);
            return;
        }

        Logger.wrn("Суперпользователь (root) не был создан, задайте пароль для него.");

        Logger.outL("Password: "); String providedPass = Main.getScanner().nextLine();
        root = User.create("root", providedPass, true);
    }

    private static void authError(){
        Logger.wrn("Неверный логин или пароль! Попробуйте ещё раз.");
    }

    public User getUser() {
        return user;
    }
}
