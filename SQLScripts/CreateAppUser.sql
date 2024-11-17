-- Create the database
 CREATE DATABASE IF NOT EXISTS usermanagement;
 DROP TABLE Users;
USE usermanagement;


CREATE TABLE Users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    second_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

-- Insert sample categories
INSERT INTO Users (first_name, second_name, email, password, role) VALUES
('John', 'Wolf', 'johnwolf@gmail.com', 'abc', 'CLIENT'),
('Mery', 'Wolf', 'merywolf@gmail.com', 'abc', 'EMPLOYEE'),
('Susan', 'Wolf', 'susanwolf@gmail.com', 'abc', 'MANAGER');
