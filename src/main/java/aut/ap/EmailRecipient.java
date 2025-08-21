package aut.ap;

import jakarta.persistence.*;

@Entity
@Table(name = "email_recipients")
public class EmailRecipient {

    @EmbeddedId
    private EmailRecipientId id;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @ManyToOne
    @MapsId("emailId")
    @JoinColumn(name = "email_id")
    private Email email;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public EmailRecipient() {}

    public EmailRecipient(Email email, User user) {
        this.email = email;
        this.user = user;
        this.id = new EmailRecipientId(email.getId(), user.getId());
        this.isRead = false;
    }

    public EmailRecipientId getId() {
        return id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public Email getEmail() {
        return email;
    }

    public User getUser() {
        return user;
    }
}