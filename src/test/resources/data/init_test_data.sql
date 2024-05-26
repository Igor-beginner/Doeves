DELETE FROM task;
DELETE FROM users;

INSERT INTO users (id, email, password)
VALUES (1, 'test@mail.ru', '123456');


INSERT INTO users (id, email, password, role_id)
VALUES (2, 'testadmin@gmail.com', 'admin', 2);

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

SELECT SETVAL('task_id_seq', (SELECT MAX(id) FROM users) + 1);

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

