package utmn.trifonov.auth;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utmn.trifonov.HashUtils;
import utmn.trifonov.HibernateUtil;
import utmn.trifonov.Logger;
import utmn.trifonov.file.File;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean root;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<File> ownFiles;

    public User(Long id, String username, String passwordHash, boolean root, List<File> ownFiles) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.root = root;
        this.ownFiles = ownFiles;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public List<File> getOwnFiles() {
        return ownFiles;
    }

    public void setOwnFiles(List<File> ownFiles) {
        this.ownFiles = ownFiles;
    }

    public boolean passwordMatch(String provided){
        return HashUtils.isMatch(provided, passwordHash);
    }

    public static User create(String username, String password, boolean root){
        if (get(username) != null) {
            throw new IllegalArgumentException("Пользователь '" + username + "' уже существует");
        }

        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(HashUtils.hash(password));
            user.setRoot(root);
            user.setOwnFiles(new ArrayList<>());

            session.persist(user);
            tx.commit();

            Logger.out(Logger.Color.GREEN + "Пользователь %s успешно создан!".formatted(user.getUsername()));
            return user;
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка создания пользователя: " + username, e);
        }
    }

    public static User get(String username){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            if(username != null){
                return session.createQuery("FROM User WHERE username = :username", User.class)
                        .setParameter("username", username)
                        .uniqueResult();
            }
            return null;
        }
    }

    public static List<User> list(){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        }
    }

    public static void change(Long id, UserVariables variable, Object param){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            User managedBook = session.get(User.class, id);
            if (managedBook == null) {
                throw new RuntimeException("Object not found!");
            }
            switch (variable){
                case ROOT -> {
                    if(!(param instanceof Boolean rootBoolean)) throw new IllegalArgumentException();
                    managedBook.setRoot(rootBoolean);
                }
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void delete(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            User user = session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();

            if (user == null) {
                Logger.err("Пользователя %s не существует!".formatted(username));
            }

            session.remove(user);
            tx.commit();

            Logger.out(Logger.Color.GREEN + "Пользователь %s удалён!".formatted(username));
        }
    }

    public enum UserVariables{
        ROOT;
    }
}
