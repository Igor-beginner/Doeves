INSERT INTO users (id, email, password)
VALUES (1, 'test@mail.ru', '123456');


INSERT INTO users (id, email, password, role_id)
VALUES (2, 'testadmin@gmail.com', 'admin', 2);

SELECT SETVAL('users_id_seq', (SELECT MAX(id) FROM users));

INSERT INTO task(
                 id,
                 name,
                 description,
                 complete,
                 date_of_create,
                 deadline,
                 owner_id
) VALUES (
          1,
          'Task1',
          'Description1',
          false,
          now(),
          null,
          1
);

INSERT INTO task(
    id,
    name,
    description,
    complete,
    date_of_create,
    deadline,
    owner_id
) VALUES (
             2,
             'Task2',
             'Description2',
             true,
             now(),
             null,
             1
         );

INSERT INTO task(
    id,
    name,
    description,
    complete,
    date_of_create,
    deadline,
    owner_id
) VALUES (
             3,
             'Task3',
             'Description3',
             false,
             now(),
             null,
             1
         );

SELECT SETVAL('task_id_seq', (SELECT MAX(id) FROM users));