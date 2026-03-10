package utmn.trifonov.access.mandatory;

import utmn.trifonov.access.AccessManager;
import utmn.trifonov.access.AccessObject;
import utmn.trifonov.access.AccessSubject;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.file.File;

public class MandatoryAccessManager extends AccessManager {
    @Override
    protected boolean checkAccess(AccessSubject subject, AccessObject object, int mask) throws CommandExecutionException {
        if(!(object instanceof MandatoryObject mandatoryObject))
            throw new CommandExecutionException("Данный объект не может работать с установленной моделью доступа.");
        if(!(subject instanceof MandatorySubject mandatorySubject))
            throw new CommandExecutionException("Данный субъект не может работать с установленной моделью доступа.");

        int subjLevel = subject.isAdmin() ? -1 : mandatorySubject.getMandatoryLevel();
        int objLevel = mandatoryObject.getMandatoryLevel();

        if(subjLevel <= -1) return true;
        if(objLevel <= -1) return false;

        if(!correctMask(mask)) throw new CommandExecutionException("Неккоректное значение маски");

        return switch (mask){
            case 0b1000 -> objLevel <= subjLevel;
            case 0b0100 -> objLevel == subjLevel;
            case 0b0010 -> {
                if(subject.isRoot()) yield true;

                if(!object.isOwner(subject))
                    throw new CommandExecutionException("Оставить запрос на удаление может только собственник файла.");
                if(!(subject instanceof User user))
                    throw new CommandExecutionException("Оставить запрос на удаление можно только от имени пользователя.");
                if(!(object instanceof File file))
                    throw new CommandExecutionException("Оставить запрос на удаление можно только для файла или директории.");

                MandatoryDeleteRequest.create(user, file);
                throw new CommandExecutionException("ЗАПРОС НА УДАЛЕНИЕ ОТПРАВЛЕН АДМИНИСТРАТОРУ.");
            }
            case 0b0001 -> {
                yield subject.isAdmin() || subject.isRoot();
            }
            default -> throw new CommandExecutionException("Невозможно определить маску");
        };
    }

    public void setLevel(MandatoryElement element, int level) throws CommandExecutionException{
        if(level < 0)
            throw new CommandExecutionException("Минимальный допустимый уровень допуска: 0");
        if(element instanceof User user){
            User.change(user.getId(), User.UserVariables.MANDATORY, level);
        }
        else if(element instanceof File file){
            File.change(file.getId(), File.FileVariables.MANDATORY, level);
        }
        else{
            element.setMandatoryLevel(level);
        }
    }

    @Override
    public String getName() {
        return "МАНДАТНАЯ МОДЕЛЬ РАЗГРАНИЧЕНИЯ ДОСТУПА";
    }
}
