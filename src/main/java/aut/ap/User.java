package aut.ap;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "receiver")
    private List<Email> inbox = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    private List<Email> sent = new ArrayList<>();

    @ElementCollection
    private Set<String> readCodes = new HashSet<>();

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email.endsWith("@milou.com") ? email : email + "@milou.com";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
