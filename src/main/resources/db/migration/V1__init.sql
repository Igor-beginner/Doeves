CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(30) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN DEFAULT true NOT NULL
);


CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE users_role (
    role_id INT REFERENCES role(id) NOT NULL,
    user_id INT REFERENCES users(id) NOT NULL
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

INSERT INTO role (name) VALUES ('USER');

INSERT INTO role (name) VALUES ('ADMIN');