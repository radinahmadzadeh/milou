import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "Name")
    private String name;

    @Basic(optional = false)
    @Column(name = "Email")
    private String email;

    @Basic(optional = false)
    @Column(name = "Password")
    private String password;

    private List<Email> inbox = new ArrayList<>();

    private List<Email> sent = new ArrayList<>();

    private Set<String> readCodes = new HashSet<>();

    public User() {}


    public User(String name, String email, String password) {
        this.name = name;
        if (email.endsWith("@milou.com")) {
            this.email = email;
        } else {
            this.email = email + "@milou.com";
        }
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void receiveEmail(Email email) {
        inbox.add(email);
    }

    public void sendEmail(Email email) {
        sent.add(email);
    }

    public List<Email> getInbox() {
        return inbox;
    }

    public List<Email> getSent() {
        return sent;
    }

    public void markAsRead(String code) {
        readCodes.add(code);
    }

    public boolean isRead(String code) {
        return readCodes.contains(code);
    }

    public Set<String> getReadCodes() {
        return readCodes;
    }
}
