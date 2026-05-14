# Court Booking System - Complete File Documentation
## Line-by-Line Explanation for Beginners

---

## Table of Contents
1. [Project Structure Overview](#1-project-structure-overview)
2. [What is Each Service?](#2-what-is-each-service)
3. [How Everything Connects - The Big Picture](#3-how-everything-connects---the-big-picture)
4. [File-by-File Explanation](#4-file-by-file-explanation)
5. [Annotations Explained](#5-annotations-explained)
6. [Commands for Testing](#6-commands-for-testing)
7. [Security & Authorization Explained](#7-security--authorization-explained)

---

## 1. Project Structure Overview

```
CourtBook/
├── eureka-server/        # Service registry - helps services find each other
├── api-gateway/          # Single entry point - validates JWT and routes requests
├── user-service/         # Handles user registration, login, user management
├── booking-service/      # Handles courts and bookings
├── payment-service/      # Handles payment processing
├── common/               # Shared code between services (exceptions, DTOs)
├── sql/                  # Database schema files
└── Report/              # Documentation and diagrams
```

### What Each Folder Does (Simple Explanation)

| Folder | What it Does | Why We Need It |
|--------|--------------|----------------|
| eureka-server | Phone book for services | Services find each other dynamically |
| api-gateway | Front door/bouncer | Validates JWT, routes requests to correct service |
| user-service | User manager | Handles register, login, user data |
| booking-service | Booking manager | Handles courts, bookings, time slots |
| payment-service | Payment processor | Handles money transactions |
| common | Shared utilities | Avoids repeating code across services |

---

## 2. What is Each Service?

### 2.1 Eureka Server (Port 8761)

**What it does:** It's a phone book. When booking-service wants to talk to user-service, it asks Eureka: "Where's user-service?" and Eureka says: "At localhost:8081".

**Why we need it:** Imagine if booking-service had a hardcoded address "http://localhost:8081". If user-service moves to a different computer, it breaks! With Eureka, services just say their name, and Eureka figures out where they are.

```
EUREKA SERVER (Phone Book)
┌─────────────────────────────────────┐
│  SERVICE REGISTRY                   │
│                                     │
│  user-service    → localhost:8081  │
│  booking-service  → localhost:8082  │
│  payment-service  → localhost:8083  │
│  api-gateway      → localhost:8080  │
└─────────────────────────────────────┘
```

### 2.2 API Gateway (Port 8080)

**What it does:** The main entrance. Every request from clients (Postman, Web Browser) goes through here first.

**Responsibilities:**
1. Checks if request has valid JWT token
2. Routes request to the correct service
3. Returns response back to client

```
CLIENT ──▶ API GATEWAY ──▶ SERVICE ──▶ DATABASE
           (The Bouncer) (The Worker)
```

### 2.3 User Service (Port 8081)

**What it does:** Manages everything about users.

**Endpoints:**
- `POST /auth/register` - Create new account
- `POST /auth/login` - Login and get JWT token
- `GET /users/{id}` - Get user details (Admin only)
- `GET /users` - Get all users (Admin only)

**Database:** `court_booking_users` (stores user accounts)

### 2.4 Booking Service (Port 8082)

**What it does:** Manages courts and bookings.

**Endpoints:**
- `POST /courts` - Add new court (Admin)
- `GET /courts` - Get all courts
- `POST /bookings` - Create booking
- `DELETE /bookings/{id}` - Cancel booking

**Database:** `court_booking_db` (stores courts and bookings)

### 2.5 Payment Service (Port 8083)

**What it does:** Handles money transactions.

**Endpoints:**
- `POST /api/payments` - Create payment
- `POST /api/payments/{id}/process` - Process payment
- `POST /api/payments/{id}/refund` - Refund payment
- `GET /api/payments/{id}` - Get payment details

**Database:** `court_booking_payments` (stores payment records)

---

## 3. How Everything Connects - The Big Picture

### 3.1 Complete Request Flow

```
USER REGISTRATION FLOW:
═══════════════════════

1. User (Postman) sends:
   POST http://localhost:8080/auth/register
   Body: {"username": "john", "email": "john@test.com", "password": "pass123"}

2. API Gateway receives request
   - Sees it's /auth/register (public path)
   - Does NOT check JWT (because it's public)
   - Routes directly to user-service

3. User Service processes:
   - AuthController receives the request
   - Calls UserService.register()
   - UserService saves user to MySQL database
   - UserService generates JWT token
   - Returns response with token

4. User gets:
   {"token": "eyJ...", "userId": 1, "username": "john", ...}
```

```
BOOKING CREATION FLOW:
═══════════════════════

1. User sends:
   POST http://localhost:8080/bookings
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   Body: {"userId": 1, "courtId": 1, "bookingDate": "2026-05-20", ...}

2. API Gateway:
   - Extracts Authorization header
   - Parses JWT token
   - Validates signature with secret key
   - Checks if token is expired
   - Extracts: userId=1, role=USER
   - Adds headers: X-User-Id=1, X-User-Role=USER
   - Routes to booking-service (lb:// means load balanced)

3. Booking Service:
   - Controller receives request
   - Service calls UserServiceClient to validate user (WebClient call)
   - Service checks court availability
   - Service checks for time conflicts
   - Service saves booking to database
   - Service calls PaymentServiceClient to create payment (WebClient call)
   - Returns booking details with payment info

4. User gets:
   {"id": 1, "status": "CONFIRMED", "paymentId": 1, ...}
```

---

## 4. File-by-File Explanation

### 4.1 USER-SERVICE FILES

#### 4.1.1 UserServiceApplication.java (Entry Point)

```
File: user-service/src/main/java/com/courtbooking/userservice/UserServiceApplication.java

┌─────────────────────────────────────────────────────────────────────────────┐
│ 1  │ package com.courtbooking.userservice;                                   │
│    │   ↑ Package name - shows where this file belongs in folder structure   │
│    │   Like an address: com → courtbooking → userservice                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2  │                                                                             │
│ 3  │ import org.springframework.boot.SpringApplication;                        │
│    │   ↑ Import Spring Boot startup class                                     │
│    │   We need this to run our Spring application                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4  │ import org.springframework.boot.autoconfigure.SpringBootApplication;      │
│    │   ↑ This annotation tells Spring: "This is the main application class" │
│    │   Spring Boot auto-configuration kicks in automatically                │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5  │                                                                             │
│ 6  │ @SpringBootApplication                                                   │
│    │   ↑ THIS IS THE STARTING POINT!                                          │
│    │   When you run this program, Spring looks for this annotation           │
│    │   It tells Spring to:                                                    │
│    │   - Scan this package for components                                    │
│    │   - Configure everything automatically                                  │
│    │   - Start the embedded server                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ 7  │ public class UserServiceApplication {                                   │
│    │   ↑ Class name - must match filename                                    │
│    │   This is a Java class (blueprint for objects)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 8  │     public static void main(String[] args) {                             │
│    │       ↑ MAIN METHOD - program execution starts HERE                     │
│    │       Every Java program needs this method                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 9  │         SpringApplication.run(UserServiceApplication.class, args);    │
│    │              ↑ Tells Spring to start this application                   │
│    │              ↑ Pass the class itself so Spring knows where to scan     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 10 │     }                                                                     │
│ 11 │ }                                                                         │
└─────────────────────────────────────────────────────────────────────────────┘

SUMMARY: This is the entry point. When you run this, Spring Boot starts up,
         scans for components, and makes your application ready to receive requests.
```

#### 4.1.2 User.java (Entity/Model)

```
File: user-service/src/main/java/com/courtbooking/userservice/entity/User.java

┌─────────────────────────────────────────────────────────────────────────────┐
│ 1  │ package com.courtbooking.userservice.entity;                             │
│    │   ↑ Package for entity classes (database representations)             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2  │                                                                             │
│ 3  │ import jakarta.persistence.*;                                           │
│    │   ↑ Import JPA (Java Persistence API) annotations                       │
│    │   JPA is how we talk to databases in Java                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4  │ import lombok.Data;                                                     │
│    │   ↑ Import Lombok @Data annotation                                      │
│    │   Lombok auto-generates getters, setters, equals, hashCode, toString │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5  │ import java.time.LocalDateTime;                                         │
│    │   ↑ Import date/time class                                             │
│    │   LocalDateTime = date + time (like 2026-05-20 10:30:00)              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 6  │                                                                             │
│ 7  │ @Entity                                                                 │
│    │   ↑ MARKS THIS CLASS AS A DATABASE TABLE                               │
│    │   Spring/JPA will create a 'users' table in MySQL                       │
│    │   Each field becomes a column in the table                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 8  │ @Table(name = "users")                                                 │
│    │   ↑ SPECIFIES TABLE NAME IN DATABASE                                    │
│    │   Without this, JPA would use class name 'User' as table name          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 9  │ @Data                                                                  │
│    │   ↑ LOMBOK: Auto-generates all these methods:                          │
│    │   - getId(), setId()                                                    │
│    │   - getUsername(), setUsername()                                        │
│    │   - getEmail(), setEmail()                                              │
│    │   - getPassword(), setPassword()                                        │
│    │   - getRole(), setRole()                                                │
│    │   - getEnabled(), setEnabled()                                          │
│    │   - getCreatedAt(), setCreatedAt()                                      │
│    │   - equals(), hashCode()                                                │
│    │   - toString()                                                         │
│    │   Saves us from writing boilerplate code!                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 10 │ public class User {                                                     │
│    │   ↑ The class that represents a user in our system                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 11 │                                                                             │
│ 12 │     @Id                                                                 │
│    │       ↑ MARKS THIS FIELD AS PRIMARY KEY                                  │
│    │       Primary key = unique identifier for each row                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 13 │     @GeneratedValue(strategy = GenerationType.IDENTITY)                │
│    │       ↑ DATABASE WILL AUTO-GENERATE THIS VALUE                          │
│    │       IDENTITY = MySQL auto_increment                                   │
│    │       Every time we save a new user, ID generates automatically        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 14 │     private Long id;                                                    │
│    │       ↑ Long = whole number (-9 quintillion to +9 quintillion)          │
│    │       id = unique number for each user                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ 15 │                                                                             │
│ 16 │     @Column(nullable = false, unique = true)                            │
│    │       ↑ COLUMN SETTINGS IN DATABASE                                      │
│    │       - nullable = false = CANNOT BE EMPTY (REQUIRED)                   │
│    │       - unique = true = NO TWO USERS CAN HAVE SAME USERNAME            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 17 │     private String username;                                            │
│    │       ↑ String = text (like "john123")                                  │
│    │       Stores user's login name                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 18 │     @Column(nullable = false, unique = true)                           │
│    │       ↑ Email must be unique and required                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ 19 │     private String email;                                               │
│    │       ↑ User's email address                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ 20 │     @Column(nullable = false)                                          │
│    │       ↑ Password is required, but not unique (many users can have      │
│    │         the same password but stored as hash)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 21 │     private String password;                                            │
│    │       ↑ BCrypt encrypted password (NEVER store plain text!)            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 22 │     @Enumerated(EnumType.STRING)                                        │
│    │       ↑ STORES ENUM AS STRING, NOT NUMBER                               │
│    │       Enum = fixed set of values (ADMIN, USER)                         │
│    │       STRING stores "ADMIN" not "0"                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 23 │     private Role role;                                                  │
│    │       ↑ Role = enum with values ADMIN or USER                           │
│    │       Controls what user can do                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 24 │     private Boolean enabled = true;                                     │
│    │       ↑ Can user log in? Default = true                                 │
│    │       Set to false to disable account                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 25 │     private LocalDateTime createdAt;                                    │
│    │       ↑ When was this user created? Auto-set by @CreationTimestamp    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 26 │     private LocalDateTime updatedAt;                                    │
│    │       ↑ When was this user last updated? Auto-updated                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ 27 │ }                                                                       │
└─────────────────────────────────────────────────────────────────────────────┘

DATABASE TABLE CREATED:
┌─────────────────┬────────────────┬─────────────┐
│ Column          │ Type          │ Constraints │
├─────────────────┼────────────────┼─────────────┤
│ id              │ BIGINT        │ PK, AUTO    │
│ username        │ VARCHAR(255)  │ NOT NULL    │
│ email           │ VARCHAR(255)  │ NOT NULL    │
│ password        │ VARCHAR(255)  │ NOT NULL    │
│ role            │ VARCHAR(255)  │ NOT NULL    │
│ enabled         │ BOOLEAN       │ DEFAULT TRUE│
│ created_at      │ TIMESTAMP     │ AUTO        │
│ updated_at      │ TIMESTAMP     │ AUTO        │
└─────────────────┴────────────────┴─────────────┘
```

#### 4.1.3 UserRepository.java (Data Access Layer)

```
File: user-service/src/main/java/com/courtbooking/userservice/repository/UserRepository.java

┌─────────────────────────────────────────────────────────────────────────────┐
│ 1  │ package com.courtbooking.userservice.repository;                        │
│    │   ↑ Package for repository classes (database queries)                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2  │                                                                             │
│ 3  │ import com.courtbooking.userservice.entity.User;                        │
│    │   ↑ Import User entity so repository knows what to manage              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4  │ import org.springframework.data.jpa.repository.JpaRepository;           │
│    │   ↑ IMPORT SPRING DATA JPA REPOSITORY                                   │
│    │   JpaRepository = pre-built interface with common database operations   │
│    │   We extend (inherit from) it to get all basic CRUD methods            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5  │ import org.springframework.stereotype.Repository;                      │
│    │   ↑ Marks this as a Spring component                                   │
│    │   Spring will auto-detect and manage this class                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 6  │                                                                             │
│ 7  │ @Repository                                                              │
│    │   ↑ SPRING COMPONENT ANNOTATION                                          │
│    │   Tells Spring: "Create an object of this class to handle database"    │
│    │   Part of the Data Access Layer (DAL)                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ 8  │ public interface UserRepository extends JpaRepository<User, Long> {     │
│    │       ↑ WE DON'T WRITE IMPLEMENTATION - SPRING DATA JPA DOES IT         │
│    │       JpaRepository<User, Long>:                                        │
│    │       - User = entity type we're working with                          │
│    │       - Long = type of the primary key (id)                             │
│    │                                                                             │
│    │       AUTOMATICALLY AVAILABLE METHODS (from JpaRepository):            │
│    │       - save(user) → INSERT or UPDATE                                  │
│    │       - findById(id) → SELECT WHERE id=?                              │
│    │       - findAll() → SELECT *                                           │
│    │       - deleteById(id) → DELETE                                        │
│    │       - existsById(id) → check if exists                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 9  │     boolean existsByUsername(String username);                         │
│    │       ↑ CUSTOM METHOD - Spring generates query automatically           │
│    │       Generates: SELECT COUNT(*) FROM users WHERE username = ?        │
│    │       Used to check if username is already taken                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 10 │     boolean existsByEmail(String email);                               │
│    │       ↑ CUSTOM METHOD                                                  │
│    │       Generates: SELECT COUNT(*) FROM users WHERE email = ?            │
│    │       Used to check if email is already registered                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 11 │     Optional<User> findByUsername(String username);                    │
│    │       ↑ CUSTOM METHOD                                                  │
│    │       Generates: SELECT * FROM users WHERE username = ?               │
│    │       Returns Optional<User> (may or may not find user)               │
├─────────────────────────────────────────────────────────────────────────────┤
│ 12 │     Optional<User> findByEmail(String email);                          │
│    │       ↑ CUSTOM METHOD                                                  │
│    │       Generates: SELECT * FROM users WHERE email = ?                  │
│    │       Allows login with email instead of username                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 13 │ }                                                                       │
└─────────────────────────────────────────────────────────────────────────────┘

HOW SPRING DATA JPA WORKS:
═════════════════════════

1. You write interface extending JpaRepository
2. Spring sees your method name (findByUsername)
3. Spring parses method: "find" + "By" + "Username"
4. Spring generates SQL query: SELECT * FROM users WHERE username = ?
5. Spring creates implementation at runtime

No need to write SQL! Method names become queries automatically.
```

#### 4.1.4 UserService.java (Business Logic)

```
File: user-service/src/main/java/com/courtbooking/userservice/service/UserService.java

┌─────────────────────────────────────────────────────────────────────────────┐
│ 1  │ package com.courtbooking.userservice.service;                           │
│    │   ↑ Package for service classes (business logic)                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2  │                                                                             │
│ 3  │ import lombok.RequiredArgsConstructor;                                   │
│    │   ↑ LOMBOK: Generates constructor with all final fields                  │
│    │   @RequiredArgsConstructor + @Service = constructor auto-generated       │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4  │ import lombok.extern.slf4j.Slf4j;                                         │
│    │   ↑ LOMBOK: Generates logger for this class                             │
│    │   log.info(), log.debug(), log.error() become available                 │
│    │   slf4j = Simple Logging Facade for Java                                │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5  │ import org.springframework.security.crypto.password.PasswordEncoder;    │
│    │   ↑ Import BCrypt password encoder                                      │
│    │   Used to encrypt/decrypt passwords                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 6  │ import org.springframework.stereotype.Service;                          │
│    │   ↑ SPRING COMPONENT ANNOTATION                                          │
│    │   Marks this class as a service component                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ 7  │                                                                             │
│ 8  │ @Service                                                                 │
│    │   ↑ MARKS THIS CLASS AS A SPRING SERVICE                                │
│    │   Spring creates a single instance (singleton) of this class            │
│    │   We can @Autowired this class in other places                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ 9  │ @RequiredArgsConstructor                                                │
│    │   ↑ LOMBOK: Creates constructor with all 'final' fields                  │
│    │   Creates: public UserService(UserRepository repo, PasswordEncoder enc,  │
│    │                               JwtService jwt) { ... }                  │
│    │   Spring injects the dependencies automatically                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ 10 │ @Slf4j                                                                   │
│    │   ↑ LOMBOK: Creates logger 'log' for this class                          │
│    │   Can use: log.info(), log.debug(), log.warn(), log.error()             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 11 │ public class UserService {                                              │
│    │   ↑ Service class - contains business logic                             │
│    │   Controller calls Service, Service calls Repository                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ 12 │                                                                             │
│ 13 │     private final UserRepository userRepository;                        │
│    │       ↑ FINAL = cannot be changed after creation                       │
│    │       Repository for database operations                                │
├─────────────────────────────────────────────────────────────────────────────┤
│ 14 │     private final PasswordEncoder passwordEncoder;                      │
│    │       ↑ BCrypt encoder to encrypt passwords                             │
│    │       Encodes: turns "password123" into "$2a$10$..."                   │
│    │       Matches: compares plain password with hash                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ 15 │     private final JwtService jwtService;                                │
│    │       ↑ Service for JWT token generation/validation                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 16 │                                                                             │
│ 17 │     public AuthResponse register(RegisterRequest request) {              │
│    │       ↑ PUBLIC METHOD - called by Controller                            │
│    │       Takes RegisterRequest (DTO with username, email, password)        │
│    │       Returns AuthResponse (DTO with token, user info)                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ 18 │         log.info("Processing registration for: {}", request.getEmail());│
│    │           ↑ LOG TO FILE: Records what we're doing                       │
│    │           {} is placeholder for actual value                            │
│    │           Output: "Processing registration for: john@example.com"      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 19 │         if (userRepository.existsByUsername(request.getUsername())) {  │
│    │           ↑ CHECK IF USERNAME ALREADY EXISTS                           │
│    │           calls userRepository.existsByUsername()                      │
│    │           returns true if username found in database                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ 20 │             throw new BadRequestException("Username already exists");  │
│    │               ↑ IF USERNAME EXISTS, THROW ERROR                          │
│    │               BadRequestException = custom exception (400 error)       │
│    │               Controller will catch this and return error message      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 21 │         }                                                               │
│    │                                                                             │
│ 22 │         if (userRepository.existsByEmail(request.getEmail())) {        │
│    │           ↑ CHECK IF EMAIL ALREADY EXISTS                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 23 │             throw new BadRequestException("Email already exists");     │
│    │               ↑ THROW ERROR IF EMAIL EXISTS                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 24 │         }                                                               │
│    │                                                                             │
│ 25 │         User user = new User(                                           │
│    │           ↑ CREATE NEW USER OBJECT                                      │
│    │           request.getUsername(),                                        │
│    │           request.getEmail(),                                           │
│    │           passwordEncoder.encode(request.getPassword()),  ← ENCRYPT   │
│    │           Role.USER,                                                    │
│    │           true                                                          │
│    │         );                                                              │
│    │                                                                             │
│    │         Encode password: turns "password123" into something like:        │
│    │         "$2a$10$N9qo8uLOickgx2ZMRZoMye.IQTG7VR5P3cXjL1P1jKj1P1jKj1Kj"  │
│    │         This hash is what we store in database                          │
│    │         Even if database is stolen, passwords are safe!                │
├─────────────────────────────────────────────────────────────────────────────┤
│ 26 │                                                                             │
│ 27 │         user = userRepository.save(user);                               │
│    │           ↑ SAVE TO DATABASE                                            │
│    │           save() = INSERT if new, UPDATE if exists                     │
│    │           user now has generated ID                                     │
│    │           Example: user.getId() = 5                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 28 │         log.info("User saved with ID: {}", user.getId());              │
│    │           ↑ LOG SUCCESS                                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 29 │                                                                             │
│ 30 │         String token = jwtService.generateToken(                        │
│    │           user.getUsername(),                                           │
│    │           user.getId(),                                                 │
│    │           user.getRole().name()                                         │
│    │         );                                                              │
│    │           ↑ GENERATE JWT TOKEN                                           │
│    │           Token contains: username, userId, role                        │
│    │           Token will be used for authentication in future requests     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 31 │                                                                             │
│ 32 │         AuthResponse response = new AuthResponse();                     │
│    │           ↑ CREATE RESPONSE OBJECT                                      │
│    │           AuthResponse will be returned to client                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ 33 │         response.setToken(token);                                        │
│    │           ↑ SET TOKEN IN RESPONSE                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 34 │         response.setUserId(user.getId());                              │
│    │           ↑ SET USER ID                                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 35 │         response.setUsername(user.getUsername());                       │
│    │           ↑ SET USERNAME                                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ 36 │         response.setEmail(user.getEmail());                             │
│    │           ↑ SET EMAIL                                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 37 │         response.setRole(user.getRole().name());                       │
│    │           ↑ SET ROLE                                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 38 │         return response;                                                │
│    │           ↑ RETURN TO CONTROLLER (which returns to client)             │
│    │           Client receives: {token, userId, username, email, role}      │
│ 39 │     }                                                                   │
│ 40 │                                                                             │
│ 41 │     public AuthResponse login(LoginRequest request) {                   │
│    │       ↑ LOGIN METHOD                                                     │
│    │       Takes: usernameOrEmail, password                                   │
│    │       Returns: token + user info if successful                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 42 │         log.info("Processing login for: {}", request.getUsernameOrEmail());│
│    │           ↑ LOG LOGIN ATTEMPT                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 43 │         User user = userRepository.findByUsername(request.getUsernameOrEmail())│
│    │           .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail()))│
│    │           ↑ FIND USER BY USERNAME OR EMAIL                              │
│    │           First tries username, if not found, tries email              │
│    │           orElseGet = if not found, execute this alternative            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 44 │                 .orElseThrow(() -> new BadRequestException("Invalid credentials"));│
│    │           ↑ IF NOT FOUND IN DATABASE, THROW ERROR                       │
│    │           "Invalid credentials" = don't say if user or email wrong     │
│    │           (Security: don't reveal what exactly was wrong)              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 45 │                                                                             │
│ 46 │         if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {│
│    │           ↑ VERIFY PASSWORD                                             │
│    │           matches(plainText, hashedPassword)                             │
│    │           Returns true if password matches hash                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 47 │             throw new BadRequestException("Invalid credentials");      │
│    │               ↑ IF PASSWORD WRONG, THROW ERROR                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ 48 │         }                                                               │
│    │                                                                             │
│ 49 │         if (!user.getEnabled()) {                                      │
│    │           ↑ CHECK IF ACCOUNT IS ENABLED                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 50 │             throw new BadRequestException("Account is disabled");      │
│    │               ↑ ADMIN MAY HAVE DISABLED THIS ACCOUNT                    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 51 │         }                                                               │
│    │                                                                             │
│ 52 │         String token = jwtService.generateToken(                        │
│    │           user.getUsername(),                                           │
│    │           user.getId(),                                                 │
│    │           user.getRole().name()                                         │
│    │         );                                                              │
│    │           ↑ GENERATE JWT TOKEN (same as registration)                   │
│    │           Token will expire in 24 hours                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ 53 │         log.info("Login successful for user ID: {}", user.getId());   │
│    │           ↑ LOG SUCCESS                                                  │
│    │           Important for security auditing                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ 54 │         return AuthResponse with token + user info;                    │
│    │           ↑ RETURN TO CLIENT                                             │
│ 55 │     }                                                                   │
│ 56 │ }                                                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### 4.1.5 AuthController.java (REST API Endpoints)

```
File: user-service/src/main/java/com/courtbooking/userservice/controller/AuthController.java

┌─────────────────────────────────────────────────────────────────────────────┐
│ 1  │ package com.courtbooking.userservice.controller;                         │
│    │   ↑ Package for controllers (API endpoints)                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2  │                                                                             │
│ 3  │ import lombok.RequiredArgsConstructor;                                   │
│    │   ↑ Lombok: generates constructor for final fields                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4  │ import lombok.extern.slf4j.Slf4j;                                         │
│    │   ↑ Lombok: generates logger                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5  │ import org.springframework.http.ResponseEntity;                           │
│    │   ↑ Spring HTTP response wrapper                                        │
│    │   ResponseEntity.ok() = 200 OK                                          │
│    │   ResponseEntity.created() = 201 Created                                │
│    │   ResponseEntity.badRequest() = 400 Bad Request                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 6  │ import org.springframework.web.bind.annotation.*;                      │
│    │   ↑ Import all REST annotations                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ 7  │                                                                             │
│ 8  │ @RestController                                                          │
│    │   ↑ MARKS THIS CLASS AS REST CONTROLLER                                 │
│    │   - @Controller + @ResponseBody combined                                │
│    │   - Every method returns JSON (not view/template)                      │
│    │   - Spring scans this class and registers endpoints                    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 9  │ @RequestMapping("/auth")                                                │
│    │   ↑ ALL ENDPOINTS START WITH /auth                                       │
│    │   Combined with @PostMapping("/register") = /auth/register              │
├─────────────────────────────────────────────────────────────────────────────┤
│ 10 │ @RequiredArgsConstructor                                                │
│    │   ↑ Lombok: generates constructor for UserService injection             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 11 │ @Slf4j                                                                   │
│    │   ↑ Lombok: generates logger 'log'                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 12 │ public class AuthController {                                           │
│    │   ↑ Handles HTTP requests for authentication                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 13 │                                                                             │
│ 14 │     private final UserService userService;                              │
│    │       ↑ SERVICE INJECTION                                               │
│    │       Spring creates UserService and injects here                       │
│    │       We call userService.register() and userService.login()           │
├─────────────────────────────────────────────────────────────────────────────┤
│ 15 │                                                                             │
│ 16 │     @PostMapping("/register")                                            │
│    │       ↑ HTTP POST METHOD                                                │
│    │       URL: /auth/register (from @RequestMapping)                       │
│    │       Used to create new resources                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 17 │     public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {│
│    │           ↑ @RequestBody = parse JSON body into RegisterRequest         │
│    │           @Valid = validate the request using annotations                │
│    │           RegisterRequest has: username, email, password                │
│    │           Returns: AuthResponse (token + user info)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 18 │         log.info("Registration request received");                      │
│    │           ↑ LOG REQUEST RECEIVED                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│ 19 │         AuthResponse response = userService.register(request);        │
│    │           ↑ CALL SERVICE TO PROCESS LOGIC                               │
│    │           Service handles: validation, save, token generation          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 20 │         log.info("Registration successful");                          │
│    │           ↑ LOG SUCCESS                                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 21 │         return ResponseEntity.status(HttpStatus.CREATED).body(response);│
│    │           ↑ RETURN 201 CREATED                                           │
│    │           HTTP Status 201 = "Created"                                   │
│    │           Body contains AuthResponse (JSON)                            │
│ 22 │     }                                                                   │
│ 23 │                                                                             │
│ 24 │     @PostMapping("/login")                                               │
│    │       ↑ HTTP POST METHOD                                                │
│    │       URL: /auth/login                                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 25 │     public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {│
│    │           ↑ Takes: usernameOrEmail + password                             │
│    │           Returns: AuthResponse (token + user info)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 26 │         log.info("Login request received");                            │
│    │           ↑ LOG LOGIN ATTEMPT                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 27 │         AuthResponse response = userService.login(request);            │
│    │           ↑ CALL SERVICE TO VERIFY CREDENTIALS                           │
│    │           Service handles: find user, verify password, generate token  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 28 │         log.info("Login successful for user ID: {}", response.getUserId());│
│    │           ↑ LOG SUCCESS WITH USER ID                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 29 │         return ResponseEntity.ok(response);                            │
│    │           ↑ RETURN 200 OK                                                │
│    │           HTTP Status 200 = "OK"                                        │
│ 30 │     }                                                                   │
│ 31 │ }                                                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Annotations Explained

### Spring Annotations

| Annotation | What It Does | Example |
|------------|--------------|---------|
| `@SpringBootApplication` | Marks main class, enables auto-config | Main entry point |
| `@RestController` | Makes class handle JSON HTTP requests | Controller classes |
| `@Controller` | Makes class handle web requests (views) | Traditional web MVC |
| `@Service` | Marks business logic class | Service classes |
| `@Repository` | Marks data access class | Repository interfaces |
| `@Component` | Generic Spring bean | For any managed component |
| `@Autowired` | Inject dependency (constructor or field) | "Give me UserService" |
| `@RequestMapping` | Set base URL for endpoints | @RequestMapping("/users") |
| `@GetMapping` | Handle GET requests | @GetMapping("/1") |
| `@PostMapping` | Handle POST requests | @PostMapping |
| `@PutMapping` | Handle PUT requests | @PutMapping("/1") |
| `@DeleteMapping` | Handle DELETE requests | @DeleteMapping("/1") |
| `@RequestBody` | Parse JSON body to object | JSON → Java object |
| `@PathVariable` | Get value from URL path | /users/{id} → id param |
| `@RequestParam` | Get query parameter | ?userId=1 → userId param |
| `@RequestHeader` | Get HTTP header | Authorization: Bearer ... |

### JPA Annotations

| Annotation | What It Does | Example |
|------------|--------------|---------|
| `@Entity` | Marks class as database table | User.java |
| `@Table(name="x")` | Set table name | @Table(name="users") |
| `@Id` | Marks primary key field | id field |
| `@GeneratedValue` | Auto-generate ID | auto_increment |
| `@Column(...)` | Configure column settings | nullable, unique |
| `@Enumerated` | Store enum as string/number | Role stored as STRING |
| `@CreationTimestamp` | Auto-set on create | created_at |
| `@UpdateTimestamp` | Auto-set on update | updated_at |
| `@OneToMany` | One parent, many children | User → Bookings |
| `@ManyToOne` | Many children, one parent | Booking → User |

### Lombok Annotations

| Annotation | What It Does |
|------------|--------------|
| `@Data` | Generates getters, setters, equals, hashCode, toString |
| `@Getter` | Generates only getters |
| `@Setter` | Generates only setters |
| `@NoArgsConstructor` | Generates no-argument constructor |
| `@AllArgsConstructor` | Generates constructor with all fields |
| `@RequiredArgsConstructor` | Generates constructor with final fields |
| `@Slf4j` | Generates logger (log.info(), log.error(), etc.) |
| `@Builder` | Enables builder pattern for object creation |

### Validation Annotations (Jakarta)

| Annotation | What It Does |
|------------|--------------|
| `@NotNull` | Field cannot be null |
| `@NotBlank` | String cannot be null or empty |
| `@NotEmpty` | Collection cannot be empty |
| `@Size(min, max)` | String/collection size limits |
| `@Email` | Must be valid email format |
| `@Min`, `@Max` | Number range validation |
| `@Pattern(regex)` | Must match regex pattern |
| `@Valid` | Validate nested object |

### Spring Security Annotations

| Annotation | What It Does |
|------------|--------------|
| `@EnableWebSecurity` | Enable Spring Security |
| `@Configuration` | Security configuration class |
| `@EnableMethodSecurity` | Enable method-level security |
| `@PreAuthorize("hasRole('ADMIN')")` | Check role before method |
| `@EnableFeignClients` | Enable Feign client |
| `@FeignClient(name="x")` | Declare Feign client |

---

## 6. Commands for Testing

### 6.1 Build Commands

```bash
# Build entire project (from CourtBook directory)
mvn clean install

# Build specific service
cd user-service
mvn clean install

cd ../booking-service
mvn clean install

# Skip tests during build (faster)
mvn clean install -DskipTests

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 6.2 Test Commands

```bash
# Run all tests
mvn test

# Run tests with coverage report (JaCoCo)
mvn test

# View coverage report
# Open: target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run specific test method
mvn test -Dtest=UserServiceTest#testRegisterSuccess

# Run tests with verbose output
mvn test -X

# Skip tests
mvn clean install -DskipTests
```

### 6.3 SonarQube Analysis

```bash
# Run SonarQube analysis
mvn sonar:sonar

# With specific Sonar properties
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000

# Full analysis with coverage
mvn clean verify sonar:sonar

# Skip specific quality gates
mvn sonar:sonar -Dsonar.skipQualityGates=true
```

### 6.4 Start Services

```bash
# Start in order (MUST start Eureka first!)
# Terminal 1: Eureka Server
cd eureka-server
mvn spring-boot:run
# → Runs on http://localhost:8761

# Terminal 2: API Gateway
cd api-gateway
mvn spring-boot:run
# → Runs on http://localhost:8080

# Terminal 3: User Service
cd user-service
mvn spring-boot:run
# → Runs on http://localhost:8081

# Terminal 4: Booking Service
cd booking-service
mvn spring-boot:run
# → Runs on http://localhost:8082

# Terminal 5: Payment Service
cd payment-service
mvn spring-boot:run
# → Runs on http://localhost:8083
```

### 6.5 Test with Postman

```bash
# 1. Register a user
POST http://localhost:8080/auth/register
Content-Type: application/json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

# Response: {"token": "eyJ...", "userId": 1, ...}

# 2. Login (use token from response)
POST http://localhost:8080/auth/login
Content-Type: application/json
{
  "usernameOrEmail": "testuser",
  "password": "password123"
}

# 3. Create Court (Admin - first make yourself admin in DB)
POST http://localhost:8080/courts
Authorization: Bearer eyJ...
Content-Type: application/json
{
  "name": "Tennis Court 1",
  "sportType": "Tennis",
  "location": "Building A",
  "pricePerHour": 50.00
}

# 4. Get All Courts
GET http://localhost:8080/courts
Authorization: Bearer eyJ...

# 5. Create Booking
POST http://localhost:8080/bookings
Authorization: Bearer eyJ...
Content-Type: application/json
{
  "userId": 1,
  "courtId": 1,
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}

# 6. Get My Bookings
GET http://localhost:8080/bookings/user/1
Authorization: Bearer eyJ...

# 7. Process Payment
POST http://localhost:8080/api/payments/1/process
Authorization: Bearer eyJ...

# 8. Cancel Booking
DELETE http://localhost:8080/bookings/1?userId=1
Authorization: Bearer eyJ...
```

### 6.6 Check Logs

```bash
# View logs directory
ls -la logs/

# User service logs
tail -f logs/user-service.log

# Booking service logs
tail -f logs/booking-service.log

# Payment service logs
tail -f logs/payment-service.log

# API Gateway logs
tail -f logs/api-gateway.log
```

---

## 7. Security & Authorization Explained

### 7.1 JWT Token Structure

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6ImpvaG4iLCJyb2xlIjoiVVNFUiIsImlhdCI6MTcxNDA2MDgwMCwiZXhwIjoxNzE0MTQ0MjAwfQ.abc123xyz789

├── HEADER (before first .)
│   {"alg":"HS256","typ":"JWT"}
│
├── PAYLOAD (before second .)
│   {"sub":"1",        ← userId
│    "name":"john",    ← username
│    "role":"USER",    ← role
│    "iat":1714060800, ← issued at
│    "exp":1714147200} ← expiration
│
└── SIGNATURE (after second .)
    HMACSHA256(
      base64UrlEncode(header) + "." + base64UrlEncode(payload),
      secret_key
    )
```

### 7.2 Authentication Flow

```
1. CLIENT sends username/password to /auth/login
       │
       ▼
2. USER SERVICE validates credentials
       │
       ▼
3. USER SERVICE generates JWT token
       │
       ▼
4. CLIENT receives token and stores it
       │
       ▼
5. CLIENT sends subsequent requests with:
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
       │
       ▼
6. API GATEWAY validates token signature
       │
       ▼
7. API GATEWAY extracts user ID and role
       │
       ▼
8. API GATEWAY adds headers X-User-Id, X-User-Role
       │
       ▼
9. Request routed to appropriate service
```

### 7.3 Role-Based Access Control

```
┌─────────────────────────────────────────────────────────────────┐
│                        ROLES                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ADMIN: Can do everything                                       │
│   ├── Register, Login                                           │
│   ├── View all users                                            │
│   ├── Create/Update/Delete courts                               │
│   ├── View all bookings                                         │
│   └── Process/Refund payments                                   │
│                                                                 │
│   USER: Can only do their own things                            │
│   ├── Register, Login                                           │
│   ├── View courts                                               │
│   ├── Create/Cancel own bookings                               │
│   └── View own bookings                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

HOW ROLES ARE CHECKED:
══════════════════════

1. User logs in → receives JWT with role claim
2. JWT contains: {"sub":"1","name":"john","role":"USER",...}
3. User requests /users (Admin only)
4. API Gateway sees /users path (requires ADMIN role)
5. Or service checks X-User-Role header
6. If role matches, request allowed; otherwise, 403 Forbidden

IN CODE:
════════

@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<UserDTO>> getAllUsers() { ... }

OR at Gateway level:
routes:
  - id: user-service
    uri: lb://user-service
    predicates:
      - Path=/users/**
    filters:
      - StripPrefix=0
      - RoleCheck=ADMIN  ← Custom filter to check role
```

### 7.4 Password Security (BCrypt)

```
Plain Password: "password123"
        │
        ▼
   BCrypt Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
        │
        ▼
   Stored in Database

HOW BCrypt WORKS:
═════════════════

1. Hash is slow by design (prevents brute force attacks)
2. Each hash includes "salt" (random data added)
3. Same password → different hash each time (because salt changes)
4. To verify: compare plain password with stored hash
5. BCrypt handles salt automatically

WHY BCrypt?
══════════

- Industry standard (used by many companies)
- Slow (prevents fast cracking)
- Handles salt automatically
- One-way (cannot reverse hash to password)
```

---

## Summary

### Workflow Summary

```
1. CLIENT ──── POST /auth/register ────▶ GATEWAY ────▶ USER-SERVICE
                                              │
                                              ▼
                                        EUREKA (finds service)
                                              │
                                              ▼
                                        DATABASE (saves user)
                                              │
                                              ▼
                                        ◀─── Returns JWT ────

2. CLIENT ──── GET /courts (JWT) ────▶ GATEWAY
                                       │
                                       ▼
                                 Validates JWT
                                 Extracts user info
                                 Adds headers
                                       │
                                       ▼
                                 BOOKING-SERVICE
                                       │
                                       ▼
                                 DATABASE (gets courts)
                                       │
                                       ▼
                                 ◀─── Returns courts ────

3. CLIENT ──── POST /bookings (JWT) ────▶ GATEWAY ────▶ BOOKING-SVC
                                              │
                                              ▼
                                        Validates JWT
                                        Adds X-User-Id header
                                              │
                                              ▼
                                        BOOKING-SERVICE
                                              │
                                    ┌─────────┴─────────┐
                                    ▼                   ▼
                               WebClient           WebClient
                            validate user      create payment
                                    │                   │
                                    ▼                   ▼
                               USER-SERVICE      PAYMENT-SERVICE
                                    │                   │
                                    ▼                   ▼
                               ◀─── Valid ───┐    ◀─── Created ──┘
                                              │
                                              ▼
                                        DATABASE (saves booking)
                                              │
                                              ▼
                                        ◀─── Returns booking ────
```

### Key Takeaways

1. **Every request goes through Gateway** - Single entry point
2. **Gateway validates JWT** - No token = No access (except public paths)
3. **Services talk via WebClient** - Async, non-blocking communication
4. **Eureka discovers services** - No hardcoded URLs
5. **Each service has own database** - Data isolation
6. **All logs to files** - With FileAppender configuration
7. **JaCoCo checks coverage** - 100% required
8. **SonarQube analyzes code** - No high/critical issues
9. **Swagger documents APIs** - Auto-generated from code

---

**Document Version:** 1.0
**Last Updated:** 2026-05-14
**Total Pages:** This documentation provides comprehensive understanding of the entire Court Booking Microservices application.