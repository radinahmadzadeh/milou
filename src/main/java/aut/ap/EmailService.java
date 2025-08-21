package aut.ap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
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
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
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
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
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
            normalizedRecipients.add(normalizeEmail(email.trim()));
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Email email = new Email(sender, subject, body, null, "original", em);
            em.persist(email);

            for (String recipientEmail : normalizedRecipients) {
                TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
                query.setParameter("email", recipientEmail);
                List<User> users = query.getResultList();
                if (!users.isEmpty()) {
                    User receiver = users.get(0);
                    EmailRecipient recipient = new EmailRecipient(email, receiver);
                    em.persist(recipient);
                }
            }

            em.getTransaction().commit();
            return "Successfully sent your email.\nCode: " + email.getCode();

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
            String jpql = "SELECT er.email FROM EmailRecipient er WHERE er.user = :user";
            if (unreadOnly) {
                jpql += " AND er.isRead = false";
            }
            jpql += " ORDER BY er.email.time DESC";
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
            TypedQuery<Email> query = em.createQuery("SELECT e FROM Email e WHERE e.sender = :user ORDER BY e.time DESC", Email.class);
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
            TypedQuery<Email> query = em.createQuery("SELECT e FROM Email e WHERE e.code = :code", Email.class);
            query.setParameter("code", code);
            List<Email> results = query.getResultList();
            if (results.isEmpty()) return null;
            Email email = results.get(0);
            TypedQuery<Long> countQuery = em.createQuery("SELECT COUNT(er) FROM EmailRecipient er WHERE er.email = :email AND er.user = :user", Long.class);
            countQuery.setParameter("email", email);
            countQuery.setParameter("user", user);
            boolean isRecipient = countQuery.getSingleResult() > 0;
            boolean isSender = email.getSender().equals(user);
            return isSender || isRecipient ? email : null;
        } finally {
            em.close();
        }
    }

    public String replyEmail(User user, String code, String body) {
        Email original = getEmailByCode(user, code);
        if (original == null) return null;
        Set<String> recipients = new HashSet<>();
        recipients.add(original.getSender().getEmail());
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<EmailRecipient> query = em.createQuery("SELECT er FROM EmailRecipient er WHERE er.email = :email", EmailRecipient.class);
            query.setParameter("email", original);
            for (EmailRecipient er : query.getResultList()) {
                recipients.add(er.getUser().getEmail());
            }
        } finally {
            em.close();
        }
        recipients.remove(user.getEmail());
        String newSubject = prefixSubject(original.getSubject(), "Re");
        String sendResult = sendEmail(user, new ArrayList<>(recipients), newSubject, body, original, "reply");
        String newCode = sendResult.contains("Code:") ? sendResult.split("Code:")[1].trim() : "UNKNOWN";
        return "Successfully sent your reply to email " + code + ".\nCode: " + newCode;
    }

    public String forwardEmail(User user, String code, List<String> newRecipients) {
        Email original = getEmailByCode(user, code);
        if (original == null) return null;
        String newSubject = prefixSubject(original.getSubject(), "Fw");
        return sendEmail(user, newRecipients, newSubject, original.getBody(), original, "forward");
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

    public String sendEmail(User sender, List<String> recipientEmails, String subject, String body,
                            Email parentEmail, String type) {
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
            Email email = new Email(sender, subject, body, parentEmail, type, em);
            em.persist(email);
            for (String recipientEmail : normalizedRecipients) {
                TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
                query.setParameter("email", recipientEmail);
                List<User> users = query.getResultList();
                if (!users.isEmpty()) {
                    User receiver = users.get(0);
                    EmailRecipient recipient = new EmailRecipient(email, receiver);
                    em.persist(recipient);
                }
            }
            em.getTransaction().commit();
            return "Successfully sent your email.\nCode: " + email.getCode();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}