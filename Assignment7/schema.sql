CREATE DATABASE IF NOT EXISTS campuseats_a07;
USE campuseats_a07;

CREATE TABLE IF NOT EXISTS dishes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  available BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed only an empty menu; importing again preserves existing records.
INSERT INTO dishes (name, price, available)
SELECT seed.name, seed.price, seed.available
FROM (
  SELECT 'Veg Sandwich' AS name, 60.00 AS price, TRUE AS available
  UNION ALL SELECT 'Masala Dosa', 80.00, TRUE
  UNION ALL SELECT 'Veg Biryani', 120.00, FALSE
  UNION ALL SELECT 'Lemonade', 30.00, TRUE
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM dishes);
