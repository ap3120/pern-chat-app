create table users
(
    user_id  serial primary key,
    username varchar(50) unique not null,
    password varchar(200)       not null
);
create table chats
(
    chat_id    serial primary key,
    created_by integer references users (user_id),
    created_at timestamp
);
create table users_chats
(
    user_id integer references users (user_id),
    chat_id integer references chats (chat_id),
    unique (user_id, chat_id)
);
create table messages
(
    message_id        serial primary key,
    content           varchar(1000),
    sender_id         integer references users (user_id),
    sender_username   varchar(50),
    receiver_id       integer,
    receiver_username varchar(50),
    chat_id           integer references chats (chat_id),
    send_at           timestamp
);

CREATE TABLE direct_chats
(
    chat_id INTEGER serial PRIMARY KEY
        REFERENCES chats (chat_id)
            ON DELETE CASCADE,
    user_1  INTEGER NOT NULL
        REFERENCES users (user_id),
    user_2  INTEGER NOT NULL
        REFERENCES users (user_id),
    CHECK (user_1 < user_2),
    UNIQUE (user_1, user_2)
);
