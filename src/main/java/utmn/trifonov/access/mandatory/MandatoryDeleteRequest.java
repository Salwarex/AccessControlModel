package utmn.trifonov.access.mandatory;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utmn.trifonov.HibernateUtil;
import utmn.trifonov.Logger;
import utmn.trifonov.Main;
import utmn.trifonov.auth.User;
import utmn.trifonov.cli.CommandExecutionException;
import utmn.trifonov.file.File;

import java.util.List;

@Entity
@Table(
        name = "mandatory_delete_request",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_requester_target",
                columnNames = {"requester_id", "file_id"}
        )
}
)
public class MandatoryDeleteRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File target;

    @Column(nullable = false)
    private int status;

    public MandatoryDeleteRequest(Long id, User requester, File target, int status) {
        this.id = id;
        this.requester = requester;
        this.target = target;
        this.status = status;
    }

    public MandatoryDeleteRequest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static MandatoryDeleteRequest create(User requester, File target) throws CommandExecutionException {
        if (get(requester, target) != null) {
            throw new CommandExecutionException("Запрос на удаление данным пользователем уже создан");
        }

        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            MandatoryDeleteRequest mdr = new MandatoryDeleteRequest();
            mdr.setRequester(requester);
            mdr.setTarget(target);
            mdr.setStatus(0);

            session.persist(mdr);
            tx.commit();

            Logger.out(Logger.Color.GREEN + "Запрос на удаление объекта успешно создан!");
            return mdr;
        }catch (Exception e) {
            e.printStackTrace();
            throw new CommandExecutionException("Ошибка создания запроса удаления: " + e.getMessage());
        }
    }

    public static MandatoryDeleteRequest get(User requester, File target){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            if(target != null && requester != null){
                String hql = "SELECT m FROM MandatoryDeleteRequest m " +
                        "JOIN FETCH m.requester " +
                        "JOIN FETCH m.target " +
                        "WHERE m.requester = :requester AND m.target = :target";

                return session.createQuery(hql, MandatoryDeleteRequest.class)
                        .setParameter("requester", requester)
                        .setParameter("target", target)
                        .uniqueResult();
            }
            return null;
        }
    }

    public static MandatoryDeleteRequest get(Long id){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            if(id != null){
                String hql = "SELECT m FROM MandatoryDeleteRequest m " +
                        "JOIN FETCH m.requester " +
                        "JOIN FETCH m.target " +
                        "WHERE m.id = :id";

                return session.createQuery(hql, MandatoryDeleteRequest.class)
                        .setParameter("id", id)
                        .uniqueResult();
            }
            return null;
        }
    }

    public static List<MandatoryDeleteRequest> list(){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT m FROM MandatoryDeleteRequest m " +
                    "JOIN FETCH m.requester " +
                    "JOIN FETCH m.target";

            return session.createQuery(hql, MandatoryDeleteRequest.class).list();
        }
    }

    public static void change(Long id, MandatoryDeleteRequest.Variables variable, Object param){
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            MandatoryDeleteRequest managed = session.get(MandatoryDeleteRequest.class, id);
            if (managed == null) {
                throw new RuntimeException("Object not found!");
            }
            switch (variable){
                case STATUS -> {
                    if(!(param instanceof Integer statusValue) || statusValue > 1 || statusValue < 0)
                        throw new IllegalArgumentException();
                    managed.setStatus(statusValue);
                }
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            MandatoryDeleteRequest mdr = session.createQuery("FROM MandatoryDeleteRequest WHERE id = :id", MandatoryDeleteRequest.class)
                    .setParameter("id", id)
                    .uniqueResult();

            if (mdr == null) {
                return;
            }

            session.remove(mdr);
            tx.commit();
        }
    }

    public static void accept(MandatoryDeleteRequest request, User admin) throws CommandExecutionException{
        if(!(Main.getAccessManager() instanceof MandatoryAccessManager manager))
            throw new CommandExecutionException("В настоящий момент установлена не мандатная модель доступа");

        if(!admin.isAdmin() && !admin.isRoot())
            throw new CommandExecutionException("Вы не обладаете необходимым уровнем прав для выполнения данного действия!");

        File file = request.getTarget();

        if(file == null)
            throw new CommandExecutionException("Указанный файл не найден");

        File.delete(file.getLocation());
        MandatoryDeleteRequest.delete(request.getId());

        Logger.pos("Запрос успешно одобрен");
    }

    public static void decline(MandatoryDeleteRequest request, User admin) throws CommandExecutionException{
        if(!(Main.getAccessManager() instanceof MandatoryAccessManager manager))
            throw new CommandExecutionException("В настоящий момент установлена не мандатная модель доступа");

        if(!admin.isAdmin() && !admin.isRoot())
            throw new CommandExecutionException("Вы не обладаете необходимым уровнем прав для выполнения данного действия!");

        MandatoryDeleteRequest.delete(request.getId());

        Logger.pos("Запрос успешно отклонен");
    }

    public enum Variables{
        STATUS
    }
}
