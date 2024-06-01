CREATE TABLE verification_details (
    id SERIAL PRIMARY KEY,
    code VARCHAR(6) NOT NULL,
    expire_date TIMESTAMP NOT NULL ,
    missing_attempts INTEGER NOT NULL DEFAULT 5
);

ALTER TABLE users
ADD COLUMN verified BOOLEAN DEFAULT false;

ALTER TABLE users
ADD COLUMN verification_details_id INTEGER REFERENCES verification_details(id);

