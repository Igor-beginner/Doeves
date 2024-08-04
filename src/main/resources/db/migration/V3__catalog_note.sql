CREATE TABLE catalog(
    id SERIAL PRIMARY KEY,
    prev_catalog_id INTEGER REFERENCES catalog(id) DEFAULT NULL,
    owner_id INTEGER
        REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(40),
    date_of_create TIMESTAMP DEFAULT NOW()
);

CREATE TABLE note(
    id SERIAL PRIMARY KEY,
    title VARCHAR(30),
    description VARCHAR(300),
    date_of_create TIMESTAMP DEFAULT NOW()
);


CREATE TABLE note_catalog_ordering(
    note_id INTEGER REFERENCES note(id) ON DELETE CASCADE NOT NULL,
    prev_note_id INTEGER REFERENCES note(id) ON DELETE SET NULL,
    catalog_id INTEGER NOT NULL REFERENCES catalog(id) ON DELETE CASCADE,
    PRIMARY KEY(note_id, catalog_id)
);

ALTER TABLE users
ADD COLUMN root_catalog_id INTEGER REFERENCES catalog(id) ON DELETE SET NULL;


CREATE OR REPLACE FUNCTION check_root_catalog_id_equals_with_catalog_owner_id() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS(
        SELECT 1
        FROM catalog
        WHERE id = NEW.root_catalog_id
        AND owner_id = NEW.id
    ) THEN
        RAISE EXCEPTION 'Root catalog ID does not match the owner ID';
    END IF;

    RETURN NEW;
END
$$;

CREATE TRIGGER check_root_catalog_id_equals_with_catalog_owner_id
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION check_root_catalog_id_equals_with_catalog_owner_id();

CREATE OR REPLACE FUNCTION after_insert_user_insert_root_catalog() RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
    DECLARE catalog_id INTEGER;
BEGIN
    INSERT INTO catalog(owner_id)
    VALUES (NEW.id)
    RETURNING id INTO catalog_id;

    UPDATE users u
    SET root_catalog_id = catalog_id
    WHERE id = NEW.id;

    RETURN NEW;
END
$$;

CREATE TRIGGER after_insert_user_insert_root_catalog
    AFTER INSERT ON users
    FOR EACH ROW
EXECUTE FUNCTION after_insert_user_insert_root_catalog();