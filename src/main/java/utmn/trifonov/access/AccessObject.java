package utmn.trifonov.access;

public interface AccessObject {
    String getAccessObjectIdentifier();
    boolean isOwner(AccessSubject subject);
}
