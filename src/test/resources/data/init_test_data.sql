DELETE FROM task;
DELETE FROM users;
DELETE FROM verification_details;

INSERT INTO verification_details(id, code, expire_date, missing_attempts)
VALUES (1, 231231, '3024-05-26T21:43:19.229697', 5);
INSERT INTO verification_details(id, code, expire_date, missing_attempts)
VALUES (2, 421312, '3024-05-26T21:43:19.229697', 5);
INSERT INTO verification_details(id, code, expire_date, missing_attempts)
VALUES (3, 422312, '3024-05-26T21:43:19.229697', 5);
SELECT SETVAL('verification_details_id_seq', (SELECT MAX(id) FROM verification_details));

INSERT INTO users (id, email, password, verified, verification_details_id)
VALUES (1, 'test@mail.ru', '123456', true, 1);


INSERT INTO users (id, email, password, role_id, verified, verification_details_id)
VALUES (2, 'testadmin@gmail.com', 'admin', 2, true, 2);

INSERT INTO users (id, email, password, role_id, verified, verification_details_id)
VALUES (3, 'unverified@mail.ru', 'verification', 1, false, NULL);

SELECT SETVAL('users_id_seq', (SELECT MAX(id) FROM users));

INSERT INTO task(
                 id,
                 name,
                 description,
                 is_complete,
                 date_of_create,
                 deadline,
                 owner_id
) VALUES (
          1,
          'Task1',
          'Description1',
          false,
          '2024-05-26T21:43:19.229697',
          null,
          1
);

INSERT INTO task(
    id,
    name,
    description,
    is_complete,
    date_of_create,
    deadline,
    owner_id
) VALUES (
             2,
             'Task2',
             'Description2',
             true,
             '2024-05-26T21:43:19.229697',
             null,
             1
         );

INSERT INTO task(
    id,
    name,
    description,
    is_complete,
    date_of_create,
    deadline,
    owner_id
) VALUES (
             3,
             'Task3',
             'Description3',
             false,
             '2024-05-26T21:43:19.229697',
             '2024-06-10T12:00:00.229697',
             1
         );

SELECT SETVAL('task_id_seq', (SELECT MAX(id) FROM users));

INSERT INTO task(
    id,
    name,
    description,
    is_complete,
    date_of_create,
    deadline,
    owner_id
) VALUES (
             404,
             'Task3',
             'Description3',
             false,
             '2024-05-26T21:43:19.229697',
             '2024-06-10T12:00:00.229697',
             2
         );

