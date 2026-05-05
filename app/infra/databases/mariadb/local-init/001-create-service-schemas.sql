CREATE DATABASE IF NOT EXISTS user_service_db;
CREATE DATABASE IF NOT EXISTS product_service_db;
CREATE DATABASE IF NOT EXISTS order_service_db;
CREATE DATABASE IF NOT EXISTS payment_service_db;

CREATE USER IF NOT EXISTS 'cloud'@'%' IDENTIFIED BY 'Skala25a!23$';

GRANT ALL PRIVILEGES ON user_service_db.* TO 'cloud'@'%';
GRANT ALL PRIVILEGES ON product_service_db.* TO 'cloud'@'%';
GRANT ALL PRIVILEGES ON order_service_db.* TO 'cloud'@'%';
GRANT ALL PRIVILEGES ON payment_service_db.* TO 'cloud'@'%';
GRANT RELOAD, REPLICATION SLAVE, REPLICATION CLIENT, LOCK TABLES ON *.* TO 'cloud'@'%';

FLUSH PRIVILEGES;
