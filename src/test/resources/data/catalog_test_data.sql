INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (10, NULL, 1, 'Test catalog 0');
INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (11, 10, 1, 'Test catalog 1');
INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (12, 11, 1, 'Test catalog 2');
INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (13, 12, 1, 'Test catalog 3');
INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (14, 13, 1, 'Test catalog 4');
INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (15, 14, 1, 'Test catalog 5');
INSERT INTO catalog(id, prev_catalog_id, owner_id, title)
VALUES (16, 15, 1, 'Test catalog 6');

SELECT SETVAL('catalog_id_seq', (SELECT MAX(id) FROM catalog));

INSERT INTO note(id, title, description)
VALUES(1, 'Some Note Title', 'Some Description here');

INSERT INTO note(id, title, description)
VALUES(2, 'Some Note Title2', 'Some Description here2');

INSERT INTO note(id, title, description)
VALUES(3, 'Some Note Title2', 'Some Description here2');

INSERT INTO note_catalog_ordering(note_id, prev_note_id, catalog_id)
VALUES(1, 3, 10),
      (2, 1, 10),
      (1, NULL, 12),
      (2, 1, 12),
      (3, 2, 12),
      (3, NULL, 10);
