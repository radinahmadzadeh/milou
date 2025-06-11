package aut.ap;

import aut.ap.framework.SingletonSessionFactory;
import java.util.List;

public class UserService {

    public static User register(String name, String email, String password) {
        User user = new User(name, email, password);
        SingletonSessionFactory.inTransaction(session -> session.persist(user));
        return user;
    }

    public static boolean emailExists(String email) {
        return SingletonSessionFactory.fromTransaction(session ->
                session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                        .setParameter("email", email)
                        .getSingleResult() > 0
        );
    }

    public static User login(String email, String password) {
        return SingletonSessionFactory.fromTransaction(session ->
                session.createQuery("FROM User u WHERE u.email = :email AND u.password = :password", User.class)
                        .setParameter("email", email)
                        .setParameter("password", password)
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }

    public static List<User> getAll() {
        return SingletonSessionFactory.fromTransaction(session ->
                session.createQuery("FROM User", User.class)
                        .getResultList()
        );
    }

    public static void remove(Integer id) {
        SingletonSessionFactory.inTransaction(session -> {
            User user = session.getReference(User.class, id);
            session.remove(user);
        });
    }
}
