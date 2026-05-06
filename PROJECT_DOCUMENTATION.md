# Court Booking Microservices - Complete Project Documentation

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture & Technology Stack](#2-architecture--technology-stack)
3. [System Architecture Diagram](#3-system-architecture-diagram)
4. [Service Breakdown](#4-service-breakdown)
5. [End-to-End Flow](#5-end-to-end-flow)
6. [API Endpoints Reference](#6-api-endpoints-reference)
7. [Code Deep Dive](#7-code-deep-dive)
8. [Postman Testing Guide](#8-postman-testing-guide)
9. [Database Schema](#9-database-schema)
10. [Security Implementation](#10-security-implementation)
11. [Running the Application](#11-running-the-application)

---

## 1. Project Overview

### What is this project?

This is a **Court Booking Microservices Application** built with Spring Boot that allows users to book sports courts (tennis, badminton, basketball, etc.). It's a full-stack backend system designed for scalability using microservices architecture.

### Key Features

- **User Management**: Register, login, role-based access control
- **Court Management**: Add, update, delete courts (Admin only)
- **Booking Management**: Create, view, cancel bookings
- **Payment Processing**: Automatic payment creation when booking is made
- **Service Discovery**: All services register with Eureka for dynamic discovery
- **API Gateway**: Single entry point with JWT authentication and request routing
- **API Documentation**: Swagger UI for all services

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming language |
| Spring Boot | 3.2.0 | Framework |
| Spring Cloud | 2023.0.0 | Microservices framework |
| MySQL | 8.0+ | Database |
| JWT | - | Token-based authentication |
| Eureka | - | Service registry/discovery |
| API Gateway | - | Request routing & auth |
| Swagger/OpenAPI | 3.0 | API documentation |
| Maven | 3.8+ | Build tool |

---

## 2. Architecture & Technology Stack

### Why Microservices?

Monolithic applications become hard to maintain as they grow. Microservices architecture:

- **Scalability**: Each service can scale independently
- **Maintainability**: Teams can work on different services
- **Fault Isolation**: One service failure doesn't crash the entire system
- **Technology Flexibility**: Each service can use different technologies

### Technology Components Explained

#### Spring Boot (3.2.0)
The core framework that makes building Java applications easy. It provides:
- Embedded server (no need for external app servers)
- Auto-configuration
- Starter dependencies
- Production-ready features

#### Spring Cloud (2023.0.0)
Provides tools for building distributed systems:
- Service discovery (Eureka)
- Load balancing
- API Gateway
- Distributed configuration

#### JWT (JSON Web Tokens)
A compact, URL-safe way to transmit claims between parties. In our system:
- Token contains: userId, username, role
- Expiration time: configurable
- Secret key: used for signing/verification

#### MySQL Database
Relational database for storing:
- Users (user-service)
- Courts and Bookings (booking-service)
- Payments (payment-service)

#### Eureka Server
Netflix Eureka is a service registry where all microservices register themselves. Other services can then discover them dynamically instead of hardcoding URLs.

#### API Gateway
Single entry point for all clients. Handles:
- Request routing to appropriate service
- JWT token validation
- Load balancing
- Rate limiting

---

## 3. System Architecture Diagram

```
                                    ┌─────────────────────────────┐
                                    │       CLIENT APPS           │
                                    │ (Mobile/Web/Postman)       │
                                    └────────────┬──────────────┘
                                                 │
                                                 │ HTTP Requests
                                                 ▼
                                    ┌─────────────────────────────┐
                                    │     API GATEWAY           │
                                    │      (Port 8080)          │
                                    │  ┌───────────────────┐    │
                                    │  │ JWT Filter        │    │
                                    │  │ (Validates token)  │    │
                                    │  └────────┬────────┘    │
                                    │           │              │
                                    │  ┌────────┴────────┐      │
                                    │  │ Route Config    │      │
                                    │  │ /auth/** →User  │      │
                                    │  │ /bookings/** →Book│      │
                                    │  │ /courts/** →Book  │      │
                                    │  │ /payments/** →Pay│      │
                                    │  └───────────────────┘    │
                                    └────────────┬──────────────┘
                                               │
                        ┌─────────────────────────┼─────────────────────────┐
                        │                         │                         │
                        ▼                         ▼                         ▼
            ┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐
            │   USER SERVICE    │   │ BOOKING SERVICE   │   │  PAYMENT SERVICE  │
            │    (Port 8081)   │   │   (Port 8082)    │   │   (Port 8083)    │
            │                  │   │                  │   │                  │
            │ - /auth/register │   │ - /courts/**     │   │ - /api/payments │
            │ - /auth/login    │   │ - /bookings/**  │   │                  │
            │ - /users/**     │   │                  │   │                  │
            └───────┬─────────┘   └───────┬─────────┘   └───────┬─────────┘
                    │                    │                    │
                    └─────────────────────┼────────────────────┘
                                        │
                                        ▼
                               ┌─────────────────────┐
                               │  EUREKA SERVER     │
                               │   (Port 8761)      │
                               │                   │
                               │ Service Registry  │
                               │ & Discovery      │
                               └──────────────────┘

                      ┌─────���───────────────────────────────────────────────┐
                      │                 DATABASE LAYER                    │
                      ├──────────────┬───────────────┬─────────────────────┤
                      │ court_      │ court_       │ court_            │
                      │ booking_   │ booking_     │ booking_         │
                      │ users      │ db          │ payments        │
                      └────────────┴─────────────┴───────────────────┘
```

### Request Flow Example

```
User wants to create a booking:
==============================================

1. Client sends POST /bookings with JWT token
   Header: Authorization: Bearer eyJhbGci...
            │
            ▼
2. API Gateway receives request
   - JWT Filter validates token
   - Extracts userId, role from token
   - Adds headers: X-User-Id, X-User-Role
            │
            ▼
3. Gateway routes to Booking Service (lb://booking-service)
   - Uses Eureka service discovery
   - Load balances between instances
            │
            ▼
4. Booking Service receives request
   - Validates user exists (calls User Service)
   - Validates court available
   - Checks for time slot conflicts
   - Creates booking in database
   - Calls Payment Service to create payment
            │
            ▼
5. Payment Service creates payment record
   - Stores payment with PENDING status
            │
            ▼
6. Response flows back through Gateway to Client
   Booking created successfully!
```

---

## 4. Service Breakdown

### 4.1 Eureka Server (Port 8761)

**Purpose**: Service Registry and Discovery

The Eureka Server acts as a phone book for all microservices. Each service registers itself on startup with its name and address. Other services can look up the addresses dynamically.

**Key Files**:

| File | Purpose |
|------|---------|
| `EurekaServerApplication.java` | Main Application class |

**Configuration** (`application.yml`):
```yaml
server:
  port: 8761
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false  # No self-registration
    fetchRegistry: false        # No registry fetching
```

### 4.2 API Gateway (Port 8080)

**Purpose**: Single entry point for all clients

The API Gateway is the front door of the application. All requests go through it:

1. **JWT Validation**: Checks if token is valid (except public paths)
2. **Routing**: Directs requests to appropriate service based on URL path
3. **Load Balancing**: Distributes traffic across service instances

**Key Components**:

| Component | File | Purpose |
|-----------|------|---------|
| Main App | `ApiGatewayApplication.java` | Spring Cloud Gateway application |
| JWT Filter | `JwtAuthenticationFilter.java` | Validates JWT tokens, extracts user info |

**Routing Configuration**:

| Path | Service | Load Balanced |
|------|---------|---------------|
| /auth/** | user-service | Yes |
| /users/** | user-service | Yes |
| /courts/** | booking-service | Yes |
| /bookings/** | booking-service | Yes |
| /payments/** | payment-service | Yes |

**Public Paths** (No JWT required):
- /auth/register
- /auth/login
- /eureka/**
- /actuator/**

### 4.3 User Service (Port 8081)

**Purpose**: User management and authentication

Handles:
- User registration
- User login (returns JWT token)
- User data retrieval
- User validation (for other services)

**Key Endpoints**:

| Endpoint | Method | Auth | Description |
|----------|--------|-----|-------------|
| /auth/register | POST | No | Register new user |
| /auth/login | POST | No | Login, get token |
| /users/{id} | GET | JWT (ADMIN) | Get user by ID |
| /users | GET | JWT (ADMIN) | Get all users |

**Database**: `court_booking_users`

**Key Files**:

| Layer | Files |
|-------|-------|
| Controller | `AuthController.java`, `UserController.java` |
| Service | `UserService.java` |
| Entity | `User.java` |
| Repository | `UserRepository.java` |
| Security | `JwtService.java`, `JwtAuthenticationFilter.java` |

### 4.4 Booking Service (Port 8082)

**Purpose**: Court and booking management

Handles:
- Court CRUD operations (Admin)
- Booking creation and management
- Available slot checking
- Integration with Payment Service

**Key Endpoints**:

| Endpoint | Method | Auth | Description |
|----------|--------|-----|-------------|
| /courts | POST | JWT (ADMIN) | Add court |
| /courts | GET | JWT | Get all courts |
| /courts/available | GET | JWT | Get available courts |
| /bookings | POST | JWT | Create booking |
| /bookings/{id} | DELETE | JWT | Cancel booking |
| /bookings/user/{userId} | GET | JWT | Get user bookings |

**Database**: `court_booking_db`

**Key Files**:

| Layer | Files |
|-------|-------|
| Controller | `BookingController.java`, `CourtController.java` |
| Service | `BookingService.java`, `CourtService.java` |
| Entity | `Booking.java`, `Court.java` |
| Repository | `BookingRepository.java`, `CourtRepository.java` |
| Client | `UserServiceClient.java`, `PaymentServiceClient.java` |

### 4.5 Payment Service (Port 8083)

**Purpose**: Payment processing

Handles:
- Creating payments
- Processing payments
- Refunding payments
- Payment history

**Key Endpoints**:

| Endpoint | Method | Auth | Description |
|----------|--------|-----|-------------|
| /api/payments | POST | JWT | Create payment |
| /api/payments/{id} | GET | JWT | Get payment |
| /api/payments/{id}/process | POST | JWT | Process payment |
| /api/payments/{id}/refund | POST | JWT | Refund payment |
| /api/payments/booking/{bookingId} | GET | JWT | Get by booking |

**Database**: `court_booking_payments`

**Key Files**:

| Layer | Files |
|-------|-------|
| Controller | `PaymentController.java` |
| Service | `PaymentService.java` |
| Entity | `Payment.java` |
| Repository | `PaymentRepository.java` |

---

## 5. End-to-End Flow

### Flow 1: User Registration and Login

```
Step 1: Register User
═══════════════════
Client → API Gateway (8080) → User Service (8081)
                    │
                    ▼
            POST /auth/register
            Body:
            {
              "username": "john",
              "email": "john@example.com",
              "password": "password123"
            }
                    │
                    ▼
            User saved to database (BCrypt encrypted password)
                    │
                    ▼
            Returns: AuthResponse with token
            {
              "token": "eyJhbGc...",
              "userId": 1,
              "username": "john",
              "email": "john@example.com",
              "role": "USER"
            }


Step 2: Login (Get Token)
══════════════════════
Client → API Gateway → User Service
                    │
                    ▼
            POST /auth/login
            Body:
            {
              "username": "john",
              "password": "password123"
            }
                    │
                    ▼
            Validates password with BCrypt
                    │
                    ▼
            Returns: AuthResponse with new JWT token


Step 3: Get Token Details (Decoded)
═════════════════════════════
JWT Payload:
{
  "sub": "1",           // userId
  "username": "john",    // username from claim
  "role": "USER",        // role from claim
  "iat": 1714060800,    // issued at
  "exp": 1714147200    // expiration
}
```

### Flow 2: Create Booking with Payment

```
Complete Booking Flow
═══════════════════
1. Client (with JWT token)
   │
   ▼
2. API Gateway
   - JWT Filter validates token
   - Extracts X-User-Id and X-User-Role headers
   - Routes to booking-service
   │
   ▼
3. Booking Service
   a) Validate user exists
      - Calls UserServiceClient.validateUser(userId)
      - Makes REST call to User Service
      - Returns boolean
      │
   b) Get court details
      - Finds court by courtId from database
      - Checks if court is available
      │
   c) Check for conflicts
      - Queries existing bookings
      - Checks if time slot overlaps
      - Prevents double booking
      │
   d) Calculate price
      - hours = endTime - startTime
      - amount = hours × pricePerHour
      │
   e) Create booking
      - Saves to database
      - Status: CONFIRMED
      │
   f) Create payment (if amount > 0)
      - Calls PaymentServiceClient.createPayment()
      - Makes REST call to Payment Service
      - Sends: bookingId, userId, amount
      │
   ▼
4. Payment Service
   - Creates Payment record
   - Status: PENDING
   - Returns payment ID
   │
   ▼
5. Response back to Client
   {
     "id": 1,
     "userId": 1,
     "courtId": 1,
     "courtName": "Court Alpha",
     "bookingDate": "2026-05-20",
     "startTime": "10:00",
     "endTime": "11:00",
     "status": "CONFIRMED",
     "paymentId": 1,
     "amount": 50.00
   }
```

---

## 6. API Endpoints Reference

### User Service (Port 8081)

```
Base URL: http://localhost:8080 (via API Gateway)

Authentication Endpoints
══════════════════════

POST /auth/register
──────────────────
Register a new user account
──────────────────

Request:
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123"
}

Response (201 Created):
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "username": "newuser",
  "email": "user@example.com",
  "role": "USER"
}

─────────────────────────────────────────────────

POST /auth/login
────────────────
Login and get JWT token
────────────────

Request:
{
  "username": "newuser",
  "password": "password123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "username": "newuser",
  "email": "user@example.com",
  "role": "USER"
}

─────────────────────────────────────────────────

GET /users/{id}
──────────────
Get user by ID (Admin only)
──────────────

Headers:
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "username": "newuser",
  "email": "user@example.com",
  "role": "USER",
  "createdAt": "2026-01-01T10:00:00"
}
```

### Booking Service (Port 8082)

```
Base URL: http://localhost:8080 (via API Gateway)

Court Endpoints
══════════════

POST /courts
────────────
Add a new court (Admin only)
────────────

Headers:
Authorization: Bearer <token>

Request:
{
  "name": "Court Alpha",
  "sportType": "Tennis",
  "location": "Ground Floor - Court 1",
  "pricePerHour": 50.00,
  "available": true
}

Response (201 Created):
{
  "id": 1,
  "name": "Court Alpha",
  "sportType": "Tennis",
  "location": "Ground Floor - Court 1",
  "pricePerHour": 50.00,
  "available": true
}

─────────────────────────────────────────────────

GET /courts
──────────
Get all courts
──────────

Headers:
Authorization: Bearer <token>

Response (200 OK):
[
  {
    "id": 1,
    "name": "Court Alpha",
    "sportType": "Tennis",
    "location": "Ground Floor - Court 1",
    "pricePerHour": 50.00,
    "available": true
  }
]

────────────────���─���──────────────────────────────

GET /courts/available
───────────────────
Get available courts only
───────────────────

Headers:
Authorization: Bearer <token>

Response (200 OK):
[
  {
    "id": 1,
    "name": "Court Alpha",
    "sportType": "Tennis",
    "available": true
  }
]

─────────────────────────────────────────────────

Booking Endpoints
═════════════════

POST /bookings
─────────────
Create a new booking
─────────────

Headers:
Authorization: Bearer <token>

Request:
{
  "userId": 1,
  "courtId": 1,
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "notes": "Morning practice"
}

Response (201 Created):
{
  "id": 1,
  "userId": 1,
  "courtId": 1,
  "courtName": "Court Alpha",
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "status": "CONFIRMED",
  "paymentId": 1,
  "amount": 50.00,
  "notes": "Morning practice"
}

─────────────────────────────────────────────────

GET /bookings/user/{userId}
────────────────────────
Get all bookings for a user
────────────────────────

Headers:
Authorization: Bearer <token>

Response (200 OK):
[
  {
    "id": 1,
    "userId": 1,
    "courtName": "Court Alpha",
    "bookingDate": "2026-05-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "status": "CONFIRMED"
  }
]

─────────────────────────────────────────────────

DELETE /bookings/{id}
──────────────────
Cancel a booking
──────────────────

Headers:
Authorization: Bearer <token>

Note: Pass userId as query parameter: /bookings/1?userId=1

Response (204 No Content)
```

### Payment Service (Port 8083)

```
Base URL: http://localhost:8080 (via API Gateway)

POST /api/payments
────────────────
Create a new payment
────────────────

Headers:
Authorization: Bearer <token>

Request:
{
  "bookingId": 1,
  "userId": 1,
  "amount": 50.00,
  "paymentMethod": "ONLINE"
}

Response (201 Created):
{
  "id": 1,
  "bookingId": 1,
  "userId": 1,
  "amount": 50.00,
  "status": "PENDING",
  "paymentMethod": "ONLINE",
  "transactionId": "TXN-123456",
  "paymentDate": "2026-05-20T10:30:00"
}

─────────────────────────────────────────────────

POST /api/payments/{id}/process
───────────────────────────
Process a pending payment
───────────────────────────

Headers:
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "bookingId": 1,
  "amount": 50.00,
  "status": "COMPLETED",
  "transactionId": "TXN-123456"
}

─────────────────────────────────────────────────

GET /api/payments/{id}
────────────────────
Get payment by ID
────────────────────

Headers:
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "bookingId": 1,
  "userId": 1,
  "amount": 50.00,
  "status": "COMPLETED",
  "paymentMethod": "ONLINE",
  "transactionId": "TXN-123456",
  "paymentDate": "2026-05-20T10:30:00"
}
```

---

## 7. Code Deep Dive

### 7.1 JWT Service - Token Generation and Validation

**File**: `user-service/src/main/java/com/courtbooking/userservice/security/JwtService.java`

```java
package com.courtbooking.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String jwtSecret;
    private final long jwtExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    // LINE 26-36: Generate JWT Token
    public String generateToken(String username, Long userId, String role) {
        // Create a secret key from the jwtSecret string
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Build the JWT token with claims
        return Jwts.builder()
                .subject(userId.toString())           // "sub" claim = userId
                .claim("username", username)        // Custom claim
                .claim("role", role)               // Custom claim for authorization
                .issuedAt(new Date())            // "iat" = issued at time
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // "exp" = expiration
                .signWith(key)                      // Sign with secret key
                .compact();                      // Convert to string
    }

    // LINE 38-46: Validate Token
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            // Try to parse and verify the token
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;  // Valid
        } catch (Exception e) {
            return false;  // Invalid or expired
        }
    }

    // LINE 48-56: Extract User ID from Token
    public Long getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());  // Get "sub" claim
    }

    // LINE 58-66: Extract Role from Token
    public String getRoleFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);  // Get "role" claim
    }
}
```

**Key Concepts**:
- **Subject (sub)**: User ID as identifier
- **Claims**: Additional data (username, role)
- **Signature**: HMAC-SHA256 signed with secret key
- **Expiration**: Configurable timeout

### 7.2 API Gateway JWT Filter

**File**: `api-gateway/src/main/java/com/courtbooking/apigateway/config/JwtAuthenticationFilter.java`

```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final String jwtSecret;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    // LINE 30-35: Public paths that don't require JWT
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/eureka/**",
            "/actuator/**"
    );

    // LINE 38-74: Main filter method - called for every request
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Check if path is public (no auth required)
        if (isPublicPath(path)) {
            return chain.filter(exchange);  // Allow without token
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        // 3. Check if token exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse());  // Reject
        }

        // 4. Extract token from "Bearer <token>"
        String token = authHeader.substring(7);

        try {
            // 5. Validate and parse JWT
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 6. Extract user info from token
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 7. Add headers for downstream services
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", userId)      // User ID
                            .header("X-User-Role", role != null ? role : ""))
                    .build();

            // 8. Continue to next filter
            return chain.filter(modifiedExchange);
        } catch (Exception e) {
            return unauthorized(exchange.getResponse());  // Invalid token
        }
    }

    // LINE 80-83: Return 401 Unauthorized
    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    // LINE 86-88: Filter order (-100 = high priority)
    @Override
    public int getOrder() {
        return -100;
    }
}
```

**Flow**:
```
Request → Filter
    │
    ├── Is public path? → Yes → Allow
    │
    ├── Has Authorization header? → No → 401 Unauthorized
    │
    ├── Validate JWT → Invalid → 401 Unauthorized
    │
    ├── Extract user info ✓
    │
    ├── Add X-User-Id, X-User-Role headers
    │
    └── Route to service
```

### 7.3 Booking Service - Creating a Booking

**File**: `booking-service/src/main/java/com/courtbooking/bookingservice/service/BookingService.java`

```java
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    // LINE 38-91: Create Booking Method
    @Transactional
    public BookingDTO createBooking(BookingRequest request) {
        
        // 1. Validate user exists by calling User Service
        if (!userServiceClient.validateUser(request.getUserId())) {
            throw new BadRequestException("Invalid user ID: " + request.getUserId());
        }

        // 2. Get court details
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
        
        // 3. Check if court is available
        if (!court.getAvailable()) {
            throw new BadRequestException("Court is not available");
        }

        // 4. Check for time slot conflicts
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getCourtId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Court is already booked for the selected time slot");
        }

        // 5. Calculate price
        double hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        double amount = 0;
        if (court.getPricePerHour() != null) {
            amount = hours * court.getPricePerHour();
        }

        // 6. Create booking entity
        Booking booking = new Booking(
            request.getUserId(),
            court,
            request.getBookingDate(),
            request.getStartTime(),
            request.getEndTime(),
            BookingStatus.CONFIRMED,
            request.getNotes()
        );

        // 7. Save to database
        booking = bookingRepository.save(booking);

        // 8. Create payment if amount > 0
        if (amount > 0) {
            Long paymentId = paymentServiceClient.createPayment(
                booking.getId(),
                request.getUserId(),
                BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
            );
            booking.setPaymentId(paymentId);
            booking = bookingRepository.save(booking);
        }

        // 9. Return response DTO
        return mapToDTO(booking, amount);
    }

    private BookingDTO mapToDTO(Booking booking, double amount) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setCourtId(booking.getCourt().getId());
        dto.setCourtName(booking.getCourt().getName());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus().name());
        dto.setNotes(booking.getNotes());
        dto.setPaymentId(booking.getPaymentId());
        dto.setAmount(amount);
        return dto;
    }
}
```

**Validation Steps**:
1. **User Validation**: REST call to User Service
2. **Court Validation**: Check court exists in DB
3. **Availability**: Check if court.available = true
4. **Conflict Check**: Query overlapping bookings
5. **Price Calculation**: hours × pricePerHour
6. **Payment Integration**: Create payment record

### 7.4 Payment Service Client - Service-to-Service Communication

**File**: `booking-service/src/main/java/com/courtbooking/bookingservice/service/PaymentServiceClient.java`

```java
@Service
public class PaymentServiceClient {

    private final RestTemplate restTemplate;
    private final AppConfig appConfig;

    // LINE 23-49: Create Payment via REST
    public Long createPayment(Long bookingId, Long userId, BigDecimal amount) {
        try {
            // 1. Build the URL from config
            String url = appConfig.getPaymentServiceUrl() + "/api/payments";
            
            // 2. Set HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 3. Build request body as JSON string
            String requestBody = String.format(
                "{\"bookingId\":%d,\"userId\":%d,\"amount\":%s,\"paymentMethod\":\"ONLINE\"}",
                bookingId, userId, amount
            );
            
            // 4. Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            // 5. Make POST request
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            // 6. Extract payment ID from response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Number id = (Number) response.getBody().get("id");
                return id.longValue();
            }
            return null;
        } catch (Exception e) {
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }
}
```

**Why RestTemplate?**
- Simple service-to-service communication
- Synchronous (waits for response)
- Part of Spring framework

---

## 8. Postman Testing Guide

### Setup

1. Download Postman from https://postman.com
2. Create a new collection: "Court Booking"
3. Create environment with variables:
   - `baseUrl`: http://localhost:8080
   - `token`: (leave empty, we'll set it)

### Test 1: Register User

```
POST {{baseUrl}}/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123"
}
```

**Expected Response** (201):
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "username": "testuser",
  "email": "testuser@example.com",
  "role": "USER"
}
```

**Action**: Copy `token` to environment variable

### Test 2: Login

```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Response** (200):
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "username": "testuser",
  "email": "testuser@example.com",
  "role": "USER"
}
```

### Test 3: Add Court (Admin)

Note: First, manually update user role to ADMIN in database, or use admin credentials.

```
POST {{baseUrl}}/courts
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Court Alpha",
  "sportType": "Tennis",
  "location": "Ground Floor - Court 1",
  "pricePerHour": 50.00,
  "available": true
}
```

**Expected Response** (201):
```json
{
  "id": 1,
  "name": "Court Alpha",
  "sportType": "Tennis",
  "location": "Ground Floor - Court 1",
  "pricePerHour": 50.00,
  "available": true
}
```

### Test 4: Get All Courts

```
GET {{baseUrl}}/courts
Authorization: Bearer {{token}}
```

**Expected Response** (200):
```json
[
  {
    "id": 1,
    "name": "Court Alpha",
    "sportType": "Tennis",
    "location": "Ground Floor - Court 1",
    "pricePerHour": 50.00,
    "available": true
  }
]
```

### Test 5: Create Booking

```
POST {{baseUrl}}/bookings
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "userId": 1,
  "courtId": 1,
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "notes": "Morning practice"
}
```

**Expected Response** (201):
```json
{
  "id": 1,
  "userId": 1,
  "courtId": 1,
  "courtName": "Court Alpha",
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "status": "CONFIRMED",
  "paymentId": 1,
  "amount": 50.00,
  "notes": "Morning practice"
}
```

### Test 6: Get User Bookings

```
GET {{baseUrl}}/bookings/user/1
Authorization: Bearer {{token}}
```

**Expected Response** (200):
```json
[
  {
    "id": 1,
    "userId": 1,
    "courtName": "Court Alpha",
    "bookingDate": "2026-05-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "status": "CONFIRMED",
    "amount": 50.00
  }
]
```

### Test 7: Get Payment by ID

```
GET {{baseUrl}}/payments/1
Authorization: Bearer {{token}}
```

**Expected Response** (200):
```json
{
  "id": 1,
  "bookingId": 1,
  "userId": 1,
  "amount": 50.00,
  "status": "PENDING",
  "paymentMethod": "ONLINE",
  "transactionId": "TXN-...",
  "paymentDate": "2026-05-20T10:30:00"
}
```

### Test 8: Process Payment

```
POST {{baseUrl}}/api/payments/1/process
Authorization: Bearer {{token}}
```

**Expected Response** (200):
```json
{
  "id": 1,
  "bookingId": 1,
  "amount": 50.00,
  "status": "COMPLETED",
  "transactionId": "TXN-..."
}
```

### Collection Variables

Set up in Tests tab:
```javascript
const response = pm.response.json();
if (response.token) {
    pm.collectionVariables.set("token", response.token);
}
if (response.id) {
    pm.collectionVariables.set("bookingId", response.id);
}
if (response.paymentId) {
    pm.collectionVariables.set("paymentId", response.paymentId);
}
```

---

## 9. Database Schema

### court_booking_users (User Service)

```sql
CREATE DATABASE court_booking_users;

USE court_booking_users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Sample Admin User (password: password123, BCrypt encrypted)
INSERT INTO users (username, email, password, role) VALUES
('admin', 'admin@courtbooking.com', '$2a$10$...', 'ADMIN');
```

### court_booking_db (Booking Service)

```sql
CREATE DATABASE court_booking_db;

USE court_booking_db;

CREATE TABLE courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sport_type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    price_per_hour DECIMAL(10,2),
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    FOREIGN KEY (court_id) REFERENCES courts(id)
);
```

### court_booking_payments (Payment Service)

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
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 10. Security Implementation

### 10.1 Password Encryption (BCrypt)

```java
// In UserService.java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Usage
String encodedPassword = passwordEncoder.encode(rawPassword);
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

**Why BCrypt?**
- Automatic salt handling
- Configurable work factor (slow for brute force)
- Industry standard

### 10.2 JWT Token Structure

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "1",           // User ID
  "username": "john",     // Username
  "role": "USER",        // Role
  "iat": 1714060800,     // Issued at
  "exp": 1714147200     // Expiration
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret_key
)
```

### 10.3 Authorization Roles

| Role | Permissions |
|------|-------------|
| USER | Login, view courts, create bookings, view own bookings |
| ADMIN | All USER permissions + Manage courts, view all users, view all bookings |

---

## 11. Running the Application

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- IDE (IntelliJ/Eclipse)

### Database Setup

```sql
CREATE DATABASE court_booking_users;
CREATE DATABASE court_booking_db;
CREATE DATABASE court_booking_payments;
```

### Starting Order

```
1. Start Eureka Server (Port 8761)
   cd eureka-server
   mvn spring-boot:run

2. Start API Gateway (Port 8080)
   cd api-gateway
   mvn spring-boot:run

3. Start User Service (Port 8081)
   cd user-service
   mvn spring-boot:run

4. Start Booking Service (Port 8082)
   cd booking-service
   mvn spring-boot:run

5. Start Payment Service (Port 8083)
   cd payment-service
   mvn spring-boot:run
```

### Swagger Documentation URLs

| Service | Swagger UI URL |
|---------|---------------|
| User Service | http://localhost:8081/swagger-ui.html |
| Booking Service | http://localhost:8082/swagger-ui.html |
| Payment Service | http://localhost:8083/swagger-ui.html |

### Health Check Endpoints

| Service | Endpoint |
|---------|----------|
| Eureka | http://localhost:8761 |
| API Gateway | http://localhost:8080/actuator/health |
| User Service | http://localhost:8081/actuator/health |
| Booking Service | http://localhost:8082/actuator/health |
| Payment Service | http://localhost:8083/actuator/health |

---

## Summary

This microservices application demonstrates:

1. **Service Discovery**: Eureka for dynamic service registration
2. **API Gateway**: Central entry point with JWT authentication
3. **Service Communication**: REST calls between services
4. **Database per Service**: Separate databases for data isolation
5. **Security**: JWT tokens + BCrypt password encryption
6. **API Documentation**: Swagger UI integration
7. **Transaction Management**: @Transactional annotations
8. **Error Handling**: Global exception handlers

The application is production-ready with:
- Proper error handling
- Input validation
- Role-based access control
- Health monitoring endpoints
- API documentation