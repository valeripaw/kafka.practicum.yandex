ALTER USER kafka WITH SUPERUSER;

CREATE TABLE logo (
    id SERIAL PRIMARY KEY,
    name TEXT
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name TEXT,
    email TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    product_name TEXT,
    quantity INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Тестовые данные
INSERT INTO logo (name) VALUES
('logo1'),
('logo2');

-- Добавление пользователей
INSERT INTO users (name, email) VALUES ('John Doe', 'john@example.com');
INSERT INTO users (name, email) VALUES ('Jane Smith', 'jane@example.com');
INSERT INTO users (name, email) VALUES ('Alice Johnson', 'alice@example.com');
INSERT INTO users (name, email) VALUES ('Bob Brown', 'bob@example.com');

-- Добавление заказов
INSERT INTO orders (user_id, product_name, quantity) VALUES (1, 'Product A', 2);
INSERT INTO orders (user_id, product_name, quantity) VALUES (1, 'Product B', 1);
INSERT INTO orders (user_id, product_name, quantity) VALUES (2, 'Product C', 5);
INSERT INTO orders (user_id, product_name, quantity) VALUES (3, 'Product D', 3);
INSERT INTO orders (user_id, product_name, quantity) VALUES (4, 'Product E', 4);

ALTER TABLE users OWNER TO kafka;
ALTER TABLE orders OWNER TO kafka;

