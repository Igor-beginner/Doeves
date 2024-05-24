CREATE TABLE role (
    id INTEGER PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

INSERT INTO role (id, name) VALUES (1, 'USER');

INSERT INTO role (id, name) VALUES (2, 'ADMIN');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(30) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN DEFAULT true NOT NULL,
    role_id INTEGER REFERENCES role(id) DEFAULT 1
);

CREATE TABLE task (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    complete BOOLEAN DEFAULT false,
    date_of_create TIMESTAMP DEFAULT now() NOT NULL,
    deadline TIMESTAMP,
    owner_id INTEGER REFERENCES users(id) NOT NULL
);



