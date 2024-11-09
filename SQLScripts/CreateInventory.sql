-- Create the database
 CREATE DATABASE IF NOT EXISTS inventorymanagement;
DROP TABLE Inventory;
USE inventorymanagement;

CREATE TABLE Inventory (
    id INT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id INT NOT NULL UNIQUE,
    stock_level INT NOT NULL,
    is_available BOOLEAN DEFAULT FALSE
);

-- Insert sample categories
INSERT INTO Inventory (menu_item_id, stock_level, is_available) VALUES
(1, 10, TRUE),
(2, 8, TRUE);
