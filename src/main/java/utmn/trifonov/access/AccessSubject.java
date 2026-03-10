package utmn.trifonov.access;

public interface AccessSubject {
    String getAccessSubjectIdentifier();
    boolean isRoot();
    boolean isAdmin();
}
