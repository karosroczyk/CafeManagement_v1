DROP USER if exists 'ordermanagement'@'%';
-- Create a new user
CREATE USER 'ordermanagement'@'%' IDENTIFIED BY 'ordermanagement';
-- Grant privileges to the new user (example: grant all privileges on a specific database)
GRANT ALL PRIVILEGES ON ordermanagement.* TO 'ordermanagement'@'%';
