# Court Booking System - Entity Relationship Diagram

## Database Design (3 Independent Databases)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        COURT_BOOKING_USERS (Database 1)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                              users table                                     │
│  ┌──────────────────┬──────────────────┬──────────────────┬────────────────┐│
│  │      Column      │      Type        │    Constraints   │   Description  ││
│  ├──────────────────┼──────────────────┼──────────────────┼────────────────┤│
│  │       id         │    BIGINT        │   PK, AUTO_INC   │  Primary Key   ││
│  │    username      │   VARCHAR(50)    │   UNIQUE, NOT NULL│ User's name   ││
│  │     email        │  VARCHAR(100)    │   UNIQUE, NOT NULL│ User's email  ││
│  │    password      │  VARCHAR(255)    │   NOT NULL       │ BCrypt hashed  ││
│  │      role        │   VARCHAR(20)    │   NOT NULL, DEFAULT 'USER' │ ADMIN/USER ││
│  │    enabled       │    BOOLEAN       │   NOT NULL, DEFAULT TRUE   │ Account status ││
│  │   created_at     │    TIMESTAMP     │   DEFAULT CURRENT_TIMESTAMP│ Creation time ││
│  │   updated_at     │    TIMESTAMP     │   ON UPDATE CURRENT_TIMESTAMP│ Last update ││
│  └──────────────────┴──────────────────┴──────────────────┴────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        COURT_BOOKING_DB (Database 2)                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                              courts table                                   │
│  ┌──────────────────┬──────────────────┬──────────────────┬────────────────┐│
│  │      Column      │      Type        │    Constraints   │   Description  ││
│  ├──────────────────┼──────────────────┼──────────────────┼────────────────┤│
│  │       id         │    BIGINT        │   PK, AUTO_INC   │  Primary Key   ││
│  │      name        │  VARCHAR(100)    │   NOT NULL       │ Court name    ││
│  │   sport_type     │   VARCHAR(50)    │   NOT NULL       │ Tennis/Basket ││
│  │     location     │  VARCHAR(255)    │                  │ Physical loc  ││
│  │  price_per_hour  │  DECIMAL(10,2)   │                  │ Hourly rate   ││
│  │   description    │      TEXT        │                  │ Court details ││
│  │    available     │    BOOLEAN       │   DEFAULT TRUE   │ Is bookable   ││
│  │   created_at     │    TIMESTAMP     │   DEFAULT CURRENT_TIMESTAMP│        ││
│  │   updated_at     │    TIMESTAMP     │   ON UPDATE CURRENT_TIMESTAMP│      ││
│  └──────────────────┴──────────────────┴──────────────────┴────────────────┘│

│                              bookings table                                  │
│  ┌──────────────────┬──────────────────┬──────────────────┬────────────────┐│
│  │      Column      │      Type        │    Constraints   │   Description  ││
│  ├──────────────────┼──────────────────┼──────────────────┼────────────────┤│
│  │       id         │    BIGINT        │   PK, AUTO_INC   │  Primary Key   ││
│  │     user_id      │    BIGINT        │   NOT NULL, FK   │ → users.id     ││
│  │    court_id      │    BIGINT        │   NOT NULL, FK   │ → courts.id    ││
│  │   booking_date   │      DATE        │   NOT NULL       │ Booking date  ││
│  │    start_time    │      TIME        │   NOT NULL       │ Start hour    ││
│  │     end_time     │      TIME        │   NOT NULL       │ End hour      ││
│  │      status      │   VARCHAR(20)    │   NOT NULL       │ CONFIRMED/CANCELLED│
│  │      notes       │      TEXT        │                  │ User notes    ││
│  │    payment_id    │    BIGINT        │                  │ → payments.id  ││
│  │   created_at     │    TIMESTAMP     │   DEFAULT CURRENT_TIMESTAMP│        ││
│  │   updated_at     │    TIMESTAMP     │   ON UPDATE CURRENT_TIMESTAMP│      ││
│  └──────────────────┴──────────────────┴──────────────────┴────────────────┘│
│                                                                             │
│                         RELATIONSHIPS                                       │
│                                                                             │
│    users (1)───(N) bookings                                                 │
│    courts (1)───(N) bookings                                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                     COURT_BOOKING_PAYMENTS (Database 3)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                              payments table                                 │
│  ┌──────────────────┬──────────────────┬──────────────────┬────────────────┐│
│  │      Column      │      Type        │    Constraints   │   Description  ││
│  ├──────────────────┼──────────────────┼──────────────────┼────────────────┤│
│  │       id         │    BIGINT        │   PK, AUTO_INC   │  Primary Key   ││
│  │    booking_id    │    BIGINT        │   NOT NULL       │ Booking ref   ││
│  │     user_id      │    BIGINT        │   NOT NULL       │ User ref      ││
│  │     amount       │  DECIMAL(10,2)   │   NOT NULL       │ Payment amt   ││
│  │      status      │   VARCHAR(20)    │   NOT NULL       │ PENDING/COMPLETED/REFUNDED│
│  │  payment_method  │   VARCHAR(20)    │                  │ ONLINE/CASH   ││
│  │ transaction_id   │  VARCHAR(100)   │   UNIQUE         │ External ref  ││
│  │   payment_date   │    TIMESTAMP     │                  │ Payment time  ││
│  │   created_at     │    TIMESTAMP     │   DEFAULT CURRENT_TIMESTAMP│        ││
│  │   updated_at     │    TIMESTAMP     │   ON UPDATE CURRENT_TIMESTAMP│      ││
│  └──────────────────┴──────────────────┴──────────────────┴────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

## Entity Relationship Diagram (Visual)

```
┌─────────────────────┐          1:N          ┌─────────────────────┐
│        users        │◀─────────────────────│       bookings       │
├─────────────────────┤                      ├─────────────────────┤
│ - id (PK)           │                      │ - id (PK)           │
│ - username          │                      │ - user_id (FK)      │
│ - email             │                      │ - court_id (FK)     │
│ - password          │                      │ - booking_date      │
│ - role              │                      │ - start_time        │
│ - enabled           │                      │ - end_time          │
│ - created_at        │                      │ - status            │
│ - updated_at        │                      │ - payment_id        │
└─────────────────────┘                      │ - notes             │
                                             └──────────┬──────────┘
                                                        │
                                                        │ N:1
                                                        ▼
                                             ┌─────────────────────┐
                                             │       courts        │
                                             ├─────────────────────┤
                                             │ - id (PK)           │
                                             │ - name              │
                                             │ - sport_type        │
                                             │ - location          │
                                             │ - price_per_hour    │
                                             │ - available         │
                                             │ - description       │
                                             └─────────────────────┘
                                             ┌─────────────────────┐
                                             │    payments         │
                                             ├─────────────────────┤
                                             │ - id (PK)           │
                                             │ - booking_id        │
                                             │ - user_id           │
                                             │ - amount            │
                                             │ - status            │
                                             │ - payment_method    │
                                             │ - transaction_id    │
                                             │ - payment_date      │
                                             └─────────────────────┘
```

## Database Isolation

```
┌────────────────────────────────────────────────────────────────────┐
│                    SERVICE-DATABASE MAPPING                        │
├──────────────────────┬───────────────────────┬─────────────────────┤
│     Service          │     Database          │     Tables           │
├──────────────────────┼───────────────────────┼─────────────────────┤
│   user-service       │ court_booking_users  │     users            │
│   booking-service    │ court_booking_db     │ courts, bookings     │
│   payment-service   │ court_booking_payments│    payments          │
│   eureka-server     │       NONE            │      NONE            │
│   api-gateway       │       NONE            │      NONE            │
└──────────────────────┴───────────────────────┴─────────────────────┘

IMPORTANT: Each service has its own database. No sharing between services!
```

## SQL Schema Scripts

### User Service Schema (court_booking_users)

```sql
CREATE DATABASE court_booking_users;
USE court_booking_users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);
```

### Booking Service Schema (court_booking_db)

```sql
CREATE DATABASE court_booking_db;
USE court_booking_db;

CREATE TABLE courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sport_type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    price_per_hour DECIMAL(10,2),
    description TEXT,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sport_type (sport_type),
    INDEX idx_available (available)
);

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    notes TEXT,
    payment_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (court_id) REFERENCES courts(id),
    INDEX idx_user_id (user_id),
    INDEX idx_booking_date (booking_date),
    INDEX idx_court_date_time (court_id, booking_date, start_time, end_time)
);
```

### Payment Service Schema (court_booking_payments)

```sql
CREATE DATABASE court_booking_payments;
USE court_booking_payments;

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20),
    transaction_id VARCHAR(100) UNIQUE,
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_booking_id (booking_id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_id (transaction_id)
);
```