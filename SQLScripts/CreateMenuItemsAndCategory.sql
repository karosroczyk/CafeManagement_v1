-- Create the database
 CREATE DATABASE IF NOT EXISTS menumanagement;
--   drop table MenuItems;
--  drop table Categories;
USE menumanagement;

-- Table to store categories of menu items (e.g., Beverages, Pastries, etc.)
CREATE TABLE IF NOT EXISTS Categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Table to store menu items
CREATE TABLE IF NOT EXISTS MenuItems (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(7, 2) NOT NULL,
    category_id INT,
    image BLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

-- Insert sample categories
INSERT INTO Categories (category_name, description) VALUES
('Beverages', 'Drinks such as coffee, tea, juices'),
('Pastries', 'Baked goods like cakes, croissants, and muffins');

-- Insert sample menu items
INSERT INTO MenuItems (name, description, price, category_id, image) VALUES
('Espresso', 'Strong black coffee', 2.50, 1, NULL),
('Cappuccino', 'Espresso with steamed milk', 3.00, 1, NULL),
('Croissant', 'Buttery, flaky pastry', 2.00, 2, NULL),
('Muffin', 'Soft baked muffin, various flavors', 1.50, 2, NULL);
