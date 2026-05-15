# Court Booking Microservices Application - Complete Technical Documentation

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Microservices Architecture Deep Dive](#2-microservices-architecture-deep-dive)
3. [Technology Stack Explained](#3-technology-stack-explained)
4. [System Architecture & Component Interactions](#4-system-architecture--component-interactions)
5. [End-to-End System Flow](#5-end-to-end-system-flow)
6. [Service-by-Service Breakdown](#6-service-by-service-breakdown)
7. [API Documentation & Endpoints](#7-api-documentation--endpoints)
8. [Database Architecture](#8-database-architecture)
9. [Security Implementation](#9-security-implementation)
10. [Code Deep Dive - Line by Line Explanations](#10-code-deep-dive---line-by-line-explanations)
11. [Postman Testing Guide](#11-postman-testing-guide)
12. [Swagger/OpenAPI Documentation Guide](#12-swaggeropenapi-documentation-guide)
13. [Configuration Management](#13-configuration-management)
14. [AOP (Aspect-Oriented Programming) in Common Module](#14-aop-aspect-oriented-programming-in-common-module)
15. [Running & Deployment Guide](#15-running--deployment-guide)
16. [Troubleshooting & Common Issues](#16-troubleshooting--common-issues)

---

## 1. Project Overview

### 1.1 What is this Project?

The **Court Booking Microservices Application** is a distributed backend system built with Spring Boot that allows sports facilities to manage court reservations. It enables users to:

- Register and authenticate as users
- View available sports courts (Tennis, Badminton, Basketball, etc.)
- Book courts for specific date/time slots
- Make payments for bookings
- Manage bookings (view, cancel)

### 1.2 Core Features

| Feature | Description |
|---------|-------------|
| **User Management** | Registration, Login, Role-based access (USER, ADMIN) |
| **Court Management** | CRUD operations for sports courts (Admin only) |
| **Booking System** | Create, view, and cancel court reservations |
| **Payment Processing** | Automatic payment creation when booking with fee |
| **Service Discovery** | Eureka server for dynamic service registration |
| **API Gateway** | Centralized JWT authentication and request routing |
| **API Documentation** | Swagger UI for all services |

### 1.3 Project Structure

```
CourtBook/
├── pom.xml                          # Parent Maven POM
├── common/                          # Shared AOP components
│   ├── pom.xml
│   └── src/main/java/com/courtbooking/common/
│       ├── CommonApplication.java   # Spring Boot Application
│       ├── config/AopConfig.java    # AOP configuration
│       └── aspect/
│           ├── LoggingAspect.java   # Method entry/exit logging
│           ├── PerformanceAspect.java # Performance monitoring
│           ├── ExceptionAspect.java   # Exception handling
│           ├── TraceAspect.java        # Request tracing
│           └── TraceFilter.java       # HTTP trace filter
├── eureka-server/                   # Service Registry (Port 8761)
│   └── src/main/resources/application.yml
├── api-gateway/                     # API Gateway (Port 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/courtbooking/apigateway/
│       │   ├── ApiGatewayApplication.java
│       │   └── config/
│       │       ├── JwtAuthenticationFilter.java  # JWT validation
│       │       ├── GatewayAuthorizationFilter.java # Role-based access
│       │       ├── JwtUtil.java                   # JWT utilities
│       │       └── SecurityConfig.java           # WebFlux security
│       └── resources/application.yml
├── user-service/                    # User Management (Port 8081)
│   └── src/main/java/com/courtbooking/userservice/
│       ├── controller/
│       │   ├── AuthController.java   # /auth/register, /auth/login
│       │   ├── UserController.java   # /users/**
│       │   └── UserValidationController.java # /users/validate/{id}
│       ├── service/UserService.java
│       ├── entity/
│       │   ├── User.java
│       │   └── Role.java (ADMIN, USER)
│       ├── repository/UserRepository.java
│       ├── dto/
│       │   ├── RegisterRequest.java
│       │   ├── LoginRequest.java
│       │   ├── AuthResponse.java
│       │   └── UserDTO.java
│       ├── security/JwtService.java
│       ├── config/
│       │   ├── EncoderConfig.java
│       │   └── OpenApiConfig.java
│       └── exception/
│           ├── GlobalExceptionHandler.java
│           ├── BadRequestException.java
│           ├── ResourceNotFoundException.java
│           └── ErrorResponse.java
├── booking-service/                # Booking Management (Port 8082)
│   └── src/main/java/com/courtbooking/bookingservice/
│       ├── controller/
│       │   ├── BookingController.java  # /bookings/**
│       │   └── CourtController.java    # /courts/**
│       ├── service/
│       │   ├── BookingService.java
│       │   ├── CourtService.java
│       │   ├── UserServiceClient.java  # REST client to User Service
│       │   └── PaymentServiceClient.java # REST client to Payment Service
│       ├── entity/
│       │   ├── Booking.java
│       │   ├── BookingStatus.java (CONFIRMED, CANCELLED)
│       │   ├── Court.java
│       ├── repository/
│       │   ├── BookingRepository.java
│       │   └── CourtRepository.java
│       ├── dto/
│       │   ├── BookingRequest.java
│       │   ├── BookingDTO.java
│       │   ├── CourtRequest.java
│       │   └── CourtDTO.java
│       └── exception/
│           └── (same structure as user-service)
└── payment-service/                 # Payment Processing (Port 8083)
    └── src/main/java/com/courtbooking/paymentservice/
        ├── controller/PaymentController.java  # /api/payments/**
        ├── service/PaymentService.java
        ├── entity/
        │   ├── Payment.java
        │   └── PaymentStatus.java (PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED)
        ├── repository/PaymentRepository.java
        ├── dto/
        │   ├── PaymentRequest.java
        │   └── PaymentResponse.java
        └── exception/
            └── (same structure as user-service)
```

---

## 2. Microservices Architecture Deep Dive

### 2.1 Why Microservices?

**Monolithic Architecture Problems:**
- Single point of failure - one bug crashes everything
- Cannot scale individual components
- All developers work on same codebase
- Technology lock-in - must use same language/framework for all

**Microservices Benefits:**
- **Fault Isolation**: One service failing doesn't crash the entire system
- **Independent Scaling**: Hot services can have more instances
- **Technology Flexibility**: Each service can use different tools
- **Team Autonomy**: Teams can work independently
- **Deployment Freedom**: Deploy services at different times

### 2.2 Service Responsibilities

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        COURT BOOKING MICROSERVICES                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                        EUREKA SERVER (8761)                          │  │
│  │                  Service Registry & Discovery Center                 │  │
│  │                                                                       │  │
│  │   All microservices register themselves here on startup              │  │
│  │   Other services look up addresses dynamically                       │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                    │                                       │
│                                    ▼                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                      API GATEWAY (8080)                              │  │
│  │                   Single Entry Point for All Clients                 │  │
│  │                                                                       │  │
│  │   Responsibilities:                                                   │  │
│  │   • JWT Token Validation                                              │  │
│  │   • Route requests to appropriate services                            │  │
│  │   • Add user info headers (X-User-Id, X-User-Role)                    │  │
│  │   • Load balancing between service instances                          │  │
│  │   • Rate limiting (configurable)                                      │  │
│  │   • CORS handling                                                      │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                    │                                       │
│          ┌─────────────────────────┼─────────────────────────┐             │
│          │                         │                         │             │
│          ▼                         ▼                         ▼             │
│  ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐      │
│  │   USER SERVICE    │  │ BOOKING SERVICE   │  │  PAYMENT SERVICE  │      │
│  │     (Port 8081)   │  │    (Port 8082)    │  │    (Port 8083)     │      │
│  │                   │  │                   │  │                   │      │
│  │ User Management   │  │ Court Management  │  │ Payment Processing │      │
│  │ Authentication   │  │ Booking Logic     │  │ Transaction Mgmt   │      │
│  │ Registration     │  │ Availability      │  │ Refund Handling    │      │
│  │ Role Management  │  │ Price Calculation │  │ Status Tracking    │      │
│  └───────────────────┘  └───────────────────┘  └───────────────────┘      │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                       COMMON MODULE (Library)                        │  │
│  │                Shared AOP Aspects for All Services                   │  │
│  │                                                                       │  │
│  │   • LoggingAspect: Auto-logs method entry/exit/errors                │  │
│  │   • PerformanceAspect: Monitors slow operations                      │  │
│  │   • ExceptionAspect: Centralized exception handling                   │  │
│  │   • TraceAspect: Request tracing for debugging                        │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Communication Patterns

**1. Synchronous HTTP (Used in this project)**

```
Booking Service                    Payment Service
      │                                  │
      │────── HTTP POST ─────────────────►│
      │      /api/payments               │
      │      {bookingId, userId, amount} │
      │                                  │
      │◄───── Response ──────────────────│
      │      {id, status, transactionId} │
```

**2. Service-to-Service Communication Flow**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SERVICE COMMUNICATION FLOW                           │
└─────────────────────────────────────────────────────────────────────────┘

BookingService.createBooking():
─────────────────────────────────────────────────────────────

Step 1: Validate User
         │
         ▼
    UserServiceClient.validateUser(userId)
         │
         ├──► HTTP GET /users/validate/123
         │
         └──► Response: true/false

Step 2: Check Court Availability
         │
         ▼
    CourtRepository.findById(courtId)
         │
         └──► Court object or exception

Step 3: Check Time Slot Conflicts
         │
         ▼
    BookingRepository.findConflictingBookings()
         │
         └──► Custom JPQL query:
              "SELECT b FROM Booking b WHERE 
               b.court.id = :courtId AND 
               b.bookingDate = :date AND 
               b.status = 'CONFIRMED' AND 
               b.startTime < :endTime AND 
               b.endTime > :startTime"

Step 4: Create Payment (if amount > 0)
         │
         ▼
    PaymentServiceClient.createPayment()
         │
         ├──► HTTP POST http://localhost:8080/api/payments
         │    Body: {"bookingId":1,"userId":1,"amount":50.00,"paymentMethod":"ONLINE"}
         │
         └──► Response: {"id":1,"status":"PENDING",...}
```

---

## 3. Technology Stack Explained

### 3.1 Core Technologies

| Technology | Version | Purpose | Why Use It |
|------------|---------|---------|------------|
| **Java** | 17 LTS | Programming Language | Long-term support, modern features |
| **Spring Boot** | 3.2.0 | Application Framework | Auto-configuration, embedded server |
| **Spring Cloud** | 2023.0.0 | Microservices Toolkit | Service discovery, API gateway |
| **Spring Data JPA** | - | Database ORM | Simplifies database operations |
| **MySQL** | 8.0+ | Relational Database | ACID compliance, reliability |
| **Maven** | 3.8+ | Build Tool | Dependency management, multi-module |
| **JWT** | - | Token Authentication | Stateless, scalable auth |
| **Eureka** | - | Service Registry | Dynamic service discovery |
| **Spring Cloud Gateway** | - | API Gateway | Request routing, filtering |
| **Springdoc OpenAPI** | - | API Documentation | Auto-generated Swagger UI |
| **Lombok** | - | Boilerplate Reduction | @Getter, @Setter, @Builder |
| **JJWT** | - | JWT Library | Token generation/validation |

### 3.2 Spring Cloud Gateway (vs Zuul)

**Why Spring Cloud Gateway?**
- Built on reactive stack (WebFlux)
- Non-blocking request handling
- Better performance under load
- Predicate-based routing
- Filter-based middleware

### 3.3 Spring Data JPA - How It Works

```java
// UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // Auto-implemented
    boolean existsByUsername(String username);       // Auto-implemented
}

// What happens behind the scenes:
// 1. Spring Data sees method name "findByUsername"
// 2. Parses method: "find" + "By" + "Username"
// 3. Generates JPQL: SELECT u FROM User u WHERE u.username = :username
// 4. Creates proxy implementation at runtime
```

### 3.4 JWT Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       JWT AUTHENTICATION FLOW                           │
└─────────────────────────────────────────────────────────────────────────┘

1. USER REGISTRATION:
─────────────────────────────────────────
User sends POST /auth/register
{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
}
         │
         ▼
AuthController.register()
         │
         ▼
UserService.register():
  1. Encode password with BCrypt
  2. Save user to database
  3. Generate JWT token with claims:
     {
       "sub": "1",           ← userId
       "username": "john",   ← username
       "role": "USER",        ← role
       "iat": 1714060800,     ← issued at
       "exp": 1714147200      ← expiration (24h)
     }
  4. Sign with HMAC-SHA256 secret key
         │
         ▼
Response:
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "john",
    "role": "USER"
}

2. MAKING AUTHENTICATED REQUEST:
─────────────────────────────────────────
Client sends GET /bookings with header:
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
         │
         ▼
API Gateway JwtAuthenticationFilter:
  1. Extract Authorization header
  2. Extract token from "Bearer <token>"
  3. Validate signature using secret key
  4. Parse claims (userId, username, role)
  5. Add headers for downstream service:
     X-User-Id: 1
     X-Username: john
     X-User-Role: USER
         │
         ▼
Route to booking-service with headers
         │
         ▼
BookingService receives request with user context
```

### 3.5 JWT Token Structure

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         JWT TOKEN ANATOMY                               │
└─────────────────────────────────────────────────────────────────────────┘

JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwi
      dXNlcm5hbWUiOiJqb2huIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3
      MTQwNjA4MDAsImV4cCI6MTcxNDE0NzIwMH0.SflKxwRJSMeKK
      F2QT4fwpMeJf36POk6yJV_adQssw5c"

              ▼ ▼ ▼

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   HEADER        │    │   PAYLOAD       │    │   SIGNATURE     │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ alg: "HS256"    │    │ sub: "1"        │    │ HMAC-SHA256(    │
│ typ: "JWT"      │    │ username: "john"│    │   header +      │
│                 │    │ role: "USER"    │    │   payload,      │
│ Base64URL       │    │ iat: 1714060800 │    │   secret        │
│ encoded         │    │ exp: 1714147200 │    │ )               │
└─────────────────┘    └─────────────────┘    └─────────────────┘

Decoded Payload:
{
  "sub": "1",           ← Subject (user ID)
  "username": "john",   ← Custom claim
  "role": "USER",       ← Custom claim
  "iat": 1714060800,    ← Issued at (Unix timestamp)
  "exp": 1714147200     ← Expiration (24 hours from issue)
}
```

---

## 4. System Architecture & Component Interactions

### 4.1 Complete System Architecture

```
                                    ┌─────────────────────────────────────────────┐
                                    │              CLIENT APPLICATIONS            │
                                    │  (Web Browser / Mobile App / Postman)       │
                                    └──────────────────────┬──────────────────────┘
                                                           │
                                                           │ HTTPS/HTTP Requests
                                                           │ Authorization: Bearer <JWT>
                                                           │
                                                           ▼
                                    ┌─────────────────────────────────────────────┐
                                    │              API GATEWAY                     │
                                    │               (Port 8080)                    │
                                    │  ┌─────────────────────────────────────────┐│
                                    │  │         JwtAuthenticationFilter          ││
                                    │  │  • Validates JWT token                   ││
                                    │  │  • Extracts user info                   ││
                                    │  │  • Adds X-User-Id, X-User-Role headers  ││
                                    │  └─────────────────────────────────────────┘│
                                    │  ┌─────────────────────────────────────────┐│
                                    │  │     GatewayAuthorizationFilter           ││
                                    │  │  • Checks role-based permissions        ││
                                    │  │  • Allows/denies requests                ││
                                    │  └─────────────────────────────────────────┘│
                                    │  ┌─────────────────────────────────────────┐│
                                    │  │            Route Configuration            ││
                                    │  │  /auth/** → user-service                 ││
                                    │  │  /users/** → user-service                ││
                                    │  │  /courts/** → booking-service           ││
                                    │  │  /bookings/** → booking-service         ││
                                    │  │  /api/payments/** → payment-service     ││
                                    │  └─────────────────────────────────────────┘│
                                    └──────────────────────┬──────────────────────┘
                                                           │
                                    ┌──────────────────────┼──────────────────────┐
                                    │                      │                      │
                                    ▼                      ▼                      ▼
                    ┌───────────────────────┐  ┌───────────────────────┐  ┌───────────────────────┐
                    │    USER SERVICE      │  │   BOOKING SERVICE     │  │   PAYMENT SERVICE     │
                    │     (Port 8081)      │  │     (Port 8082)       │  │     (Port 8083)       │
                    │                      │  │                      │  │                      │
                    │ Controllers:        │  │ Controllers:         │  │ Controllers:         │
                    │ • AuthController    │  │ • CourtController    │  │ • PaymentController  │
                    │ • UserController    │  │ • BookingController  │  │                      │
                    │ • UserValidation    │  │                      │  │ Services:            │
                    │                      │  │ Services:            │  │ • PaymentService     │
                    │ Services:           │  │ • UserServiceClient  │  │                      │
                    │ • UserService       │  │ • CourtService       │  │ Entities:            │
                    │                      │  │ • BookingService     │  │ • Payment            │
                    │ Entities:           │  │ • PaymentServiceClient│ │                      │
                    │ • User              │  │                      │  │ Repositories:        │
                    │ • Role (ADMIN/USER) │  │ Entities:            │  │ • PaymentRepository  │
                    │                      │  │ • Court              │  │                      │
                    │ Repositories:       │  │ • Booking            │  │ Database:            │
                    │ • UserRepository    │  │ • BookingStatus      │  │ court_booking_       │
                    │                      │  │                      │  │ payments             │
                    │ Database:            │  │ Repositories:        │  │                      │
                    │ court_booking_      │  │ • CourtRepository    │  │                      │
                    │ users               │  │ • BookingRepository  │  │                      │
                    │                      │  │                      │  │                      │
                    │ ┌────────────────┐  │  │ Database:            │  │                      │
                    │ │   JwtService  │  │  │ court_booking_db     │  │                      │
                    │ │ (Token Gen)   │  │  │                      │  │                      │
                    │ └────────────────┘  │  │ WebClient:           │  │                      │
                    └──────────────────────┘  │ • To UserService    │  └──────────────────────┘
                                              │ • To PaymentService │
                                              └──────────────────────┘
                                                           │
                                                           ▼
                                    ┌─────────────────────────────────────────────┐
                                    │              EUREKA SERVER                  │
                                    │               (Port 8761)                    │
                                    │                                             │
                                    │   ┌─────────────────────────────────────┐  │
                                    │   │         SERVICE REGISTRY            │  │
                                    │   │                                      │  │
                                    │   │  INSTANCE: api-gateway (localhost:8080)│  │
                                    │   │  INSTANCE: user-service (localhost:8081)│ │
                                    │   │  INSTANCE: booking-service (localhost:8082)│
                                    │   │  INSTANCE: payment-service (localhost:8083)│
                                    │   │                                      │  │
                                    │   │  Health checks every 30 seconds       │  │
                                    │   └─────────────────────────────────────┘  │
                                    └─────────────────────────────────────────────┘
```

### 4.2 Request Routing Logic

```yaml
# application.yml in api-gateway
spring:
  cloud:
    gateway:
      routes:
        # Route 1: User Service
        - id: user-service
          uri: lb://user-service        # Load balanced to user-service
          predicates:
            - Path=/auth/**,/users/**,/users  # Match these paths
          filters:
            - StripPrefix=0             # Don't strip any prefix

        # Route 2: Booking Service
        - id: booking-service
          uri: lb://booking-service
          predicates:
            - Path=/courts/**,/bookings/**,/bookings,/courts
          filters:
            - StripPrefix=0

        # Route 3: Payment Service
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
          filters:
            - StripPrefix=0
```

### 4.3 Inter-Service Communication

```
┌─────────────────────────────────────────────────────────────────────────┐
│           SERVICE-TO-SERVICE COMMUNICATION DIAGRAM                      │
└─────────────────────────────────────────────────────────────────────────┘

                    BOOKING SERVICE
                         │
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
   ┌───────────┐  ┌───────────┐  ┌───────────┐
   │   Court   │  │  Booking  │  │  Payment  │
   │ Repository│  │ Repository│  │  Service  │
   └───────────┘  └───────────┘  │  Client   │
         │               │         └─────┬─────┘
         │               │               │
         │               │               │
         ▼               │               ▼
   ┌───────────┐         │        ┌───────────┐
   │  MySQL    │         │        │ WebClient │
   │  Database │         │        └─────┬─────┘
   └───────────┘         │              │
                         │              │
                         │              │
   ┌───────────┐         │              │
   │   User    │         │              │
   │  Service  │         │              │
   │  Client   │◄─────────────────────────┤
   └─────┬─────┘         │              │
         │               │              │
         │               │              │
         │               │              │
   ┌─────┴─────┐         │              │
   │ WebClient │         │              │
   └─────┬─────┘         │              │
         │               │              │
         └───────────────┼──────────────┘
                         │
                         ▼
                   ┌───────────┐
                   │  Payment  │
                   │  Service  │
                   └─────┬─────┘
                         │
                         ▼
                   ┌───────────┐
                   │  MySQL    │
                   │  Database │
                   └───────────┘
```

---

## 5. End-to-End System Flow

### 5.1 Complete User Journey

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     COMPLETE USER JOURNEY                               │
└─────────────────────────────────────────────────────────────────────────┘

╔═══════════════════════════════════════════════════════════════════════╗
║  STEP 1: Register                                                        ║
╚═══════════════════════════════════════════════════════════════════════╝

Client                          API Gateway                    User Service
   │                                 │                               │
   │ POST /auth/register             │                               │
   │ {                               │                               │
   │   "username": "john",           │                               │
   │   "email": "john@example.com",  │                               │
   │   "password": "password123"    │                               │
   │ }                               │                               │
   │──────────────────────────────────►│                               │
   │                                 ││ /auth/register              │
   │                                 ││ (public path, no JWT)       │
   │                                 │└─────────────────────────────►
   │                                 │                               │
   │                                 │                               │
   │                                 │   1. Check username exists?   │
   │                                 │   2. Check email exists?      │
   │                                 │   3. BCrypt.encode(password)  │
   │                                 │   4. Save to users table      │
   │                                 │   5. Generate JWT token       │
   │                                 │                               │
   │◄─────────────────────────────────│ Response (201 Created)       │
   │ {                                │ {                            │
   │   "token": "eyJ...",             │   "token": "eyJ...",          │
   │   "userId": 1,                   │   "userId": 1,               │
   │   "username": "john",            │   "username": "john",         │
   │   "role": "USER"                 │   "role": "USER"              │
   │ }                                │ }                            │

╔═══════════════════════════════════════════════════════════════════════╗
║  STEP 2: Login (Get Token)                                               ║
╚═══════════════════════════════════════════════════════════════════════╝

Client                          API Gateway                    User Service
   │                                 │                               │
   │ POST /auth/login                │                               │
   │ {                               │                               │
   │   "usernameOrEmail": "john",     │                               │
   │   "password": "password123"    │                               │
   │ }                               │                               │
   │──────────────────────────────────►│                               │
   │                                 ││ /auth/login                 │
   │                                 ││ (public path, no JWT)       │
   │                                 │└─────────────────────────────►
   │                                 │                               │
   │                                 │   1. Find user by username    │
   │                                 │      or email                 │
   │                                 │   2. BCrypt.matches(password)  │
   │                                 │   3. Check if enabled         │
   │                                 │   4. Generate new JWT token   │
   │                                 │                               │
   │◄─────────────────────────────────│ Response (200 OK)            │
   │ {                                │ {                            │
   │   "token": "eyJ...",             │   "token": "eyJ...",          │
   │   "userId": 1,                   │   "userId": 1,               │
   │   "username": "john",            │   "username": "john",        │
   │   "role": "USER"                 │   "role": "USER"              │
   │ }                                │ }                            │

╔═══════════════════════════════════════════════════════════════════════╗
║  STEP 3: Add Court (Admin Only)                                          ║
╚═══════════════════════════════════════════════════════════════════════╝

Client                          API Gateway                    Booking Service
   │                                 │                               │
   │ POST /courts                    │                               │
   │ Authorization: Bearer eyJ...    │                               │
   │ {                               │                               │
   │   "name": "Court Alpha",         │                               │
   │   "sportType": "Tennis",         │                               │
   │   "location": "Ground Floor",    │                               │
   │   "pricePerHour": 50.00,        │                               │
   │   "available": true             │                               │
   │ }                               │                               │
   │──────────────────────────────────►│                               │
   │                                 ││ 1. JwtAuthenticationFilter:  │
   │                                 ││    - Validate token          │
   │                                 ││    - Extract X-User-Role     │
   │                                 ││    - X-User-Role: ADMIN      │
   │                                 ││                              │
   │                                 ││ 2. GatewayAuthorizationFilter:│
   │                                 ││    - Check /courts path      │
   │                                 ││    - ADMIN role has access   │
   │                                 ││    - Allowed!                │
   │                                 ││                              │
   │                                 ││ /courts (route to booking-service)
   │                                 ││ + Add headers:               │
   │                                 ││   X-User-Role: ADMIN         │
   │                                 │└─────────────────────────────►
   │                                 │                               │
   │                                 │   CourtService.addCourt()     │
   │                                 │   1. Create Court entity      │
   │                                 │   2. Save to courts table     │
   │                                 │   3. Return CourtDTO          │
   │                                 │                               │
   │◄─────────────────────────────────│ Response (201 Created)       │
   │ {                                │ {                            │
   │   "id": 1,                       │   "id": 1,                    │
   │   "name": "Court Alpha",         │   "name": "Court Alpha",      │
   │   "sportType": "Tennis",         │   "sportType": "Tennis",     │
   │   "pricePerHour": 50.00,         │   "pricePerHour": 50.00,     │
   │   "available": true             │   "available": true          │
   │ }                                │ }                            │

╔═══════════════════════════════════════════════════════════════════════╗
║  STEP 4: Create Booking (with automatic payment)                         ║
╚═══════════════════════════════════════════════════════════════════════╝

Client                          API Gateway                    Booking Service
   │                                 │                               │
   │ POST /bookings                  │                               │
   │ Authorization: Bearer eyJ...    │                               │
   │ {                               │                               │
   │   "userId": 1,                  │                               │
   │   "courtId": 1,                 │                               │
   │   "bookingDate": "2026-05-20",  │                               │
   │   "startTime": "10:00:00",      │                               │
   │   "endTime": "11:00:00",        │                               │
   │   "notes": "Morning practice"   │                               │
   │ }                               │                               │
   │──────────────────────────────────►│                               │
   │                                 ││ JwtAuthenticationFilter      │
   │                                 ││ - Validate token            │
   │                                 ││ - Add X-User-Id: 1          │
   │                                 ││ - Add X-User-Role: USER     │
   │                                 ││                              │
   │                                 ││ GatewayAuthorizationFilter   │
   │                                 ││ - /bookings path            │
   │                                 ││ - USER role allowed         │
   │                                 ││                              │
   │                                 ││ Route to booking-service    │
   │                                 │└─────────────────────────────►
   │                                 │                               │
   │                                 │   BookingService.createBooking():
   │                                 │                               │
   │                                 │   1. Validate User            │
   │                                 │      UserServiceClient.validateUser(1)
   │                                 │      │                         │
   │                                 │      │ GET /users/validate/1   │
   │                                 │      ▼                         │
   │                                 │   ┌─────────────────────┐    │
   │                                 │   │   User Service      │    │
   │                                 │   │   Returns: true     │    │
   │                                 │   └─────────────────────┘    │
   │                                 │                               │
   │                                 │   2. Get Court               │
   │                                 │      CourtRepository.findById(1)
   │                                 │      → Court Alpha           │
   │                                 │                               │
   │                                 │   3. Check Availability       │
   │                                 │      court.available = true   │
   │                                 │                               │
   │                                 │   4. Check Time Conflicts    │
   │                                 │      BookingRepository.      │
   │                                 │      findConflictingBookings()
   │                                 │      → No conflicts          │
   │                                 │                               │
   │                                 │   5. Calculate Amount        │
   │                                 │      hours = 11:00 - 10:00 = 1
   │                                 │      amount = 1 × 50.00 = 50 │
   │                                 │                               │
   │                                 │   6. Create Booking          │
   │                                 │      BookingRepository.save()│
   │                                 │      → Booking ID: 1          │
   │                                 │      → Status: CONFIRMED      │
   │                                 │                               │
   │                                 │   7. Create Payment (amount > 0)
   │                                 │      PaymentServiceClient.   │
   │                                 │      createPayment()          │
   │                                 │      │                        │
   │                                 │      │ POST http://localhost:8080
   │                                 │      │         /api/payments   │
   │                                 │      ▼                        │
   │                                 │   ┌─────────────────────┐    │
   │                                 │   │  Payment Service    │    │
   │                                 │   │                     │    │
   │                                 │   │  1. Create Payment  │    │
   │                                 │   │     - Generate TXN  │    │
   │                                 │   │     - Status: PEND  │    │
   │                                 │   │  2. Save to DB     │    │
   │                                 │   │  3. Return {id: 1}  │    │
   │                                 │   └─────────────────────┘    │
   │                                 │      ← Payment ID: 1         │
   │                                 │                               │
   │                                 │   8. Update Booking          │
   │                                 │      booking.setPaymentId(1) │
   │                                 │      bookingRepository.save()│
   │                                 │                               │
   │◄─────────────────────────────────│ Response (201 Created)       │
   │ {                                │ {                            │
   │   "id": 1,                       │   "id": 1,                    │
   │   "userId": 1,                   │   "userId": 1,               │
   │   "courtId": 1,                  │   "courtId": 1,              │
   │   "courtName": "Court Alpha",    │   "courtName": "Court Alpha",│
   │   "bookingDate": "2026-05-20",   │   "bookingDate": "2026-05-20",
   │   "startTime": "10:00:00",       │   "startTime": "10:00:00",  │
   │   "endTime": "11:00:00",         │   "endTime": "11:00:00",    │
   │   "status": "CONFIRMED",         │   "status": "CONFIRMED",    │
   │   "paymentId": 1,                │   "paymentId": 1,           │
   │   "amount": 50.00                │   "amount": 50.00           │
   │ }                                │ }                            │
```

---

## 6. Service-by-Service Breakdown

### 6.1 Eureka Server (Port 8761)

**Purpose**: Service Registry and Discovery

**What it does**:
- Acts as a "phone book" for all microservices
- All services register themselves on startup
- Other services can discover them dynamically
- Provides health monitoring

**Key Files**:
- `EurekaServerApplication.java` - Main Spring Boot application
- `application.yml` - Configuration

**Configuration Explained**:
```yaml
eureka:
  instance:
    hostname: localhost              # Instance hostname
  client:
    registerWithEureka: false        # This is a server, don't register itself
    fetchRegistry: false            # No need to fetch registry (it's the registry)
  server:
    enableSelfPreservation: false    # Disable self-preservation mode (for dev)
```

**Eureka Dashboard**: http://localhost:8761

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          EUREKA DASHBOARD                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  System Status: UP (All services healthy)                              │
│                                                                         │
│  Instances currently registered with Eureka:                           │
│  ┌─────────────────┬──────────┬────────┬────────────────────────────┐ │
│  │ Application     │ AMIs     │ Status │ Metadata                   │ │
│  ├─────────────────┼──────────┼────────┼────────────────────────────┤ │
│  │ API-GATEWAY     │ n/a(1)   │ UP     │ localhost:8080             │ │
│  │ USER-SERVICE    │ n/a(1)   │ UP     │ localhost:8081            │ │
│  │ BOOKING-SERVICE  │ n/a(1)   │ UP     │ localhost:8082            │ │
│  │ PAYMENT-SERVICE │ n/a(1)   │ UP     │ localhost:8083            │ │
│  └─────────────────┴──────────┴────────┴────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 API Gateway (Port 8080)

**Purpose**: Single Entry Point, Security, Routing

**Components**:

1. **JwtAuthenticationFilter** (Order: -100, runs first)
   - Validates JWT tokens
   - Extracts user information
   - Adds headers for downstream services

2. **GatewayAuthorizationFilter** (Order: 100, runs second)
   - Checks role-based permissions
   - Allows/denies requests based on path and role

3. **SecurityConfig**
   - Configures Spring Security for WebFlux
   - Permits public paths

**JWT Authentication Filter Deep Dive**:

```java
// JwtAuthenticationFilter.java - Line by Line Analysis

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    // GlobalFilter: Interface for all Spring Cloud Gateway filters
    // Ordered: Allows us to specify execution order

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    // Logger for debugging and monitoring

    private final String jwtSecret;
    // Injected from application.yml: app.jwt.secret

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",        // User registration (public)
            "/auth/login",           // User login (public)
            "/users/validate/",      // Service-to-service validation (public)
            "/eureka/**",            // Eureka dashboard (public)
            "/actuator/**"           // Health endpoints (public)
    );
    // Paths that don't require JWT authentication

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Main filter method - called for EVERY request
        
        ServerHttpRequest request = exchange.getRequest();
        // Get the incoming HTTP request
        String path = request.getURI().getPath();
        // Extract the URL path (e.g., "/bookings", "/auth/login")

        logger.info("JWT Filter: Processing path = {}", path);

        // STEP 1: Check if path is public (no auth required)
        if (isPublicPath(path)) {
            logger.info("JWT Filter: Path is public, skipping");
            return chain.filter(exchange);  // Continue without validation
        }

        // STEP 2: Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        // Gets: "Authorization: Bearer eyJhbGci..."

        // STEP 3: Validate header exists and has Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse());  // Return 401
        }

        // STEP 4: Extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        try {
            // STEP 5: Parse and validate JWT token
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            // Create HMAC-SHA256 key from secret string
            
            Claims claims = Jwts.parser()
                    .verifyWith(key)                    // Use key to verify signature
                    .build()
                    .parseSignedClaims(token)          // Parse and validate token
                    .getPayload();                      // Get the payload (claims)

            // STEP 6: Extract user information from token
            String userId = claims.getSubject();        // "sub" claim = user ID
            String username = claims.get("username", String.class);  // Custom claim
            String role = claims.get("role", String.class);          // Custom claim

            // STEP 7: Create modified request with user headers
            ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
            // Mutate = create a builder to modify the request

            requestBuilder.header("X-User-Id", userId);       // User ID for downstream
            if (username != null) {
                requestBuilder.header("X-Username", username);
            }
            if (role != null) {
                requestBuilder.header("X-User-Role", role);
            }
            // These headers are sent to the actual service

            logger.info("JWT Filter: Added headers - X-User-Id: {}, X-User-Role: {}", userId, role);

            // STEP 8: Create modified exchange with new headers
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(requestBuilder.build())
                    .build();

            // STEP 9: Continue to next filter (GatewayAuthorizationFilter)
            return chain.filter(modifiedExchange);
            
        } catch (Exception e) {
            // Token invalid or expired
            logger.error("JWT Filter: Token validation failed: {}", e.getMessage());
            return unauthorized(exchange.getResponse());  // Return 401
        }
    }

    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();  // Send 401 response
    }

    @Override
    public int getOrder() {
        return -100;  // High priority, runs before other filters
    }
}
```

### 6.3 User Service (Port 8081)

**Purpose**: User management, Authentication

**Database**: `court_booking_users`

**Entities**:
- `User` - User account information
- `Role` - Enum (ADMIN, USER)

**Controllers**:

1. **AuthController** - Authentication endpoints
   - `POST /auth/register` - Register new user
   - `POST /auth/login` - Login and get JWT token

2. **UserController** - User management (Admin only)
   - `GET /users/{id}` - Get user by ID
   - `GET /users` - Get all users

3. **UserValidationController** - For service-to-service communication
   - `GET /users/validate/{id}` - Validate if user exists

**UserService Deep Dive**:

```java
// UserService.java - Line by Line Analysis

@Service
public class UserService {

    private final UserRepository userRepository;
    // Spring Data JPA repository - auto-implemented methods
    private final PasswordEncoder passwordEncoder;
    // BCrypt password encoder for secure password storage
    private final JwtService jwtService;
    // Service for generating JWT tokens

    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtService jwtService) {
        // Constructor injection - Spring auto-wires these dependencies
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional  // Ensures atomic operation, rolls back on failure
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        // Step 2: Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Step 3: Create User entity with encoded password
        // BCrypt.hashpw(password, BCrypt.gensalt()) - automatic salt generation
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),  // BCrypt encryption
            Role.USER,                                       // Default role
            true                                            // Enabled = true
        );

        // Step 4: Save user to database
        user = userRepository.save(user);

        // Step 5: Generate JWT token with user info
        // Token contains: userId, username, role, expiration time
        String token = jwtService.generateToken(
            user.getUsername(), 
            user.getId(), 
            user.getRole().name()
        );

        // Step 6: Build and return AuthResponse
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        // Step 1: Find user by username OR email
        // First try username, if not found try email
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new BadRequestException("Invalid username/email or password")));

        // Step 2: Validate password against stored BCrypt hash
        // passwordEncoder.matches(rawPassword, encodedPassword)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username/email or password");
        }

        // Step 3: Check if user account is enabled
        if (!user.getEnabled()) {
            throw new BadRequestException("User account is disabled");
        }

        // Step 4: Generate new JWT token
        String token = jwtService.generateToken(
            user.getUsername(), 
            user.getId(), 
            user.getRole().name()
        );

        // Step 5: Build and return AuthResponse
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }

    // Service-to-service validation endpoint
    public boolean validateUser(Long userId) {
        return userRepository.findById(userId)
                .map(User::getEnabled)  // If user exists, return enabled status
                .orElse(false);         // If user doesn't exist, return false
    }
}
```

### 6.4 Booking Service (Port 8082)

**Purpose**: Court and booking management, Payment integration

**Database**: `court_booking_db`

**Entities**:
- `Court` - Sports court information
- `Booking` - Court reservation
- `BookingStatus` - Enum (CONFIRMED, CANCELLED)

**Controllers**:

1. **CourtController** - Court management
   - `POST /courts` - Add new court (Admin)
   - `PUT /courts/{id}` - Update court (Admin)
   - `DELETE /courts/{id}` - Delete court (Admin)
   - `GET /courts` - Get all courts
   - `GET /courts/{id}` - Get court by ID
   - `GET /courts/available` - Get available courts

2. **BookingController** - Booking management
   - `POST /bookings` - Create booking
   - `DELETE /bookings/{id}` - Cancel booking
   - `GET /bookings` - Get all bookings
   - `GET /bookings/user/{userId}` - Get user's bookings
   - `GET /bookings/available` - Get available time slots

**BookingService Deep Dive**:

```java
// BookingService.java - Complete Line-by-Line Analysis

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    // Repository for booking database operations
    private final CourtRepository courtRepository;
    // Repository for court database operations
    private final UserServiceClient userServiceClient;
    // REST client to call User Service for validation
    private final PaymentServiceClient paymentServiceClient;
    // REST client to call Payment Service

    @Transactional  // Entire method runs as a single database transaction
    public BookingDTO createBooking(BookingRequest request) {
        
        // STEP 1: Validate User via User Service
        // Make HTTP call to user-service to verify user exists
        if (!userServiceClient.validateUser(request.getUserId())) {
            throw new BadRequestException("Invalid user ID: " + request.getUserId());
        }

        // STEP 2: Get Court Details
        // Find court in database or throw ResourceNotFoundException
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Court not found with id: " + request.getCourtId()));

        // STEP 3: Check Court Availability
        // Only allow booking if court.available = true
        if (!court.getAvailable()) {
            throw new BadRequestException("Court is not available");
        }

        // STEP 4: Check for Time Slot Conflicts
        // Use custom JPQL query to find overlapping bookings
        // Query finds bookings where:
        //   - Same court
        //   - Same date
        //   - Same status (CONFIRMED)
        //   - Time ranges overlap (startTime < endTime AND endTime > startTime)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getCourtId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        // If any conflicts found, reject the booking
        if (!conflicts.isEmpty()) {
            throw new BadRequestException(
                "Court is already booked for the selected time slot");
        }

        // STEP 5: Calculate Price
        // Calculate duration in hours
        double hours = Duration.between(request.getStartTime(), request.getEndTime())
                       .toHours();
        double amount = 0;
        if (court.getPricePerHour() != null) {
            amount = hours * court.getPricePerHour();
        }

        // STEP 6: Create Booking Entity
        Booking booking = new Booking(
            request.getUserId(),
            court,
            request.getBookingDate(),
            request.getStartTime(),
            request.getEndTime(),
            BookingStatus.CONFIRMED,  // Default status
            request.getNotes()
        );

        // STEP 7: Save Booking to Database
        booking = bookingRepository.save(booking);
        // Now booking has ID: booking.getId()

        // STEP 8: Create Payment (if amount > 0)
        // Only create payment if there's a fee (some courts might be free)
        if (amount > 0) {
            Long paymentId = paymentServiceClient.createPayment(
                booking.getId(),
                request.getUserId(),
                BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
            );
            // Update booking with payment ID
            booking.setPaymentId(paymentId);
            booking = bookingRepository.save(booking);
        }

        // STEP 9: Return BookingDTO
        return mapToDTO(booking, amount);
    }

    @Transactional
    public void cancelBooking(Long id, Long userId) {
        // Find booking by ID and user ID (user can only cancel their own bookings)
        Booking booking = bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Booking not found with id: " + id));

        // Update status to CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public List<BookingDTO> getBookingsByUser(Long userId) {
        // Find all bookings for a specific user
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)  // Convert each booking to DTO
                .collect(Collectors.toList());  // Collect into list
    }

    // Helper method: Convert Booking entity to BookingDTO
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

**PaymentServiceClient Deep Dive**:

```java
// PaymentServiceClient.java - REST Client Implementation

@Service
public class PaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);
    private final WebClient webClient;

    // WebClient: Spring's reactive HTTP client (replaces RestTemplate)
    public PaymentServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Long createPayment(Long bookingId, Long userId, BigDecimal amount) {
        try {
            // STEP 1: Make HTTP POST request to Payment Service
            // WebClient is configured with base URL: http://localhost:8080
            Map<String, Object> response = webClient.post()
                    .uri("/api/payments")  // Append to base URL
                    .bodyValue(Map.of(  // Request body as JSON
                            "bookingId", bookingId,
                            "userId", userId,
                            "amount", amount,
                            "paymentMethod", "ONLINE"
                    ))
                    .retrieve()  // Start retrieving response
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> 
                        Mono.error(new BadRequestException("Payment creation failed")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> 
                        Mono.error(new BadRequestException("Payment service error")))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();  // Block until response received (synchronous)

            // STEP 2: Extract and return payment ID from response
            if (response != null && response.containsKey("id")) {
                return ((Number) response.get("id")).longValue();
            }
            return null;
            
        } catch (Exception e) {
            log.error("Error creating payment for booking {}: {}", bookingId, e.getMessage());
            throw new BadRequestException("Failed to create payment: " + e.getMessage());
        }
    }

    // Async version (non-blocking) - returns Mono<Long>
    public Mono<Long> createPaymentAsync(Long bookingId, Long userId, BigDecimal amount) {
        return webClient.post()
                .uri("/api/payments")
                .bodyValue(Map.of(
                        "bookingId", bookingId,
                        "userId", userId,
                        "amount", amount,
                        "paymentMethod", "ONLINE"
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    if (response != null && response.containsKey("id")) {
                        return ((Number) response.get("id")).longValue();
                    }
                    return -1L;
                })
                .doOnNext(id -> log.info("Async payment created with ID: {}", id))
                .onErrorResume(e -> {
                    log.error("Async payment creation failed: {}", e.getMessage());
                    return Mono.just(-1L);
                });
    }
}
```

### 6.5 Payment Service (Port 8083)

**Purpose**: Payment processing, Transaction management

**Database**: `court_booking_payments`

**Entities**:
- `Payment` - Payment transaction
- `PaymentStatus` - Enum (PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED)

**Controllers**:
- `PaymentController` - Payment management
  - `POST /api/payments` - Create payment
  - `POST /api/payments/{id}/process` - Process payment
  - `POST /api/payments/{id}/refund` - Refund payment
  - `GET /api/payments/{id}` - Get payment by ID
  - `GET /api/payments/user/{userId}` - Get payments by user
  - `GET /api/payments/booking/{bookingId}` - Get payments by booking

**PaymentService Deep Dive**:

```java
// PaymentService.java - Line by Line Analysis

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for booking: {}, user: {}", 
                 request.getBookingId(), request.getUserId());

        // Generate unique transaction ID
        // Format: TXN-XXXXXXXX (8 random uppercase hex chars)
        String transactionId = "TXN-" + UUID.randomUUID()
            .toString().substring(0, 8).toUpperCase();

        // Create Payment entity
        Payment payment = new Payment(
                null,  // id - auto-generated
                request.getBookingId(),
                request.getUserId(),
                request.getAmount(),
                PaymentStatus.PENDING,  // Initial status
                request.getPaymentMethod() != null ? 
                    request.getPaymentMethod() : "ONLINE",
                transactionId,  // Generated unique ID
                null,  // paymentDate - set by @PrePersist
                null,  // createdAt - set by @PrePersist
                null   // updatedAt - set by @PrePersist
        );

        // Save to database
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with transaction ID: {}", transactionId);

        // Return response DTO
        return mapToResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        log.info("Processing payment with ID: {}", paymentId);

        // Find payment or throw exception
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Payment not found with ID: " + paymentId));

        // Only PENDING payments can be processed
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException(
                "Payment cannot be processed. Current status: " + payment.getStatus());
        }

        // Update status to COMPLETED
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(java.time.LocalDateTime.now());

        // Save updated payment
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully. Transaction ID: {}", 
                 payment.getTransactionId());

        return mapToResponse(updatedPayment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Payment not found with ID: " + paymentId));

        // Only COMPLETED payments can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException(
                "Only completed payments can be refunded. Current status: " 
                + payment.getStatus());
        }

        // Update status to REFUNDED
        payment.setStatus(PaymentStatus.REFUNDED);

        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Payment refunded successfully. Transaction ID: {}", 
                 payment.getTransactionId());

        return mapToResponse(refundedPayment);
    }
}
```

---

## 7. API Documentation & Endpoints

### 7.1 Complete API Endpoint Reference

#### User Service (via API Gateway: http://localhost:8080)

| Method | Endpoint | Description | Auth | Request Body | Response |
|--------|----------|-------------|------|--------------|----------|
| POST | /auth/register | Register new user | No | `{username, email, password}` | `AuthResponse` |
| POST | /auth/login | User login | No | `{usernameOrEmail, password}` | `AuthResponse` |
| GET | /users/{id} | Get user by ID | JWT | - | `UserDTO` |
| GET | /users | Get all users | JWT | - | `[UserDTO]` |
| GET | /users/validate/{id} | Validate user exists | No | - | `Boolean` |

#### Booking Service (via API Gateway: http://localhost:8080)

| Method | Endpoint | Description | Auth | Request Body | Response |
|--------|----------|-------------|------|--------------|----------|
| POST | /courts | Add new court | JWT (ADMIN) | `CourtRequest` | `CourtDTO` |
| PUT | /courts/{id} | Update court | JWT (ADMIN) | `CourtRequest` | `CourtDTO` |
| DELETE | /courts/{id} | Delete court | JWT (ADMIN) | - | 204 |
| GET | /courts | Get all courts | JWT | - | `[CourtDTO]` |
| GET | /courts/{id} | Get court by ID | JWT | - | `CourtDTO` |
| GET | /courts/available | Get available courts | JWT | - | `[CourtDTO]` |
| POST | /bookings | Create booking | JWT | `BookingRequest` | `BookingDTO` |
| DELETE | /bookings/{id}?userId={id} | Cancel booking | JWT | - | 204 |
| GET | /bookings | Get all bookings | JWT | - | `[BookingDTO]` |
| GET | /bookings/user/{userId} | Get user bookings | JWT | - | `[BookingDTO]` |
| GET | /bookings/available?date={date} | Get available slots | JWT | - | `[BookingDTO]` |

#### Payment Service (via API Gateway: http://localhost:8080)

| Method | Endpoint | Description | Auth | Request Body | Response |
|--------|----------|-------------|------|--------------|----------|
| POST | /api/payments | Create payment | JWT | `PaymentRequest` | `PaymentResponse` |
| POST | /api/payments/{id}/process | Process payment | JWT | - | `PaymentResponse` |
| POST | /api/payments/{id}/refund | Refund payment | JWT | - | `PaymentResponse` |
| GET | /api/payments/{id} | Get payment by ID | JWT | - | `PaymentResponse` |
| GET | /api/payments/user/{userId} | Get payments by user | JWT | - | `[PaymentResponse]` |
| GET | /api/payments/booking/{bookingId} | Get payments by booking | JWT | - | `[PaymentResponse]` |

### 7.2 Request/Response Examples

#### POST /auth/register

**Request:**
```json
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
}
```

**Response (201 Created):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJqb2huZG9lIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MTQwNjA4MDAsImV4cCI6MTcxNDE0NzIwMH0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    "userId": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
}
```

#### POST /auth/login

**Request:**
```json
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "usernameOrEmail": "johndoe",
    "password": "password123"
}
```

**Response (200 OK):**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
}
```

#### POST /courts (Admin Only)

**Request:**
```json
POST http://localhost:8080/courts
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "name": "Court Alpha",
    "sportType": "Tennis",
    "location": "Ground Floor - Court 1",
    "pricePerHour": 50.00,
    "available": true,
    "description": "Professional tennis court with hard surface"
}
```

**Response (201 Created):**
```json
{
    "id": 1,
    "name": "Court Alpha",
    "sportType": "Tennis",
    "location": "Ground Floor - Court 1",
    "pricePerHour": 50.00,
    "available": true,
    "description": "Professional tennis court with hard surface"
}
```

#### POST /bookings

**Request:**
```json
POST http://localhost:8080/bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "userId": 1,
    "courtId": 1,
    "bookingDate": "2026-05-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "notes": "Morning practice session"
}
```

**Response (201 Created):**
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
    "notes": "Morning practice session"
}
```

---

## 8. Database Architecture

### 8.1 Database Overview

Each microservice has its own dedicated database for:
- **Data isolation** - Services can't access each other's data directly
- **Independent scaling** - Each database can scale separately
- **Fault isolation** - Database failure in one service doesn't affect others

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATABASE LAYER                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────┐ │
│  │ court_booking_users │  │ court_booking_db     │  │ court_booking_ │ │
│  │                     │  │                     │  │ payments       │ │
│  │ ┌─────────────────┐ │  │ ┌─────────────────┐ │  │                │ │
│  │ │     users       │ │  │ │    courts       │ │  │ ┌────────────┐ │ │
│  │ │                 │ │  │ │                │ │  │ │ payments   │ │ │
│  │ │ id (PK)         │ │  │ │ id (PK)        │ │  │ │            │ │ │
│  │ │ username        │ │  │ │ name           │ │  │ │ id (PK)    │ │ │
│  │ │ email           │ │  │ │ sport_type     │ │  │ │ booking_id │ │ │
│  │ │ password (bcrypt│ │  │ │ location       │ │  │ │ user_id    │ │ │
│  │ │ role            │ │  │ │ available      │ │  │ │ amount     │ │ │
│  │ │ enabled         │ │  │ │ price_per_hour │ │  │ │ status     │ │ │
│  │ │ created_at      │ │  │ │ description    │ │  │ │ txn_id     │ │ │
│  │ │ updated_at      │ │  │ │ created_at     │ │  │ │ payment_dt │ │ │
│  │ └─────────────────┘ │  │ └─────────────────┘ │  │ └────────────┘ │ │
│  │                     │  │                     │  │                │ │
│  │                     │  │ ┌─────────────────┐ │  │                │ │
│  │                     │  │ │    bookings     │ │  │                │ │
│  │                     │  │ │                │ │  │                │ │
│  │                     │  │ │ id (PK)        │ │  │                │ │
│  │                     │  │ │ user_id (FK)   │ │  │                │ │
│  │                     │  │ │ court_id (FK) │ │  │                │ │
│  │                     │  │ │ booking_date  │ │  │                │ │
│  │                     │  │ │ start_time    │ │  │                │ │
│  │                     │  │ │ end_time     │ │  │                │ │
│  │                     │  │ │ status       │ │  │                │ │
│  │                     │  │ │ notes        │ │  │                │ │
│  │                     │  │ │ payment_id   │ │  │                │ │
│  │                     │  │ └─────────────────┘ │  │                │ │
│  └─────────────────────┘  └─────────────────────┘  └────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.2 Database Schemas

#### court_booking_users Schema

```sql
-- Database creation
CREATE DATABASE IF NOT EXISTS court_booking_users;
USE court_booking_users;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Sample users (password: 'password123' encoded with BCrypt)
INSERT INTO users (username, email, password, role, enabled) VALUES
('admin', 'admin@courtbooking.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE),
('user1', 'user1@courtbooking.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
('user2', 'user2@courtbooking.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE);
```

#### court_booking_db Schema

```sql
CREATE DATABASE IF NOT EXISTS court_booking_db;
USE court_booking_db;

-- Courts table
CREATE TABLE IF NOT EXISTS courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sport_type VARCHAR(50) NOT NULL,
    location VARCHAR(200) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    price_per_hour DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
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
    
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE,
    
    INDEX idx_bookings_user_id (user_id),
    INDEX idx_bookings_court_id (court_id),
    INDEX idx_bookings_date (booking_date)
);

-- Sample courts
INSERT INTO courts (name, sport_type, location, available, description, price_per_hour) VALUES
('Court Alpha', 'Tennis', 'Ground Floor - Court 1', TRUE, 'Professional tennis court with hard surface', 50.00),
('Court Beta', 'Badminton', 'Ground Floor - Court 2', TRUE, 'Badminton court with carpet surface', 30.00),
('Court Gamma', 'Basketball', 'First Floor - Court 1', TRUE, 'Full size basketball court', 60.00),
('Court Delta', 'Volleyball', 'First Floor - Court 2', TRUE, 'Indoor volleyball court', 40.00),
('Court Epsilon', 'Tennis', 'Second Floor - Court 3', TRUE, 'Clay tennis court', 45.00);
```

#### court_booking_payments Schema

```sql
CREATE DATABASE IF NOT EXISTS court_booking_payments;
USE court_booking_payments;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(50) UNIQUE,
    payment_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_booking_id (booking_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status)
);
```

---

## 9. Security Implementation

### 9.1 Security Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     SECURITY LAYER ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Client Request                                                        │
│        │                                                                │
│        ▼                                                                │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                    API GATEWAY                                   │  │
│   │                                                                   │  │
│   │   ┌─────────────────────────────────────────────────────────┐   │  │
│   │   │           JwtAuthenticationFilter (-100)                │   │  │
│   │   │                                                         │   │  │
│   │   │   1. Extract Authorization header                       │   │  │
│   │   │   2. Parse JWT token                                    │   │  │
│   │   │   3. Validate signature with secret key                 │   │  │
│   │   │   4. Check expiration                                   │   │  │
│   │   │   5. Extract claims (userId, username, role)           │   │  │
│   │   │   6. Add X-User-* headers for downstream services       │   │  │
│   │   │                                                         │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │                         │                                       │  │
│   │                         ▼                                       │  │
│   │   ┌─────────────────────────────────────────────────────────┐   │  │
│   │   │        GatewayAuthorizationFilter (100)                 │   │  │
│   │   │                                                         │   │  │
│   │   │   1. Check path against public paths list               │   │  │
│   │   │   2. If not public, validate X-User-Role header         │   │  │
│   │   │   3. Compare role against path permissions              │   │  │
│   │   │   4. Allow/deny based on role permissions               │   │  │
│   │   │                                                         │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │                                                                   │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                   │
│                                    ▼                                   │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                    DOWNSTREAM SERVICES                           │  │
│   │   (User receives headers: X-User-Id, X-Username, X-User-Role)    │  │
│   │                                                                   │  │
│   │   Note: Services trust headers from API Gateway                  │  │
│   │         No need for separate JWT validation in services           │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 9.2 JWT Token Structure & Claims

```
JWT Token = Header.Payload.Signature

┌─────────────────────────────────────────────────────────────────────────┐
│                           JWT STRUCTURE                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  HEADER (Base64URL encoded):                                            │
│  {                                                                     │
│    "alg": "HS256",              ← HMAC-SHA256 algorithm                 │
│    "typ": "JWT"                 ← Token type                           │
│  }                                                                     │
│                                                                         │
│  PAYLOAD (Base64URL encoded):                                          │
│  {                                                                     │
│    "sub": "1",                    ← Subject (userId)                  │
│    "username": "john",             ← Username claim                     │
│    "role": "USER",                 ← Role claim (USER or ADMIN)         │
│    "iat": 1714060800,              ← Issued At (Unix timestamp)         │
│    "exp": 1714147200               ← Expiration (24 hours later)       │
│  }                                                                     │
│                                                                         │
│  SIGNATURE (HMAC-SHA256):                                              │
│  HMAC-SHA256(                                                          │
│    base64UrlEncode(header) + "." + base64UrlEncode(payload),           │
│    secret_key                                                           │
│  )                                                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 9.3 Password Security (BCrypt)

```java
// BCrypt is a password hashing function based on Blowfish cipher
// Key features:
// - Automatic salt generation (each hash includes unique salt)
// - Configurable work factor (cost factor) - higher = slower but more secure
// - Built-in timing attack protection

// How BCrypt works:
// 1. Generate random salt (16 bytes)
// 2. Hash password with salt using Blowfish cipher
// 3. Cost factor determines iterations (2^cost)
// 4. Store: $2a$10$salt$hash

// Example:
// Password: "password123"
// Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
//        │ │ └──┬──┘└──────┬──────┘└─────────────┬─────────────┐
//        │ │    │         │                    │             │
//        │ │    │         │                    │             └── 31 char hash
//        │ │    │         │                    └── 22 char salt (base64)
//        │ │    │         └── 22 char encoded salt
//        │ │    └── Work factor (2^10 = 1024 iterations)
//        │ └── Algorithm identifier (bcrypt)
//        └── Version ($2a = bcrypt with minor enhancement)

// Verification:
// BCrypt.matches(rawPassword, hashedPassword)
// - Decodes stored hash
// - Extracts salt
// - Hashes rawPassword with extracted salt
// - Compares hashes in constant time (prevents timing attacks)
```

### 9.4 Role-Based Access Control

```java
// Role Permissions Map (in GatewayAuthorizationFilter.java)

private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
        "/users", Set.of(USER_ROLE, ADMIN_ROLE),      // Both can access
        "/bookings", Set.of(USER_ROLE, ADMIN_ROLE),   // Both can access
        "/payments", Set.of(USER_ROLE, ADMIN_ROLE),   // Both can access
        "/courts", Set.of(USER_ROLE, ADMIN_ROLE)     // Both can access
);

// Public Paths (no auth required)
private static final List<String> PUBLIC_PATHS = List.of(
        "/auth/",          // Registration and login
        "/eureka/",        // Service registry dashboard
        "/actuator/",      // Health and metrics endpoints
        "/swagger-ui/",    // API documentation
        "/api-docs/"       // OpenAPI specification
);

// Access Control Logic:
/*
 * Path: /courts (add court)
 * User Role: USER
 * Result: FORBIDDEN (403) - User role not in permissions for /courts
 *
 * Path: /courts (add court)
 * User Role: ADMIN
 * Result: ALLOWED (200) - Admin role has access
 *
 * Path: /bookings (create booking)
 * User Role: USER
 * Result: ALLOWED (201) - User role has access
 */
```

---

## 10. Code Deep Dive - Line by Line Explanations

### 10.1 User Entity (User.java)

```java
package com.courtbooking.userservice.entity;

import jakarta.persistence.*;  // JPA annotations for ORM mapping

@Entity  // Marks this class as a JPA entity (maps to database table)
@Table(name = "users")  // Maps to 'users' table in database
public class User {
    
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(unique = true, nullable = false)  // NOT NULL, UNIQUE constraint
    private String username;

    @Column(unique = true, nullable = false)  // NOT NULL, UNIQUE constraint
    private String email;

    @Column(nullable = false)  // NOT NULL, will store BCrypt hash
    private String password;

    @Enumerated(EnumType.STRING)  // Store enum as VARCHAR in database
    @Column(nullable = false)  // NOT NULL, default is set in constructor
    private Role role;

    @Column(nullable = false)  // Account enabled/disabled flag
    private Boolean enabled = true;  // Default: enabled

    // Default constructor (required by JPA)
    public User() {
    }

    // All-args constructor
    public User(String username, String email, String password, Role role, Boolean enabled) {
        this.username = username;
        this.email = email;
        this.password = password;  // Should be BCrypt encoded before saving
        this.role = role;
        this.enabled = enabled;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
```

### 10.2 JwtService - Token Generation (User Service)

```java
@Service
public class JwtService {

    // Default secret for development (should be overridden in production)
    private static final String DEFAULT_SECRET = 
        "ThisIsAVerySecureSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm";

    @Value("${app.jwt.secret:" + DEFAULT_SECRET + "}")
    // Injected from application.yml, falls back to default if not set
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    // Token expiration in milliseconds (default: 24 hours)
    private long jwtExpiration;

    // Main method to generate JWT token
    public String generateToken(String username, Long userId, String role) {
        return Jwts.builder()  // Start building JWT
                .subject(String.valueOf(userId))  // "sub" claim = user ID
                .claim("username", username)  // Custom claim: username
                .claim("role", role)  // Custom claim: role (USER/ADMIN)
                .issuedAt(new Date())  // "iat" claim = issue time
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))  // "exp" claim
                .signWith(getSigningKey())  // Sign with HMAC-SHA256
                .compact();  // Convert to string (URL-safe base64)
    }

    // Validate token (check signature and expiration)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())  // Use secret to verify
                    .build()
                    .parseSignedClaims(token);  // Parse and validate
            return true;  // Token is valid
        } catch (Exception e) {
            return false;  // Token invalid or expired
        }
    }

    // Extract user ID from token
    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> Long.parseLong(claims.getSubject()));
        // claims.getSubject() returns the "sub" claim value
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    // Extract role from token
    public String getRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Generic method to extract any claim using a resolver function
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();  // Get all claims
        return claimsResolver.apply(claims);  // Apply function to claims
    }

    // Create signing key from secret string
    private SecretKey getSigningKey() {
        // HMAC-SHA256 requires 256-bit (32 bytes) minimum key
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
```

### 10.3 BookingRepository - Custom JPQL Query

```java
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Standard Spring Data methods (auto-implemented)
    List<Booking> findByUserId(Long userId);
    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    // Custom JPQL query for conflict detection
    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId " +
           "AND b.bookingDate = :date " +
           "AND b.status = 'CONFIRMED' " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("courtId") Long courtId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Query Explanation:
    // SELECT b FROM Booking b WHERE ...
    //   b.court.id = :courtId          → Same court
    //   b.bookingDate = :date          → Same date
    //   b.status = 'CONFIRMED'         → Only confirmed bookings
    //   b.startTime < :endTime         → Existing booking starts before new ends
    //   b.endTime > :startTime         → Existing booking ends after new starts
    //
    // Time Overlap Logic:
    // ─────────────────────────────────────────────────
    // Existing: |████|
    // New:              |░░░|
    // Overlap: No overlap (existing ends before new starts)
    //
    // Existing: |████|
    // New:      |░░░|
    // Overlap: FULL overlap (blocked!)
    //
    // Existing:    |████|
    // New:    |░░░|
    // Overlap: PARTIAL overlap (blocked!)
    //
    // Existing: |████|
    // New:         |░░|
    // Overlap: PARTIAL overlap (blocked!)
    //
    // Existing:   |████|
    // New:       |░░░░░|
    // Overlap: PARTIAL overlap (blocked!)
}
```

### 10.4 Payment Entity with Lifecycle Callbacks

```java
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    // precision = total digits, scale = decimal places
    // So 12345.67 needs precision=7, scale=2
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "transaction_id", unique = true)
    // Unique constraint - each transaction ID appears only once
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA Lifecycle Callbacks
    @PrePersist  // Called BEFORE INSERT (before saving to database)
    protected void onCreate() {
        createdAt = LocalDateTime.now();       // Set creation timestamp
        updatedAt = LocalDateTime.now();       // Set update timestamp
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now(); // Default payment date to now
        }
        if (status == null) {
            status = PaymentStatus.PENDING;    // Default status to PENDING
        }
    }

    @PreUpdate  // Called BEFORE UPDATE (before updating in database)
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();       // Update timestamp
    }
}
```

### 10.5 Common Module - AOP Logging Aspect

```java
@Aspect  // Marks this class as an Aspect
@Component  // Spring bean - auto-registered
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Pointcut: Match all controller and service methods
    @Pointcut("within(com.courtbooking.*.controller..*) || within(com.courtbooking.*.service..*)")
    public void controllerAndServicePointcut() {}

    // BEFORE advice: Log method entry
    @Before("controllerAndServicePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        // JoinPoint: Represents the method being executed
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();  // Fully qualified class name
        String methodName = signature.getName();              // Method name
        Object[] args = joinPoint.getArgs();                 // Method arguments

        logger.info("[ENTER] {}.{}() | Args: {}",
            className, methodName, Arrays.toString(args));
    }

    // AFTER RETURNING advice: Log method exit with result
    @AfterReturning(pointcut = "controllerAndServicePointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        logger.info("[EXIT] {}.{}() | Result: {}",
            className, methodName, result != null ? result.getClass().getSimpleName() : "void");
    }

    // AFTER THROWING advice: Log exceptions
    @AfterThrowing(pointcut = "controllerAndServicePointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        logger.error("[EXCEPTION] {}.{}() | Exception: {} | Message: {}",
            className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
    }

    // AROUND advice: Log execution time
    @Around("controllerAndServicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        try {
            Object result = joinPoint.proceed();  // Execute the actual method
            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("[TIMING] {}.{}() | Execution Time: {}ms",
                className, methodName, executionTime);

            return result;  // Return the result
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[TIMING-ERROR] {}.{}() | Failed after: {}ms | Error: {}",
                className, methodName, executionTime, ex.getMessage());
            throw ex;  // Re-throw the exception
        }
    }
}
```

---

## 11. Postman Testing Guide

### 11.1 Setting Up Postman

1. **Download and Install Postman**: https://www.postman.com/downloads/

2. **Create a New Collection**: "Court Booking API"

3. **Create Environment Variables**:
   ```
   Variable: baseUrl
   Initial Value: http://localhost:8080
   Current Value: http://localhost:8080
   
   Variable: token
   Initial Value: (leave empty)
   
   Variable: userId
   Initial Value: (leave empty)
   ```

### 11.2 Test Scenarios

#### Test 1: Register New User

```
Request:
POST {{baseUrl}}/auth/register
Content-Type: application/json

Body (raw JSON):
{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "password123"
}

Expected Response (201):
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "email": "testuser@example.com",
    "role": "USER"
}

Postman Script (Tests tab):
const response = pm.response.json();
if (response.token) {
    pm.collectionVariables.set("token", response.token);
}
if (response.userId) {
    pm.collectionVariables.set("userId", response.userId);
}
pm.test("Response has token", function() {
    pm.expect(response).to.have.property("token");
});
pm.test("Response has userId", function() {
    pm.expect(response).to.have.property("userId");
});
```

#### Test 2: Login

```
Request:
POST {{baseUrl}}/auth/login
Content-Type: application/json

Body (raw JSON):
{
    "usernameOrEmail": "testuser",
    "password": "password123"
}

Expected Response (200):
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "role": "USER"
}
```

#### Test 3: Add Court (Admin)

```
Request:
POST {{baseUrl}}/courts
Authorization: Bearer {{token}}
Content-Type: application/json

Body (raw JSON):
{
    "name": "Tennis Court Alpha",
    "sportType": "Tennis",
    "location": "Ground Floor",
    "pricePerHour": 50.00,
    "available": true,
    "description": "Professional tennis court"
}

Expected Response (201):
{
    "id": 1,
    "name": "Tennis Court Alpha",
    "sportType": "Tennis",
    "location": "Ground Floor",
    "pricePerHour": 50.00,
    "available": true,
    "description": "Professional tennis court"
}
```

#### Test 4: Get All Courts

```
Request:
GET {{baseUrl}}/courts
Authorization: Bearer {{token}}

Expected Response (200):
[
    {
        "id": 1,
        "name": "Tennis Court Alpha",
        "sportType": "Tennis",
        "location": "Ground Floor",
        "pricePerHour": 50.00,
        "available": true
    }
]
```

#### Test 5: Create Booking

```
Request:
POST {{baseUrl}}/bookings
Authorization: Bearer {{token}}
Content-Type: application/json

Body (raw JSON):
{
    "userId": {{userId}},
    "courtId": 1,
    "bookingDate": "2026-05-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "notes": "Morning practice"
}

Expected Response (201):
{
    "id": 1,
    "userId": 1,
    "courtId": 1,
    "courtName": "Tennis Court Alpha",
    "bookingDate": "2026-05-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "status": "CONFIRMED",
    "paymentId": 1,
    "amount": 50.00
}

Postman Script (Tests tab):
const response = pm.response.json();
if (response.paymentId) {
    pm.collectionVariables.set("paymentId", response.paymentId);
}
pm.test("Booking status is CONFIRMED", function() {
    pm.expect(response.status).to.equal("CONFIRMED");
});
pm.test("Payment created", function() {
    pm.expect(response.paymentId).to.be.a("number");
});
```

#### Test 6: Get User Bookings

```
Request:
GET {{baseUrl}}/bookings/user/{{userId}}
Authorization: Bearer {{token}}

Expected Response (200):
[
    {
        "id": 1,
        "userId": 1,
        "courtName": "Tennis Court Alpha",
        "bookingDate": "2026-05-20",
        "startTime": "10:00:00",
        "endTime": "11:00:00",
        "status": "CONFIRMED",
        "amount": 50.00
    }
]
```

#### Test 7: Get Payment Details

```
Request:
GET {{baseUrl}}/api/payments/{{paymentId}}
Authorization: Bearer {{token}}

Expected Response (200):
{
    "id": 1,
    "bookingId": 1,
    "userId": 1,
    "amount": 50.00,
    "status": "PENDING",
    "paymentMethod": "ONLINE",
    "transactionId": "TXN-12345678",
    "paymentDate": "2026-05-15T10:30:00"
}
```

#### Test 8: Process Payment

```
Request:
POST {{baseUrl}}/api/payments/{{paymentId}}/process
Authorization: Bearer {{token}}

Expected Response (200):
{
    "id": 1,
    "bookingId": 1,
    "amount": 50.00,
    "status": "COMPLETED",
    "transactionId": "TXN-12345678"
}
```

#### Test 9: Cancel Booking

```
Request:
DELETE {{baseUrl}}/bookings/1?userId={{userId}}
Authorization: Bearer {{token}}

Expected Response (204 No Content)
```

### 11.3 Complete Postman Collection

```json
{
    "info": {
        "name": "Court Booking API",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "variable": [
        {
            "key": "baseUrl",
            "value": "http://localhost:8080"
        },
        {
            "key": "token",
            "value": ""
        },
        {
            "key": "userId",
            "value": ""
        }
    ],
    "item": [
        {
            "name": "Register User",
            "request": {
                "method": "POST",
                "url": "{{baseUrl}}/auth/register",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {
                    "mode": "raw",
                    "raw": "{\"username\":\"testuser\",\"email\":\"testuser@example.com\",\"password\":\"password123\"}"
                }
            }
        },
        {
            "name": "Login",
            "request": {
                "method": "POST",
                "url": "{{baseUrl}}/auth/login",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "body": {
                    "mode": "raw",
                    "raw": "{\"usernameOrEmail\":\"testuser\",\"password\":\"password123\"}"
                }
            }
        },
        {
            "name": "Add Court",
            "request": {
                "method": "POST",
                "url": "{{baseUrl}}/courts",
                "header": [
                    {"key": "Content-Type", "value": "application/json"},
                    {"key": "Authorization", "value": "Bearer {{token}}"}
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\"name\":\"Tennis Court Alpha\",\"sportType\":\"Tennis\",\"location\":\"Ground Floor\",\"pricePerHour\":50.00,\"available\":true}"
                }
            }
        },
        {
            "name": "Create Booking",
            "request": {
                "method": "POST",
                "url": "{{baseUrl}}/bookings",
                "header": [
                    {"key": "Content-Type", "value": "application/json"},
                    {"key": "Authorization", "value": "Bearer {{token}}"}
                ],
                "body": {
                    "mode": "raw",
                    "raw": "{\"userId\":{{userId}},\"courtId\":1,\"bookingDate\":\"2026-05-20\",\"startTime\":\"10:00:00\",\"endTime\":\"11:00:00\"}"
                }
            }
        }
    ]
}
```

---

## 12. Swagger/OpenAPI Documentation Guide

### 12.1 Accessing Swagger UI

| Service | URL | Description |
|---------|-----|-------------|
| User Service | http://localhost:8081/swagger-ui.html | User auth endpoints |
| Booking Service | http://localhost:8082/swagger-ui.html | Court and booking endpoints |
| Payment Service | http://localhost:8083/swagger-ui.html | Payment endpoints |

### 12.2 OpenAPI Configuration

```yaml
# user-service/src/main/resources/application.yml

springdoc:
  api-docs:
    path: /api-docs          # OpenAPI JSON: http://localhost:8081/api-docs
  swagger-ui:
    path: /swagger-ui.html   # Swagger UI: http://localhost:8081/swagger-ui.html
```

### 12.3 Swagger Annotations Usage

```java
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
// @Tag: Groups endpoints in Swagger UI under "Authentication" section
public class AuthController {

    @PostMapping("/register")
    @Operation(summary = "Register a new user", 
               description = "Creates a new user account with USER role")
    // @Operation: Adds summary and description to this endpoint
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", 
               description = "Authenticates user and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
```

### 12.4 Using Swagger UI

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         SWAGGER UI GUIDE                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. Navigate to: http://localhost:8081/swagger-ui.html                 │
│                                                                         │
│  2. You will see grouped endpoints:                                     │
│     ├── Authentication                                                  │
│     │   ├── POST /auth/register                                        │
│     │   └── POST /auth/login                                           │
│     ├── User Management                                                 │
│     │   ├── GET /users/{id}                                            │
│     │   └── GET /users                                                 │
│     └── User Validation                                                │
│         └── GET /users/validate/{id}                                   │
│                                                                         │
│  3. Click on an endpoint to expand it                                  │
│                                                                         │
│  4. Click "Try it out" button                                         │
│                                                                         │
│  5. Fill in required parameters/JSON body                             │
│                                                                         │
│  6. Click "Execute" to send the request                               │
│                                                                         │
│  7. View the response (status, body, time)                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 13. Configuration Management

### 13.1 Configuration Files Overview

```
Each service has: src/main/resources/application.yml

Key configurations:
├── Eureka Client Config (all services)
├── Database Config (per service)
├── JWT Config (api-gateway, user-service)
├── Route Config (api-gateway)
└── Swagger/OpenAPI Config (user, booking, payment)
```

### 13.2 API Gateway Configuration

```yaml
# api-gateway/src/main/resources/application.yml

server:
  port: 8080  # API Gateway port

spring:
  main:
    web-application-type: reactive  # WebFlux (non-blocking)
  application:
    name: api-gateway
  cloud:
    gateway:
      # Route configurations
      routes:
        - id: user-service
          # Load balanced routing to user-service registered in Eureka
          uri: lb://user-service
          predicates:
            # Match paths: /auth/** OR /users/** OR /users
            - Path=/auth/**,/users/**,/users
          filters:
            - StripPrefix=0  # Don't strip path prefix
        
        - id: booking-service
          uri: lb://booking-service
          predicates:
            - Path=/courts/**,/bookings/**,/bookings,/courts
          filters:
            - StripPrefix=0
        
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
          filters:
            - StripPrefix=0

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/  # Eureka server URL
  instance:
    hostname: localhost

app:
  jwt:
    secret: ${JWT_SECRET:ThisIsAVerySecureSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm}
    expiration: 86400000  # 24 hours in milliseconds

# Key points:
# - lb:// prefix enables load balancing via Eureka
# - Multiple paths can be matched with comma separation
# - Environment variables (${JWT_SECRET}) allow secure configuration
# - Default values after : handle missing environment variables
```

### 13.3 Service Configuration (Example: user-service)

```yaml
# user-service/src/main/resources/application.yml

server:
  port: 8081  # Service port

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://localhost:3306/court_booking_users
         ?createDatabaseIfNotExist=true     # Auto-create DB if not exists
         &useSSL=false                      # Disable SSL for local dev
         &allowPublicKeyRetrieval=true      # Allow key retrieval
         &serverTimezone=UTC                # Set timezone
    username: ${DB_USERNAME:root}          # From environment variable
    password: ${DB_PASSWORD:root}          # From environment variable
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update                      # Auto-create/update tables
    show-sql: true                          # Log SQL queries
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    hostname: localhost

app:
  jwt:
    secret: ${JWT_SECRET:ThisIsAVerySecureSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm}
    expiration: 86400000

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  level:
    com.courtbooking: DEBUG
  file:
    name: logs/user-service.log
    max-size: 10MB
    max-history: 30
```

---

## 14. AOP (Aspect-Oriented Programming) in Common Module

### 14.1 What is AOP?

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        AOP CONCEPT OVERVIEW                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Cross-Cutting Concerns:                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │   Logging    │  │Performance  │  │   Security   │  │  Exception   ││
│  │              │  │  Monitoring  │  │   Check      │  │   Handling   ││
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘│
│                                                                         │
│  These concerns apply to MANY methods across the application:          │
│                                                                         │
│  public class UserService {                                             │
│      public AuthResponse register(...) { // Need logging, perf, exc     │
│      public AuthResponse login(...) { // Need logging, perf, exc        │
│      public UserDTO getUser(...) { // Need logging, perf, exc           │
│  }                                                                      │
│                                                                         │
│  Without AOP: Copy-paste logging code in every method                 │
│  With AOP: Create ONE aspect that applies to all methods              │
│                                                                         │
│  Aspect = Pointcut + Advice                                            │
│  ├── Pointcut: "When to apply?" (which methods to match)                │
│  └── Advice: "What to do?" (logging, timing, etc)                      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 14.2 Common Module Aspects

#### LoggingAspect

```java
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.courtbooking.*.controller..*) || within(com.courtbooking.*.service..*)")
    public void controllerAndServicePointcut() {}
    // Pointcut: Match all methods in controllers and services

    @Before("controllerAndServicePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        // Advice executed BEFORE the method
        logger.info("[ENTER] {}.{}()", className, methodName);
    }

    @AfterReturning(pointcut = "controllerAndServicePointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        // Advice executed AFTER successful method completion
        logger.info("[EXIT] {}.{}()", className, methodName);
    }

    @AfterThrowing(pointcut = "controllerAndServicePointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        // Advice executed when method throws exception
        logger.error("[EXCEPTION] {}.{}()", className, exception.getMessage());
    }
}
```

#### PerformanceAspect

```java
@Aspect
@Component
public class PerformanceAspect {

    @Value("${app.performance.threshold:500}")
    private long thresholdMs;  // Configurable threshold (default 500ms)

    @Around("within(com.courtbooking.*.controller..*) || within(com.courtbooking.*.service..*)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        // @Around: Executed before and after method (can modify behavior)
        
        long startTime = System.nanoTime();
        try {
            result = joinPoint.proceed();  // Execute the actual method
            return result;
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            
            if (durationMs > thresholdMs) {
                // Log warning for slow operations
                logger.warn("[PERFORMANCE-SLOW] {}.{}() | Duration: {}ms", ...);
            }
        }
    }
}
```

### 14.3 How Aspects are Applied

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ASPECT EXECUTION FLOW                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  UserService.register() is called                                       │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ LoggingAspect.logBefore()                                        │  │
│  │   "Entering UserService.register()"                              │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ PerformanceAspect.monitorPerformance()                           │  │
│  │   - Start timer                                                   │  │
│  │   - proceed() → Execute actual method                            │  │
│  │   - Stop timer                                                    │  │
│  │   - If duration > 500ms, log warning                             │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ UserService.register() executes                                  │  │
│  │   - Validate username, email                                      │  │
│  │   - Encode password                                               │  │
│  │   - Save to database                                              │  │
│  │   - Generate JWT                                                  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ LoggingAspect.logAfterReturning()                                │  │
│  │   "Exiting UserService.register()"                               │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  If exception occurs:                                                   │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ LoggingAspect.logAfterThrowing()                                 │  │
│  │   "Exception in UserService.register(): BadRequestException"     │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 15. Running & Deployment Guide

### 15.1 Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- IDE (IntelliJ IDEA recommended)

### 15.2 Database Setup

```sql
-- Run in MySQL

-- Create databases for each service
CREATE DATABASE IF NOT EXISTS court_booking_users;
CREATE DATABASE IF NOT EXISTS court_booking_db;
CREATE DATABASE IF NOT EXISTS court_booking_payments;

-- Verify databases created
SHOW DATABASES;
```

### 15.3 Build the Project

```bash
# Navigate to project root
cd C:\CourtBook

# Clean and build all modules
mvn clean install

# To skip tests (faster build)
mvn clean install -DskipTests
```

### 15.4 Starting Services (Order Matters!)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        STARTUP ORDER                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. EUREKA SERVER (must start first)                                    │
│     └─ cd eureka-server && mvn spring-boot:run                         │
│     └─ Wait for: "Started EurekaServerApplication"                    │
│     └─ Verify: http://localhost:8761 (Eureka Dashboard)                 │
│                                                                         │
│  2. API GATEWAY                                                         │
│     └─ cd api-gateway && mvn spring-boot:run                           │
│     └─ Wait for: "Started ApiGatewayApplication"                      │
│     └─ Verify: http://localhost:8080/actuator/health                    │
│                                                                         │
│  3. USER SERVICE                                                        │
│     └─ cd user-service && mvn spring-boot:run                         │
│     └─ Wait for: "Started UserServiceApplication"                     │
│     └─ Verify: http://localhost:8081/actuator/health                   │
│     └─ Wait for registration in Eureka (appears in dashboard)           │
│                                                                         │
│  4. BOOKING SERVICE                                                     │
│     └─ cd booking-service && mvn spring-boot:run                       │
│     └─ Wait for: "Started BookingServiceApplication"                  │
│     └─ Verify: http://localhost:8082/actuator/health                   │
│     └─ Wait for registration in Eureka                                 │
│                                                                         │
│  5. PAYMENT SERVICE                                                     │
│     └─ cd payment-service && mvn spring-boot:run                       │
│     └─ Wait for: "Started PaymentServiceApplication"                  │
│     └─ Verify: http://localhost:8083/actuator/health                   │
│     └─ Wait for registration in Eureka                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 15.5 Verify All Services Running

```bash
# Check Eureka dashboard
# Open browser: http://localhost:8761

# Should see all services registered:
# - API-GATEWAY (UP)
# - USER-SERVICE (UP)
# - BOOKING-SERVICE (UP)
# - PAYMENT-SERVICE (UP)

# Health check each service
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Booking Service
curl http://localhost:8083/actuator/health  # Payment Service
```

### 15.6 Docker Deployment (Optional)

```dockerfile
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: court_booking_users,court_booking_db,court_booking_payments
    ports:
      - "3306:3306"

  eureka:
    build: ./eureka-server
    ports:
      - "8761:8761"

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka

  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    depends_on:
      - eureka
      - mysql

  booking-service:
    build: ./booking-service
    ports:
      - "8082:8082"
    depends_on:
      - eureka
      - mysql

  payment-service:
    build: ./payment-service
    ports:
      - "8083:8083"
    depends_on:
      - eureka
      - mysql
```

---

## 16. Troubleshooting & Common Issues

### 16.1 Common Issues and Solutions

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TROUBLESHOOTING GUIDE                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ISSUE: Services not registering with Eureka                            │
│  ─────────────────────────────────────────────────────────────────────   │
│  Cause: Eureka server not running or network issue                      │
│  Solution:                                                              │
│    1. Start Eureka server first                                         │
│    2. Check eureka.client.serviceUrl in application.yml                 │
│    3. Verify network connectivity (ping localhost)                     │
│    4. Check Eureka dashboard at http://localhost:8761                   │
│                                                                         │
│  ISSUE: JWT Token validation failing                                     │
│  ─────────────────────────────────────────────────────────────────────   │
│  Cause: JWT secret mismatch between services                            │
│  Solution:                                                              │
│    1. Ensure same JWT secret in all services                            │
│    2. Check API Gateway logs for token parsing errors                   │
│    3. Verify token not expired                                          │
│    4. Check JWT secret has minimum 256 bits                             │
│                                                                         │
│  ISSUE: Database connection errors                                       │
│  ─────────────────────────────────────────────────────────────────────   │
│  Cause: MySQL not running or wrong credentials                           │
│  Solution:                                                              │
│    1. Verify MySQL is running: mysql -u root -p                         │
│    2. Check database name in connection string                           │
│    3. Verify username/password in application.yml                       │
│    4. Create databases if not exist                                     │
│                                                                         │
│  ISSUE: 401 Unauthorized on all requests                                │
│  ─────────────────────────────────────────────────────────────────────   │
│  Cause: Token not sent or invalid                                       │
│  Solution:                                                              │
│    1. Ensure Authorization header is sent: "Bearer <token>"             │
│    2. Check token is not expired                                         │
│    3. Verify path is not in PUBLIC_PATHS list                           │
│    4. Check API Gateway JWT filter is running (-100 order)              │
│                                                                         │
│  ISSUE: Payment creation failing                                         │
│  ─────────────────────────────────────────────────────────────────────   │
│  Cause: Payment service not reachable or wrong URL                      │
│  Solution:                                                              │
│    1. Verify Payment Service is running (port 8083)                     │
│    2. Check WebClient base URL in AppConfig                              │
│    3. Verify route in API Gateway: /api/payments/**                     │
│    4. Check Payment Service logs for errors                             │
│                                                                         │
│  ISSUE: Time slot conflicts not being detected                          │
│  ─────────────────────────────────────────────────────────────────────   │
│  Cause: JPQL query logic issue                                          │
│  Solution:                                                              │
│    1. Verify findConflictingBookings query                              │
│    2. Check BookingRepository for correct query                         │
│    3. Test with overlapping time ranges                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 16.2 Log Locations

```
# Log files location (in project root)
logs/
├── api-gateway.log
├── user-service.log
├── booking-service.log
├── payment-service.log
└── eureka-server.log

# View recent logs
tail -100 logs/user-service.log

# Search for errors
grep -i "error" logs/user-service.log
grep -i "exception" logs/booking-service.log
```

### 16.3 Debug Mode

```yaml
# Enable debug logging in application.yml
logging:
  level:
    root: INFO
    com.courtbooking: DEBUG
    org.springframework.web: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## Appendix A: API Quick Reference

| Service | Endpoint | Method | Auth | Description |
|---------|----------|--------|------|-------------|
| User | /auth/register | POST | No | Register user |
| User | /auth/login | POST | No | Login |
| User | /users/{id} | GET | JWT | Get user |
| Court | /courts | POST | JWT+ADMIN | Add court |
| Court | /courts | GET | JWT | List courts |
| Court | /courts/available | GET | JWT | Available courts |
| Booking | /bookings | POST | JWT | Create booking |
| Booking | /bookings/{id} | DELETE | JWT | Cancel booking |
| Booking | /bookings/user/{id} | GET | JWT | User bookings |
| Payment | /api/payments | POST | JWT | Create payment |
| Payment | /api/payments/{id}/process | POST | JWT | Process payment |
| Payment | /api/payments/{id}/refund | POST | JWT | Refund payment |

---

## Appendix B: Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DB_USERNAME | root | MySQL username |
| DB_PASSWORD | root | MySQL password |
| JWT_SECRET | (built-in) | JWT signing key |

---

## Appendix C: Port Summary

| Service | Port | Purpose |
|---------|------|---------|
| Eureka Server | 8761 | Service registry |
| API Gateway | 8080 | Entry point |
| User Service | 8081 | User management |
| Booking Service | 8082 | Booking management |
| Payment Service | 8083 | Payment processing |

---

## Appendix D: Swagger URLs

| Service | Swagger UI | API Docs (JSON) |
|---------|------------|-----------------|
| User Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Booking Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Payment Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |

---

*This documentation was generated for the Court Booking Microservices Application. For questions or updates, refer to the project README or contact the development team.*