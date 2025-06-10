package aut.ap.framework;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import java.util.function.Function;
import java.util.function.Consumer;

public class SingletonSessionFactory {
    private static SessionFactory sessionFactory = null;

    public static SessionFactory get() {
        if (sessionFactory == null) {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void inTransaction(Consumer<Session> action) {
        try (Session session = get().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                action.accept(session);
                tx.commit();
            } catch (Exception ex) {
                if (tx != null) tx.rollback();
                throw ex;
            }
        }
    }

    public static <T> T fromTransaction(Function<Session, T> action) {
        try (Session session = get().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = action.apply(session);
                tx.commit();
                return result;
            } catch (Exception ex) {
                if (tx != null) tx.rollback();
                throw ex;
            }
        }
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}