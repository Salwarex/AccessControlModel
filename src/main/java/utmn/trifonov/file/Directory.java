package utmn.trifonov.file;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.hibernate.Transaction;
import utmn.trifonov.HibernateUtil;
import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("DIRECTORY")
public class Directory extends File{
    @OneToMany(
            mappedBy = "parent",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true
    )
    private Set<File> childList = new HashSet<>(); ;

    public Directory(Long id, String path, User owner, Map<String, Integer> accessList, Directory parent, boolean shared, Set<File> childList) {
        super(id, path, owner, accessList, parent, shared);
        this.childList = childList;
    }

    public Directory() {
        super();
    }

    public Set<File> getChildList() {
        return childList;
    }

    public void setChildList(Set<File> childList) {
        this.childList = childList;
    }

    public void addChild(File child) {
        if (childList.add(child)) {
            child.setParent(this);
        }
    }

    private static List<File> initChilds(Path path){
        try (var stream = Files.list(path)) {
            return stream
                    .map(File::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения директории: " + path, e);
        }
    }

    public static Directory create(Path path, User owner, Directory parent, boolean shared){
        Directory existing = get(path);
        if (existing != null) {
            return existing;
        }

        Directory directory = new Directory();
        directory.setPath(FSUtils.toRelativePath(path));
        directory.setOwner(owner);
        directory.setAccessList(new HashMap<>());
        directory.setParent(parent);
        directory.setChildList(new HashSet<>());
        directory.setShared(shared);

        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            if (parent != null && parent.getId() != null) {
                Directory managedParent = session.get(Directory.class, parent.getId());
                directory.setParent(managedParent);

                Hibernate.initialize(managedParent.getChildList());
                managedParent.getChildList().add(directory);
            }

            session.persist(directory);

            Hibernate.initialize(directory.getAccessList());
            Hibernate.initialize(directory.getChildList());
            tx.commit();

            if(Files.notExists(path)){
                Files.createDirectories(path);
            }

            Logger.out(Logger.Color.GREEN + "Директория %s успешно создана!".formatted(directory.getFormattedPath()));
            return directory;
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка создания директории: " + path, e);
        }
    }

    public static Directory get(Path path) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (path != null) {
                Directory dir = session.createQuery(
                                """
                                SELECT DISTINCT d 
                                FROM Directory d 
                                LEFT JOIN FETCH d.owner 
                                LEFT JOIN FETCH d.accessList 
                                LEFT JOIN FETCH d.parent 
                                LEFT JOIN FETCH d.childList c 
                                    LEFT JOIN FETCH c.owner 
                                    LEFT JOIN FETCH c.accessList
                                WHERE d.path = :path
                                """,
                                Directory.class)
                        .setParameter("path", FSUtils.toRelativePath(path.normalize()))
                        .uniqueResult();

                // Дополнительная страховка: инициализируем коллекции
                if (dir != null) {
                    Hibernate.initialize(dir.getAccessList());
                    Hibernate.initialize(dir.getChildList());
                    // Инициализируем вложенные файлы (если нужно)
                    for (File child : dir.getChildList()) {
                        Hibernate.initialize(child.getAccessList());
                        if (child.getOwner() != null) {
                            Hibernate.initialize(child.getOwner());
                        }
                    }
                }
                return dir;
            }
            return null;
        }
    }

    public static Directory find(File location, String providedPath) throws CommandExecutionException {
        String path;
        if(providedPath.startsWith("~")){
            providedPath = providedPath.replaceFirst("~", "/home/%s".formatted(Main.getSession().getUser().getUsername()));
        }

        if(providedPath.charAt(0) == '/'){
            path = providedPath.replaceFirst("/", Main.REP_PATH + "/");
        }else if(location instanceof Directory){
            path = "%s/%s".formatted(location.getPath(), providedPath);
        }
        else{
            throw new CommandExecutionException("Вы редактируете файл и не можете получить дочерний объект %s".formatted(providedPath));
        }

        Directory directory = Directory.get(FSUtils.toAbsolutePath(path));

        if(directory == null)
            throw new CommandExecutionException("Директория %s не найдена!".formatted(providedPath));

        return directory;
    }
}
