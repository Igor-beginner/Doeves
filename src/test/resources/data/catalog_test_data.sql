DELETE FROM catalog;

INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (1, 1, 1, "Test catalog 0");
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (2, 0, 1, "Test catalog 1");
SELECT SETVAL('catalog_id_seq', (SELECT MAX(id) FROM catalog));

INSERT INTO note(id, order_number, catalog_id, title, description)
VALUES(1, 0, 2, "Some Note Title", "Some Description here");

INSERT INTO note(id, order_number, catalog_id, title, description)
VALUES(2, 1, 2, "Some Note Title2", "Some Description here2");

SELECT SETVAL('note_id_seq', (SELECT MAX(id) FROM note));
