CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    username TEXT NOT NULL,
    password TEXT NOT NULL
);

CREATE TABLE authorities
(
    ID   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE users_authorities
(
    user_id      SERIAL NOT NULL,
    authority_id SERIAL NOT NULL,
    PRIMARY KEY (user_id, authority_id)
);

INSERT INTO users(username, password)
VALUES ('alice', '$2a$10$hc0ust6BkOkGSCNemqWHC.zKgpCuEIN.Qx/2XTSYB11Qxot8bfDIS');

INSERT INTO authorities(name)
VALUES ('READ'),
       ('WRITE'),
       ('UPDATE');

INSERT INTO users_authorities(user_id, authority_id)
VALUES (1, 1),
       (1, 2),
       (1, 3);
