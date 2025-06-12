package aut.ap;

import jakarta.persistence.*;
import java.util.*;

public class EmailService {
    private final EntityManagerFactory emf;

    public EmailService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public boolean signUp(String name, String email, String password) {
        email = normalizeEmail(email);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            boolean emailExists = !query.getResultList().isEmpty();

            if (emailExists || password.length() < 8) {
                em.getTransaction().rollback();
                return false;
            }

            User user = new User(name, email, password);
            em.persist(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public User login(String email, String password) {
        email = normalizeEmail(email);

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            List<User> users = query.getResultList();

            if (users.isEmpty()) return null;

            User user = users.get(0);
            return user.checkPassword(password) ? user : null;
        } finally {
            em.close();
        }
    }

    public String sendEmail(User sender, List<String> recipientEmails, String subject, String body) {
        if (sender == null || recipientEmails == null || recipientEmails.isEmpty()) {
            throw new IllegalArgumentException("Sender and recipients must be provided");
        }

        List<String> normalizedRecipients = new ArrayList<>();
        for (String email : recipientEmails) {
            normalizedRecipients.add(normalizeEmail(email));
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            for (String recipientEmail : normalizedRecipients) {
                TypedQuery<User> query = em.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class);
                query.setParameter("email", recipientEmail);
                List<User> users = query.getResultList();

                if (!users.isEmpty()) {
                    User receiver = users.get(0);
                    Email email = new Email(sender, receiver, subject, body);
                    em.persist(email);
                }
            }

            em.getTransaction().commit();
            return "Email sent successfully";
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Email> getInbox(User user, boolean unreadOnly) {
        if (user == null) return Collections.emptyList();

        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM Email e WHERE e.receiver = :user";
            if (unreadOnly) {
                jpql += " AND e.read = false";
            }
            jpql += " ORDER BY e.time DESC";

            TypedQuery<Email> query = em.createQuery(jpql, Email.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Email> getSent(User user) {
        if (user == null) return Collections.emptyList();

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Email> query = em.createQuery(
                    "SELECT e FROM Email e WHERE e.sender = :user ORDER BY e.time DESC", Email.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Email getEmailByCode(User user, String code) {
        if (user == null || code == null) return null;

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Email> query = em.createQuery(
                    "SELECT e FROM Email e WHERE e.code = :code", Email.class);
            query.setParameter("code", code);
            List<Email> results = query.getResultList();

            if (results.isEmpty()) return null;

            Email email = results.get(0);
            if (email.getSender().equals(user) || email.getReceiver().equals(user)) {
                return email;
            }
            return null;
        } finally {
            em.close();
        }
    }

    public String reply(User user, String code, String body) {
        Email original = getEmailByCode(user, code);
        if (original == null) return null;

        String newSubject = prefixSubject(original.getSubject(), "Re");
        return sendEmail(user, Collections.singletonList(String.valueOf(original.getSender().getEmail())), newSubject, body);
    }

    public String forward(User user, String code, List<String> newRecipients) {
        Email original = getEmailByCode(user, code);
        if (original == null) return null;

        String newSubject = prefixSubject(original.getSubject(), "Fw");
        return sendEmail(user, newRecipients, newSubject, original.getBody());
    }

    public String replyEmail(User user, String code, String body) {
        return reply(user, code, body);
    }

    public String forwardEmail(User user, String code, List<String> newRecipients) {
        return forward(user, code, newRecipients);
    }

    private String normalizeEmail(String email) {
        if (email == null) return "";
        if (!email.endsWith("@milou.com")) {
            return email + "@milou.com";
        }
        return email;
    }

    private String prefixSubject(String subject, String prefix) {
        if (subject == null) return "";
        switch (prefix) {
            case "Re":
                return subject.startsWith("[Re]") ? subject : "[Re] " + subject;
            case "Fw":
                return subject.startsWith("[Fw]") ? subject : "[Fw] " + subject;
            default:
                return subject;
        }
    }
}
