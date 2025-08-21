package aut.ap;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Sender_id", nullable = false)
    private User sender;

    @Basic(optional = false)
    @Column(name = "Subject")
    private String subject;

    @Basic(optional = false)
    @Column(name = "Body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "Code")
    private String code;

    @Basic(optional = false)
    @Column(name = "Time")
    private LocalDateTime time;

    @ManyToOne
    @JoinColumn(name = "Parent_email_id")
    private Email parentEmail;

    @Column(name = "Type")
    private String type;

    public Email() {}

    public Email(User sender, String subject, String body, Email parentEmail, String type, EntityManager em) {
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.code = generateUnique6DigitCode(em);
        this.time = LocalDateTime.now();
        this.parentEmail = parentEmail;
        this.type = type;
    }

    public Integer getId() { return id; }
    public User getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getCode() { return code; }
    public LocalDateTime getTime() { return time; }
    public Email getParentEmail() { return parentEmail; }
    public String getType() { return type; }

    private String generateUnique6DigitCode(EntityManager em) {
        Random random = new Random();
        String code;
        boolean unique;
        do {
            int number = 100000 + random.nextInt(900000);
            code = String.valueOf(number);
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(e) FROM Email e WHERE e.code = :code", Long.class);
            query.setParameter("code", code);
            Long count = query.getSingleResult();
            unique = (count == 0);
        } while (!unique);
        return code;
    }
}