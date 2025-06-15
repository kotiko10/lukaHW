INSERT INTO users (login, password, email, first_name, last_name, birthday, role_id)
SELECT 'admin', 'admin', 'admin@example.com', 'Adam', 'Sandler', '1990-01-01', 1
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'admin');

INSERT INTO users (login, password, email, first_name, last_name, birthday, role_id)
SELECT 'user', 'user', 'user@example.com', 'Regular', 'User', '1995-01-01', 2
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'user');

