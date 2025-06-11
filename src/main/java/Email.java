import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @ManyToOne
    @JoinColumn(name = "Receiver_id", nullable = false)
    private User receiver;

    @Basic(optional = false)
    @Column(name = "Subject")
    private String subject;

    @Basic(optional = false)
    @Column(name = "Body", columnDefinition = "TEXT")
    private String body;

    @Basic(optional = false)
    @Column(name = "Code", unique = true)
    private String code;

    @Basic(optional = false)
    @Column(name = "Time")
    private LocalDateTime time;

    public Email() {}

    public Email(User sender, User receiver, String subject, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.body = body;
        this.code = UUID.randomUUID().toString();
        this.time = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
