CREATE TABLE email_recipients (
    email_id INT,
    user_id INT,
    is_read BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (email_id, user_id),
    FOREIGN KEY (email_id) REFERENCES emails(Id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(Id) ON DELETE CASCADE
);