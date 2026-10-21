-- 1. create the database
CREATE DATABASE IF NOT EXISTS gqldemo;
USE gqldemo;

-- 2. create the two tables (authors 1 --- many books)
CREATE TABLE IF NOT EXISTS authors (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  country VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  year INT,
  author_id INT NOT NULL,
  FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- 3. put some sample rows in
INSERT INTO authors (id, name, country) VALUES
  (1, 'Asha Patel', 'India'),
  (2, 'Ravi Kumar', 'India')
ON DUPLICATE KEY UPDATE name=VALUES(name), country=VALUES(country);

INSERT INTO books (id, title, year, author_id) VALUES
  (1, 'Databases 101', 2020, 1),
  (2, 'SQL in Practice', 2022, 1),
  (3, 'Web Services Basics', 2023, 2)
ON DUPLICATE KEY UPDATE title=VALUES(title), year=VALUES(year), author_id=VALUES(author_id);
