package utmn.trifonov.cli.commands;

import utmn.trifonov.Main;
import utmn.trifonov.access.AccessManager;
import utmn.trifonov.access.AccessObject;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.cli.FileLayerCommandHandler;
import utmn.trifonov.file.Directory;
import utmn.trifonov.file.File;

import java.util.HashMap;
import java.util.Map;

public abstract class Command {
    protected final User executor;
    protected File location;

    public Command(User executor, File location) throws CommandExecutionException {
        this.executor = executor;
        this.location = location;
    }

    public final void execute() throws CommandExecutionException{
        accessSet();
        updateLocation();
        if(!hasAccess()) throw new CommandExecutionException("У вас нет необходимого доступа к этому файлу/директории.");
        process();
    }

    protected abstract void process() throws CommandExecutionException;

    public User getExecutor() {
        return executor;
    }

    private void updateLocation(){
        if(Main.getCommandHandler() instanceof FileLayerCommandHandler) return;
        Main.getCommandHandler().setLocation(Directory.get(location.getLocation()));
        location = Main.getCommandHandler().getLocation();
    }

    public File getLocation() {
        return location;
    }

    //ACCESS
    private final Map<AccessObject, Integer> objectRule = new HashMap<>();
    private boolean needRoot;
    private boolean needAdmin;
    private boolean needOwner;
    private boolean specificAccessRule;

    //protected abstract boolean hasAccess();

    protected abstract void accessSet() throws CommandExecutionException;
    protected void accessRule(AccessObject object, int mask) throws CommandExecutionException{
        if(!AccessManager.correctMask(mask)) throw new CommandExecutionException("Регистрируемая маска неккоректна: %d".formatted(mask));
        objectRule.put(object, mask);
    }
    protected void accessNeedAdmin(){ this.needAdmin = true; }
    protected void accessNeedRoot(){ this.needRoot = true; }
    protected void accessNeedOwner(){ this.needOwner = true; }
    protected void accessSpecific(){this.specificAccessRule = true;}
    protected boolean accessSpecificRule() throws CommandExecutionException {
        return false;
    }

    private boolean hasAccess() throws CommandExecutionException{
        boolean result = true;
        if(needRoot) return executor.isRoot();
        if(needAdmin) return executor.isAdmin() || executor.isRoot();

        if(specificAccessRule){ return accessSpecificRule(); }

        if(objectRule.isEmpty()) return result;

        boolean eachOwn = true;
        for(AccessObject obj : objectRule.keySet()){
            //if (obj == null) continue;
            if(!obj.isOwner(executor)) eachOwn = false;
            if(!result) {
                if(needOwner) break;
                return result;
            }
            if(!Main.getAccessManager().hasAccess(executor, obj, objectRule.get(obj))) result = false;
        }

        if(needOwner) return eachOwn;

        return result;
    }
}
