package aut.ap;

import jakarta.persistence.Embeddable;

@Embeddable
public class EmailRecipientId implements java.io.Serializable {

    private Integer emailId;
    private Integer userId;

    public EmailRecipientId() {}

    public EmailRecipientId(Integer emailId, Integer userId) {
        this.emailId = emailId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailRecipientId)) return false;
        EmailRecipientId that = (EmailRecipientId) o;
        return emailId.equals(that.emailId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return emailId.hashCode() + userId.hashCode();
    }
}