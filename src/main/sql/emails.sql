CREATE TABLE emails(
    Id INTEGER PRIMARY KEY AUTO_INCREMENT,
    Sender_id INTEGER NOT NULL,
    Subject NVARCHAR(255) NOT NULL,
    Body TEXT NOT NULL,
    Code NVARCHAR(255) UNIQUE,
    Time DATETIME NOT NULL,
    Parent_email_id INTEGER,
    Type NVARCHAR(50),
    FOREIGN KEY (Sender_id) REFERENCES users(Id),
    FOREIGN KEY (Parent_email_id) REFERENCES emails(Id)
);