# TradePro — Full Stack Stock Trading Platform

A complete stock trading simulation platform built with **Spring Boot** (backend) and **React + Vite** (frontend).

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Security, JPA/Hibernate |
| Database | PostgreSQL |
| Auth | JWT (HS256) |
| Build | Maven |
| Frontend | React 18, Vite, React Router v6 |
| Charts | Recharts |
| Icons | Lucide React |
| HTTP | Axios |

---

## 📁 Project Structure

```
trading-platform/
├── backend/                          # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/trading/platform/
│       ├── TradingPlatformApplication.java
│       ├── auth/                     # JWT auth, login, register
│       ├── admin/                    # Admin stock management
│       ├── analytics/                # P&L + Leaderboard
│       ├── common/                   # Enums, utilities
│       ├── config/                   # Security, Swagger, Seeder
│       ├── engine/                   # Order book + Matching engine
│       ├── margin/                   # Margin accounts
│       ├── market/                   # Price simulation, circuit breaker
│       ├── notification/             # Trade notifications
│       ├── order/                    # Order placement & history
│       ├── portfolio/                # User portfolio
│       ├── stock/                    # Stock CRUD
│       ├── trade/                    # Trade execution
│       ├── user/                     # User profiles
│       └── watchlist/                # User watchlist
│
├── frontend/                         # React + Vite application
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx                   # Router + Auth guards
│       ├── index.css                 # Global dark theme
│       ├── context/
│       │   ├── AuthContext.jsx       # Auth state
│       │   └── ToastContext.jsx      # Toast notifications
│       ├── services/
│       │   └── api.js                # All API calls (axios)
│       ├── components/
│       │   ├── Layout.jsx            # Sidebar navigation
│       │   └── Layout.css
│       └── pages/
│           ├── Login.jsx / Register.jsx
│           ├── Dashboard.jsx         # Overview + live market
│           ├── Trade.jsx             # Buy/sell with charts
│           ├── Portfolio.jsx         # Holdings + P&L + history
│           ├── Watchlist.jsx         # Stock watchlist
│           ├── Leaderboard.jsx       # Top traders
│           ├── Notifications.jsx     # Trade notifications
│           └── Admin.jsx             # Stock management
│
├── init.sql                          # DB seed script
└── README.md
```

---

## ⚡ Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+
- npm 9+

---

### 1. Database Setup

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE trading_platform;
\q
```

---

### 2. Backend Setup

```bash
cd trading-platform/backend

# Configure database credentials in:
# src/main/resources/application.properties
# Change these if needed:
#   spring.datasource.username=postgres
#   spring.datasource.password=postgres

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**

On first start:
- Hibernate auto-creates all tables
- DataSeeder creates: **admin / admin123**
- Swagger UI: http://localhost:8080/swagger-ui/index.html

---

### 3. Seed Sample Stocks

After the backend starts, run in psql:

```sql
\c trading_platform

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
```

Or use the **Admin Panel** in the UI after logging in as admin.

---

### 4. Frontend Setup

```bash
cd trading-platform/frontend

npm install
npm run dev
```

Frontend starts on **http://localhost:5173**

---

## 🔑 Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| User | Register via UI | — |

---

## 🚀 Features

### Trading Engine
- **Order Book** — In-memory priority queue per stock (price-time priority)
- **Matching Engine** — Automatically matches BUY/SELL orders
- **Order Types** — Market orders and Limit orders
- **Price Simulation** — Prices update every 5 seconds via scheduler

### User Features
- Register/Login with JWT auth
- ₹1,00,000 virtual starting balance
- Buy & Sell stocks (MARKET or LIMIT orders)
- Live price charts (Recharts)
- Portfolio with P&L calculation
- Watchlist management
- Trade history
- Real-time notifications
- Leaderboard (top 10 by total value)

### Admin Features
- Create new stocks with initial price
- Enable/Disable trading for any stock
- Delete stocks
- View all stocks with live prices

### Backend Features
- JWT stateless auth (Spring Security)
- Circuit breaker (auto-halts stocks at ₹0)
- Margin account system
- Price history tracking
- Swagger API docs at `/swagger-ui/index.html`
- CORS configured for localhost:5173

---

## 🛠️ API Endpoints

### Auth
- `POST /auth/register` — Register new user
- `POST /auth/login` — Login, returns JWT token

### Stocks
- `GET /stocks` — All stocks
- `GET /stocks/{symbol}` — Single stock

### Orders
- `POST /orders/place` — Place order `{ symbol, side, type, quantity, price? }`
- `GET /orders/my` — My order history
- `GET /orders/book/{symbol}/buy` — Buy order book
- `GET /orders/book/{symbol}/sell` — Sell order book

### Portfolio & Analytics
- `GET /portfolio` — My holdings
- `GET /analytics/pnl` — P&L per stock + total
- `GET /leaderboard` — Top 10 traders
- `GET /trades/my` — My trade history

### Market
- `GET /market/status` — OPEN or CLOSED
- `GET /prices/{symbol}` — Price history

### Watchlist
- `GET /watchlist` — My watchlist
- `POST /watchlist/add/{symbol}` — Add to watchlist
- `DELETE /watchlist/remove/{symbol}` — Remove

### Notifications
- `GET /notifications` — My notifications
- `POST /notifications/read-all` — Mark all read

### Admin (requires ADMIN role)
- `POST /admin/stocks/create` — Create stock
- `DELETE /admin/stocks/delete/{id}` — Delete stock
- `POST /admin/stocks/{id}/enable` — Enable trading
- `POST /admin/stocks/{id}/disable` — Disable trading

---

## 🎨 Design

- Dark trading terminal aesthetic
- Fonts: **Syne** (display) + **Space Mono** (numbers)
- Color palette: deep blacks, teal accent (`#00d4aa`), red/green for P&L
- Responsive sidebar layout
- Live auto-refreshing data (5–10s intervals)
- Toast notifications for all actions

---

## ⚠️ Notes

- Prices simulate every 5 seconds; trades appear instantly in portfolio
- LIMIT orders stay in the order book until matched
- Market orders execute at current stock price
- All balances and data reset only when DB is cleared
- For production: change JWT secret, add HTTPS, configure proper CORS origins
