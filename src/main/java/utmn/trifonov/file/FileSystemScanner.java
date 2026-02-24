package utmn.trifonov.file;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utmn.trifonov.HibernateUtil;
import utmn.trifonov.Logger;
import utmn.trifonov.auth.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileSystemScanner {
    public static void syncDir(Path fsPath, Directory dbParent, User owner){
        if (!Files.exists(fsPath) || !Files.isDirectory(fsPath)) return;

        try (var stream = Files.list(fsPath)) {
            stream.forEach(childPath -> {
                File dbChild = File.get(childPath);

                if (dbChild == null) {
                    if (Files.isDirectory(childPath)) {
                        dbChild = Directory.create(childPath, owner, dbParent, false);
                    } else {
                        dbChild = File.create(childPath, owner, dbParent, false);
                    }
                } else if (!Objects.equals(dbChild.getParent(), dbParent)) {
                    updateParent(dbChild, dbParent);
                }

                if (dbChild instanceof Directory dir) {
                    syncDir(childPath, dir, owner);
                }
            });
        } catch (IOException e) {
            Logger.err("Ошибка сканирования: " + fsPath);
        }
    }

    private static void updateParent(File file, Directory newParent) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            File managed = session.get(File.class, file.getId());
            Directory managedNewParent = session.get(Directory.class, newParent.getId());

            if (managed != null && managedNewParent != null) {
                Directory oldParent = managed.getParent();
                if (oldParent != null) {
                    Hibernate.initialize(oldParent.getChildList());
                    oldParent.getChildList().remove(managed);
                }

                managed.setParent(managedNewParent);

                //Инициализируем перед добавлением
                Hibernate.initialize(managedNewParent.getChildList());
                managedNewParent.getChildList().add(managed);
            }
            tx.commit();
        }
    }
}
