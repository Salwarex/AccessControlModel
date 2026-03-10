package utmn.trifonov.access;

import utmn.trifonov.cli.CommandExecutionException;

public abstract class AccessManager {
    public static final int READ = 0b1000;
    public static final int WRITE = 0b0100;
    public static final int DELETE = 0b0010;
    public static final int TRANSFER = 0b0001;

    public boolean hasAccess(AccessSubject subject, AccessObject object, int mask) throws CommandExecutionException{
        if(subject.isRoot()) return true;
        if(!correctMask(mask))
            throw new CommandExecutionException("Ошибочный ввод маски права при проверке прав.");
        return checkAccess(subject, object, mask);
    }

    public static boolean correctMask(int mask){
        return mask <= 0b1111 && mask >= 0b0000 && (Integer.bitCount(mask) == 1);
    }

    protected abstract boolean checkAccess(AccessSubject subject, AccessObject object, int mask) throws CommandExecutionException;

    public abstract String getName();
}
