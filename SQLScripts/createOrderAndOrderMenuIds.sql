-- Create the database
 CREATE DATABASE IF NOT EXISTS ordermanagement;
 -- DROP TABLE Order_MenuItem_Id;
 -- DROP TABLE Orders;

USE ordermanagement;

CREATE TABLE IF NOT EXISTS Orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    total_price DECIMAL(7, 2) NOT NULL,
    customer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_menu_item_id (
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT,
    PRIMARY KEY (order_id, menu_item_id),
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);

-- Insert sample categories
INSERT INTO Orders (status, total_price, customer_id) VALUES
('PREPARING', 10.00, 1),
('COMPLETE', 25.50, 2),
('TAKEN', 95.50, 3);

-- Insert sample menu items
INSERT INTO order_menu_item_id (order_id, menu_item_id, quantity) VALUES
(1, 2, 1),
(1, 4, 1),
(2, 3, 1),
(3, 1, 1),
(3, 3, 1)

