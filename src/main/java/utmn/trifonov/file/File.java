package utmn.trifonov.file;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utmn.trifonov.HibernateUtil;
import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Entity
@Table(name = "files")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "file_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("FILE")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Directory parent;

    @ElementCollection
    @CollectionTable(
            name = "file_access_list",
            joinColumns = @JoinColumn(name = "file_id")
    )
    @MapKeyColumn(name = "username")
    @Column(name = "permissions")
    private Map<String, Integer> accessList;

    @Column(nullable = false)
    private boolean shared;

    public File(Long id, String path, User owner, Map<String, Integer> accessList, Directory parent, boolean shared) {
        this.id = id;
        this.path = path;
        this.owner = owner;
        this.accessList = accessList;
        this.parent = parent;
        this.shared = shared;
    }

    public File() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public String getFormattedPath(){
        String result = path.replaceFirst(Main.REP_PATH, "");
        String userPart = "/home/" + Main.getSession().getUser().getUsername();
        if(result.startsWith(userPart)){
            result = result.replaceFirst(userPart, "~");
        }
        return (result.isEmpty() || result.isBlank()) ? "/" : result;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Map<String, Integer> getAccessList() {
        return accessList;
    }

    public void setAccessList(Map<String, Integer> accessList) {
        this.accessList = accessList;
    }

    public Path getLocation() {
        return FSUtils.toAbsolutePath(path);
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public int getAccessValue(String key){
        if(key == null || key.isEmpty() || key.isBlank()) return 0;
        if(owner.getUsername().equals(key) || "root".equals(key))
            return (Main.getRepository().getId().equals(this.getId()) ? 0b1100 : 0b1111); //Нельзя удалять и передавать главную директорию, остальное: r+w+d+tg
        if(shared)
            return (Main.getRepository().getId().equals(this.getId()) ? 0b1000 : 0b1100);; //В главной директории - только чтение. В остальных: r+w
        if(!accessList.containsKey(key)) return 0;
        return accessList.get(key);
    }

    public int getAccessValue(User user){
        if(user == null) return 0;
        if(user.isRoot()) return (Main.getRepository().getId().equals(this.getId()) ? 0b1100 : 0b1111); //Нельзя удалять и передавать главную директорию, остальное: r+w+d+tg
        return getAccessValue(user.getUsername());
    }

    //Static operations

    public static File create(Path path, User owner, Directory parent, boolean shared){
        File existing = get(path);
        if (existing != null) {
            return existing;
        }

        File file = new File();
        file.setPath(FSUtils.toRelativePath(path));
        file.setOwner(owner);
        file.setAccessList(new HashMap<>());
        file.setParent(parent);
        file.setShared(shared);

        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            if (parent != null && parent.getId() != null) {
                Directory managedParent = session.get(Directory.class, parent.getId());
                file.setParent(managedParent);

                Hibernate.initialize(managedParent.getChildList());
                managedParent.getChildList().add(file);
            }

            session.persist(file);
            tx.commit();

            if(Files.notExists(path)){
                Files.createFile(path);
            }

            Logger.out(Logger.Color.GREEN + "Файл %s успешно создан!".formatted(file.getFormattedPath()));
            return file;
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации файла: " + path, e);
        }
    }

    public static File get(Path path){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if(path != null){
                return session.createQuery(
                                "FROM File f LEFT JOIN FETCH f.owner LEFT JOIN FETCH f.accessList WHERE f.path = :path",
                                File.class)
                        .setParameter("path", FSUtils.toRelativePath(path.normalize()))
                        .uniqueResult();
            }
            return null;
        }
    }

    public static List<File> list(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM File f LEFT JOIN FETCH f.owner LEFT JOIN FETCH f.accessList",
                    File.class).list();
        }
    }

    public static void change(Long id, FileVariables variable, Object param){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            File managedFile = session.get(File.class, id);
            if (managedFile == null) {
                throw new RuntimeException("Object not found!");
            }
            switch (variable){
                case OWNER -> {
                    if(!(param instanceof User user)) throw new IllegalArgumentException();
                    managedFile.setOwner(user);
                }
                case ACCESS_LIST -> {
                    if(!(param instanceof HashMap accessMap)) throw new IllegalArgumentException();
                    managedFile.setAccessList(accessMap);
                }
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void delete(Path path) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            File file = session.createQuery("FROM File WHERE path = :path", File.class)
                    .setParameter("path", FSUtils.toRelativePath(path.normalize()))
                    .uniqueResult();

            if (file == null) {
                Logger.err("Файла %s не существует!".formatted(path));
                return;
            }

            String localPath = file.getPath();

            session.remove(file);
            tx.commit();

            try{
                deleteRecursively(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Logger.out("Файл %s удалён!".formatted(localPath.replaceFirst(".repository", "")));
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(child -> {
                    try {
                        deleteRecursively(child);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
        Files.delete(path);
    }

    public static File find(File location, String providedPath) throws CommandExecutionException {
        String path;
        if(providedPath.startsWith("~")){
            providedPath = providedPath.replaceFirst("~", "/home/%s".formatted(Main.getSession().getUser().getUsername()));
        }

        if(providedPath.charAt(0) == '/'){
            path = providedPath.replaceFirst("/", Main.REP_PATH + "/");
        }else {
            path = "%s/%s".formatted(location.getPath(), providedPath);
        }

        File file = File.get(FSUtils.toAbsolutePath(path));

        if(file == null)
            throw new CommandExecutionException("Файл %s не найден!".formatted(providedPath));

        return file;
    }

    public static void grantAccess(File file, String username, int permissions) throws CommandExecutionException{
        if (file == null || file.getId() == null) {
            throw new CommandExecutionException("Файл не найден или не имеет ID");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            File managedFile = session.get(File.class, file.getId());
            if (managedFile == null) {
                throw new CommandExecutionException("Файл не найден в БД");
            }

            if (managedFile.getAccessList() == null) {
                managedFile.setAccessList(new HashMap<>());
            }

            managedFile.getAccessList().put(username, permissions);

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandExecutionException("Ошибка обновления прав доступа: " + e.getMessage());
        }
    }

    public enum FileVariables{
        OWNER,
        ACCESS_LIST
    }
}
