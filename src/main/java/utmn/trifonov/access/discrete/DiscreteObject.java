package utmn.trifonov.access.discrete;

import utmn.trifonov.access.AccessObject;

public interface DiscreteObject extends AccessObject {
    int getAccessValue(String key);
    int getAccessValue(DiscreteSubject subject);
}
