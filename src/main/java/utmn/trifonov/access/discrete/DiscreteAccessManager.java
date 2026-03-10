package utmn.trifonov.access.discrete;

import utmn.trifonov.access.AccessManager;
import utmn.trifonov.access.AccessObject;
import utmn.trifonov.access.AccessSubject;
import utmn.trifonov.cli.CommandExecutionException;

public class DiscreteAccessManager extends AccessManager {

    @Override
    public boolean checkAccess(AccessSubject subject, AccessObject object, int mask) throws CommandExecutionException {
        if(!(object instanceof DiscreteObject discreteObject))
            throw new CommandExecutionException("Данный объект не может работать с установленной моделью доступа.");
        if(!(object instanceof DiscreteSubject discreteSubject))
            throw new CommandExecutionException("Данный субъект не может работать с установленной моделью доступа.");

        if(object.isOwner(subject)) return true;

        int b = discreteObject.getAccessValue(discreteSubject);
        return Integer.bitCount(b & mask) == 1;
        //Например, имеет права 1101, проверка на READ (1000):
        //1101 & 1000 => 1000
        //Если бы прав не было, было бы 0101 & 1000 => 0000
        //bitCount проверяет наличие этого бита в объединении.
    }

    @Override
    public String getName() {
        return "ДИСКРЕТНАЯ МОДЕЛЬ РАЗГРАНИЧЕНИЯ ДОСТУПА";
    }
}
