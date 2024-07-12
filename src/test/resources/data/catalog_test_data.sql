DELETE FROM catalog;

INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (1, 1, 1, 'Test catalog 0');
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (2, 0, 1, 'Test catalog 1');
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (3, 2, 1, 'Test catalog 2');
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (4, 3, 1, 'Test catalog 3');
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (5, 4, 1, 'Test catalog 4');
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (6, 5, 1, 'Test catalog 5');
INSERT INTO catalog(id, order_number, owner_id, title)
VALUES (7, 6, 1, 'Test catalog 6');

SELECT SETVAL('catalog_id_seq', (SELECT MAX(id) FROM catalog));

INSERT INTO note(id, owner_id, catalog_id, title, description)
VALUES(1, 1, 2, 'Some Note Title', 'Some Description here');

INSERT INTO note(id, owner_id, catalog_id, title, description)
VALUES(2, 1, 2, 'Some Note Title2', 'Some Description here2');

INSERT INTO note(id, owner_id, catalog_id, title, description)
VALUES(3, 1, NULL, 'Some Note Title2', 'Some Description here2');

SELECT SETVAL('note_id_seq', (SELECT MAX(id) FROM note));

INSERT INTO note_order(note_id, context, context_id, order_number)
VALUES(1, 'catalog', 2, 0),
      (2, 'catalog', 2, 1),
      (3, 'catalog', 2, 2);
