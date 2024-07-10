CREATE TABLE catalog(
    id SERIAL PRIMARY KEY,
    order_number INTEGER NOT NULL,
    owner_id INTEGER
        REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(40) NOT NULL,
    date_of_create TIMESTAMP DEFAULT NOW()
);

CREATE TABLE note(
    id SERIAL PRIMARY KEY,
    catalog_id INTEGER
        REFERENCES catalog(id) ON DELETE CASCADE,
    owner_id INTEGER NOT NULL
        REFERENCES users(id) ON DELETE  CASCADE,
    title VARCHAR(30) NOT NULL,
    description VARCHAR(300),
    date_of_create TIMESTAMP DEFAULT NOW()
);

CREATE TYPE context_enum AS ENUM ('home_page', 'catalog');


CREATE TABLE note_order(
    note_id INTEGER REFERENCES note(id) ON DELETE CASCADE NOT NULL,
    context context_enum NOT NULL,
    order_number INTEGER,
    PRIMARY KEY(note_id, context)
);