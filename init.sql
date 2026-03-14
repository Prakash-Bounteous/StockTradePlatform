-- Run this to create the database
CREATE DATABASE trading_platform;

-- Optional: create a dedicated user
-- CREATE USER trading_user WITH PASSWORD 'trading_pass';
-- GRANT ALL PRIVILEGES ON DATABASE trading_platform TO trading_user;

-- The tables will be auto-created by Hibernate (spring.jpa.hibernate.ddl-auto=update)

-- Seed an admin user (password: admin123)
-- Run AFTER starting the backend for the first time so tables are created
INSERT INTO users (username, email, password, role, balance, margin_multiplier, used_margin, created_at)
VALUES (
  'admin',
  'admin@tradepro.com',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6ZZ5',
  'ADMIN',
  100000.00,
  5.00,
  0.00,
  NOW()
) ON CONFLICT (email) DO NOTHING;

-- Seed sample stocks (run AFTER backend starts)
INSERT INTO stocks (symbol, company_name, price, total_shares, tradable) VALUES
  ('RELIANCE', 'Reliance Industries Ltd', 2850.75, 10000000, true),
  ('TCS', 'Tata Consultancy Services', 3920.50, 5000000, true),
  ('INFY', 'Infosys Ltd', 1780.25, 8000000, true),
  ('HDFCBANK', 'HDFC Bank Ltd', 1620.80, 12000000, true),
  ('ICICIBANK', 'ICICI Bank Ltd', 1180.60, 9000000, true),
  ('WIPRO', 'Wipro Ltd', 520.40, 7000000, true),
  ('BAJFINANCE', 'Bajaj Finance Ltd', 7240.90, 3000000, true),
  ('TATAMOTORS', 'Tata Motors Ltd', 980.35, 6000000, true)
ON CONFLICT (symbol) DO NOTHING;
