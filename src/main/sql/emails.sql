create table emails(
    Id INTEGER primary key auto_increment,
    Sender_id INTEGER not null,
    Receiver_id INTEGER not null,
    Subject nvarchar(255) not null,
    Body text not null,
    Code nvarchar(255) unique,
    Time datetime not null ,
    foreign key (sender_id) references users(Id),
    foreign key (receiver_id) references users(Id)
)